package cz.cuni.mff.core.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages a llamafile backend process: launching, readiness polling, stopping,
 * and status notification.
 */
public final class LlamafileRuntimeManager implements RuntimeManager {

    private static final Duration READY_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(1000);
    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration STOP_TIME = Duration.ofSeconds(5);
    private final AtomicReference<RuntimeStatus> status = new AtomicReference<>(RuntimeStatus.STOPPED);
    private volatile Process process;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final List<RuntimeListener> listeners = new CopyOnWriteArrayList<>(); // multiple writers

    /**
     * Creates a runtime manager and registers a shutdown hook that kills the
     * child process if the application exits while it is still running.
     */
    public LlamafileRuntimeManager() {
        Runtime.getRuntime().addShutdownHook(
                Thread.ofPlatform().name("llamafile-cleanup").unstarted(this::cleanupOnExit)
            );
    }

    @Override
    public RuntimeStatus status() {
        return status.get();
    }

    @Override 
    public void addListener(RuntimeListener l) { 
        listeners.add(l); 
    }

    /**
     * Updates the status and notifies all listeners.
     */
    private void setStatus(RuntimeStatus newStatus) {
        status.set(newStatus);
        listeners.forEach(l -> l.onStatusChanged(newStatus));
    }

    @Override
    public CompletableFuture<RuntimeStatus> start(RuntimeConfig config, RuntimeLog log) {
        if (!tryStart()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Runtime is already active"));
        }
        try {
            ProcessBuilder pb = new ProcessBuilder(config.toCommandLine());
            pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
            pb.redirectError(ProcessBuilder.Redirect.PIPE);
            this.process = pb.start();
            redirect(process.getInputStream(), log);
            redirect(process.getErrorStream(), log);

            process.onExit().thenRun(() -> {
                if (status.get() == RuntimeStatus.RUNNING) {
                    setStatus(RuntimeStatus.FAILED);
                }
            });
        } catch (IOException e) {
            setStatus(RuntimeStatus.FAILED);
            return CompletableFuture.failedFuture(new IllegalStateException("Failed to launch llamafile", e));
        }
        return waitForRunning(config).whenComplete((s, e) -> {
            if (e != null) {
                setStatus(RuntimeStatus.FAILED);
                if (process != null && process.isAlive()) {
                    process.destroyForcibly();
                }
            } else {
                setStatus(s);
            }
        });

    }

    @Override
    public CompletableFuture<Void> stop() {
        if (process == null || !process.isAlive()) {
            setStatus(RuntimeStatus.STOPPED);
            return CompletableFuture.completedFuture(null);
        }
        setStatus(RuntimeStatus.STOPPING);
        process.destroy();
        return process
                .onExit()
                .orTimeout(STOP_TIME.toMillis(), TimeUnit.MILLISECONDS)
                .handle((v, e) -> {
                    if (e != null) {
                        process.destroyForcibly();
                        try {
                            process.waitFor();
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    setStatus(RuntimeStatus.STOPPED);
                    return null;
                });
    }

    /**
     * Attempts to transition the runtime status from STOPPED or FAILED to STARTING.
     * @return true if the transition was successful, false otherwise.
     */
    private boolean tryStart() {
        RuntimeStatus s = status.get();
        if (s != RuntimeStatus.STOPPED && s != RuntimeStatus.FAILED) {
            return false;
        }
        setStatus(RuntimeStatus.STARTING);
        return true;
    }

    /**
     * Waits for the llamafile process to become ready by polling the /v1/models endpoint.
     * @param config
     * @return A CompletableFuture that completes with RuntimeStatus.RUNNING when the process is ready, 
     * or completes exceptionally if the process fails to become ready within the timeout.
     */
    private CompletableFuture<RuntimeStatus> waitForRunning(RuntimeConfig config) {
        return CompletableFuture.supplyAsync(() -> {
            URI uri = URI.create("http://" + config.host() + ":" + config.port() + "/v1/models");
            HttpRequest req = HttpRequest.newBuilder(uri).GET().timeout(POLL_TIMEOUT).build();
            long deadline = System.nanoTime() + READY_TIMEOUT.toNanos();
            while (System.nanoTime() < deadline) {
                if (process == null || !process.isAlive()) {
                    throw new IllegalStateException("llamafile exited before becoming ready");
                }
                if (poll(req).join()) {
                    return RuntimeStatus.RUNNING;
                }
                try {
                    Thread.sleep(POLL_INTERVAL.toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Thread interrupted while waiting for llamafile to become ready", e);
                }
            }
            throw new IllegalStateException("llamafile did not become ready within " + READY_TIMEOUT.toSeconds() + " seconds");
        });
    }

    private CompletableFuture<Boolean> poll(HttpRequest req) {
        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.discarding())
                .thenApply(resp -> resp.statusCode() == 200)
                .exceptionally(e -> false);
    }
    
    /**
     * Kills the child process if the program exits while the runtime is still running.
     */
    private void cleanupOnExit() {
        Process p = process;
        if (p == null || !p.isAlive()) {
            return;
        }
        p.destroy();
        try {
            if (!p.waitFor(STOP_TIME.toMillis(), TimeUnit.MILLISECONDS)) {
                p.destroyForcibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            p.destroyForcibly();
        }
    }

    private static void redirect(InputStream in, RuntimeLog log) {
        Thread.ofPlatform().daemon().name("llamafile-output").start(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.append(line);
                }
            } catch (IOException e) {
               // stream closed, ignore
            }
        });
    }

}

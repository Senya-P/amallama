package cz.cuni.mff.core.download;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;


/**
 * 
 * A model downloader that downloads models from Hugging Face.
 */
public final class HFModelDownloader implements ModelDownloader {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);
    private static final int BUFFER_SIZE = 64 * 1024;
    private final HttpClient http;
    private final Path downloadDirectory;

    public HFModelDownloader(Path targetDir) {
        this.downloadDirectory = targetDir;
        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            throw new DownloadException("Cannot create models directory: " + targetDir + " — " + e.getMessage());
        }
        this.http = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
    }
    @Override
    public CompletableFuture<Path> download(String url, Consumer<DownloadProgress> onProgress) {
        Consumer<DownloadProgress> progress = onProgress == null ? p -> {} : onProgress;
        Path target;
        Path partial;
        try {
            target = resolveTarget(url);
            partial = target.resolveSibling(target.getFileName() + ".part");
            Files.deleteIfExists(partial);
        } catch (DownloadException e) {
            return CompletableFuture.failedFuture(e);
        } catch (IOException e) {
            return CompletableFuture.failedFuture(new DownloadException("Download failed: " + e.getMessage()));
        }
        CompletableFuture<Path> result = new CompletableFuture<>();
        AtomicReference<InputStream> active = new AtomicReference<>();

        result.whenComplete((p, e) -> {
            if (result.isCancelled() && active.get() != null) {
                try {
                    active.get().close();
                } catch (IOException ex) {}
            }
        });

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
            .timeout(REQUEST_TIMEOUT)
            .header("User-Agent", "amallama/1")
            .GET()
            .build();
        http.sendAsync(request, BodyHandlers.ofInputStream())
                .whenComplete((response, error) -> {
                    try {
                        if (error != null) { 
                            result.completeExceptionally(new DownloadException("Download failed: " + error.getMessage())); 
                            return; 
                        }
                        int code = response.statusCode();
                        if (code == 404) throw new DownloadException("Model not found (HTTP 404)");
                        if (code < 200 || code >= 300) throw new DownloadException("Download failed: HTTP " + code);
                        active.set(response.body());
                        long total = response.headers().firstValueAsLong("Content-Length").orElse(0L);
                        writeFile(response.body(), target, partial, total, progress, result);
                    } catch (Exception e) {
                        try {
                            Files.deleteIfExists(partial);
                        }
                        catch (IOException ex) {}
                        if (!result.isDone()) {
                            result.completeExceptionally(new DownloadException("Download failed: " + e.getMessage()));
                        }
                    }
                });
        return result;
    }
    private Path resolveTarget(String url) throws DownloadException {
        try {
            String path = URI.create(url.trim()).getPath();
            if (path == null || path.isEmpty()) throw new DownloadException("Invalid URL: " + url);
            String name = URLDecoder.decode(path.substring(path.lastIndexOf('/') + 1), StandardCharsets.UTF_8);
            if (name.isEmpty()) throw new DownloadException("Invalid file name: " + url);
            if (!name.toLowerCase().endsWith(".gguf")) throw new DownloadException("Only .gguf models: " + name);
            return downloadDirectory.resolve(name).normalize();
        } catch (IllegalArgumentException e) {
            throw new DownloadException("Invalid URL: " + url + " — " + e.getMessage());
        }
    }

    private void writeFile(InputStream in, Path target, Path partial, long total,
            Consumer<DownloadProgress> progress, CompletableFuture<Path> result) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        long written = 0;
        try (InputStream source = in; OutputStream out = Files.newOutputStream(partial)) {
            int read;
            while ((read = source.read(buffer)) != -1) {
                if (result.isCancelled()) throw new CancellationException("Download cancelled");
                out.write(buffer, 0, read);
                written += read;
                progress.accept(new DownloadProgress(target.getFileName().toString(), written, total));
            }
            if (result.isCancelled()) throw new CancellationException("Download cancelled");
            Files.move(partial, target, StandardCopyOption.REPLACE_EXISTING);
        }
        result.complete(target);
    }

}

package cz.cuni.mff.core;

import cz.cuni.mff.core.chat.ChatClient;
import cz.cuni.mff.core.chat.ChatMessage;
import cz.cuni.mff.core.chat.ChatRequest;
import cz.cuni.mff.core.chat.ChatResponse;
import cz.cuni.mff.core.chat.OpenAIChatClient;
import cz.cuni.mff.core.runtime.RuntimeConfig;
import cz.cuni.mff.core.runtime.RuntimeListener;
import cz.cuni.mff.core.runtime.RuntimeLog;
import cz.cuni.mff.core.runtime.RuntimeManager;
import cz.cuni.mff.core.runtime.RuntimePlan;
import cz.cuni.mff.core.runtime.RuntimeStatus;
import cz.cuni.mff.core.runtime.RuntimeTelemetry;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Single backend facade. Keeps the in-memory conversation history.
 */
public final class Session {

    private static final String DEFAULT_MODEL = "llamafile";
    private static final int MAX_LOG_LINES = 1500;

    private final RuntimeManager runtime;
    private final List<ChatMessage> history = new CopyOnWriteArrayList<>();
    private volatile ChatClient chat;
    private volatile String lastError;
    private String modelName;
    private RuntimeLog log;
    private RuntimePlan plan;

    public Session(RuntimeManager runtime) {
        this.runtime = runtime;
    }

    /**
     * Starts the runtime and prepares the chat client.
     *
     * @param plan the runtime plan to apply; kept for the UI to display
     * @return a future completing with {@link RuntimeStatus#RUNNING} on success
     * or {@link RuntimeStatus#FAILED} on failure; never completes
     * exceptionally, the reason is in {@link #lastError()}
     */
    public CompletableFuture<RuntimeStatus> start(RuntimePlan plan) {
        this.plan = plan;
        RuntimeConfig config = plan.config();
        modelName = fileNameOf(config);
        log = new RuntimeLog(MAX_LOG_LINES);
        return runtime.start(config, log).handle((status, error) -> {
            if (error != null) {
                lastError = messageOf(error);
                return RuntimeStatus.FAILED;
            }
            if (status == RuntimeStatus.RUNNING) {
                chat = new OpenAIChatClient("http://" + config.host() + ":" + config.port());
            }
            return status;
        });
    }

    /**
     * Sends a user message and returns the model's reply. 
     * The message is appended to the in-memory history, which is sent with every request to the model.
     *
     * @param message The user message
     * @return a future completing with the model's reply, or exceptionally on failure
     */
    public CompletableFuture<ChatResponse> send(String message) {
        if (chat == null) {
            lastError = "Runtime is not running";
            return CompletableFuture.failedFuture(new IllegalStateException(lastError));
        }
        ChatMessage userMessage = new ChatMessage("user", message);
        List<ChatMessage> requestMessages = new ArrayList<>(history);
        requestMessages.add(userMessage);
        return chat.send(new ChatRequest(DEFAULT_MODEL, requestMessages))
                .whenComplete((response, error) -> {
                    if (error != null) {
                        lastError = messageOf(error);
                    } else {
                        history.add(userMessage);
                        history.add(new ChatMessage("assistant", response.content()));
                    }
                });
    }

    /**
     * Stops the runtime and detaches the chat client.
     * @return a future completing when the runtime has stopped
     */
    public CompletableFuture<Void> stop() {
        chat = null;
        return runtime.stop();
    }

    /**
     * Restarts the runtime with the new plan.
     * @param plan The new runtime plan
     * @return a future completing with the new runtime status
     */
    public CompletableFuture<RuntimeStatus> restart(RuntimePlan plan) {
        history.clear();
        return stop().thenCompose(v -> start(plan));
    }

    /**
     * @return the plan the runtime is started with, or {@code null} if it has never been started
     */
    public RuntimePlan plan() {
        return plan;
    }

    /**
     * @return the current runtime status
     */
    public RuntimeStatus status() {
        return runtime.status();
    }

    /**
     * @return the file name of the loaded model 
     * (or of the backend binary when no separate model file is used), 
     * or {@code null} if never started
     */
    public String modelName() {
        return modelName;
    }

    /**
     * @return The telemetry parsed from the current run's output,
     * or {@code null} if the runtime has not been started
     */
    public RuntimeTelemetry telemetry() {
        return log == null ? null : log.telemetry();
    }

    private static String fileNameOf(RuntimeConfig config) {
        Path file = config.modelPath() != null ? config.modelPath() : config.backendBinary();
        return file != null ? file.getFileName().toString() : null;
    }

    /**
     * @return the message of the last failure, or {@code null} if there was none
     */
    public String lastError() {
        return lastError;
    }

    /**
     * Adds a listener to receive runtime status change notifications.
     * @param listener The listener to add.
     */
    public void addListener(RuntimeListener listener) { 
        runtime.addListener(listener); 
    }

    /**
     * Returns a user-friendly message for the given error.
     * @param error
     * @return a message describing the error
     */
    private static String messageOf(Throwable error) {
        Throwable cause = error.getCause() != null ? error.getCause() : error;
        return cause.getMessage() != null ? cause.getMessage() : cause.toString();
    }
}
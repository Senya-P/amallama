package cz.cuni.mff.core;

import cz.cuni.mff.core.chat.ChatClient;
import cz.cuni.mff.core.chat.ChatMessage;
import cz.cuni.mff.core.chat.ChatRequest;
import cz.cuni.mff.core.chat.ChatResponse;
import cz.cuni.mff.core.chat.OpenAIChatClient;
import cz.cuni.mff.core.runtime.RuntimeConfig;
import cz.cuni.mff.core.runtime.RuntimeListener;
import cz.cuni.mff.core.runtime.RuntimeManager;
import cz.cuni.mff.core.runtime.RuntimeStatus;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Single backend facade. Keeps the in-memory conversation history.
 */
public final class Session {

    /**
     * Model id sent in chat requests. The llamafile server does not validate
     * this field against the loaded model, so a fixed value is sufficient.
     */
    private static final String DEFAULT_MODEL = "llamafile";

    private final RuntimeManager runtime;
    private final List<ChatMessage> history = new ArrayList<>();
    private ChatClient chat;
    private String lastError;
    private String modelName;

    public Session(RuntimeManager runtime) {
        this.runtime = runtime;
    }

    /**
     * Starts the runtime and prepares the chat client.
     *
     * @param config runtime configuration
     * @return a future completing with {@link RuntimeStatus#RUNNING} on success
     * or {@link RuntimeStatus#FAILED} on failure; never completes
     * exceptionally, the reason is in {@link #lastError()}
     */
    public CompletableFuture<RuntimeStatus> start(RuntimeConfig config) {
        modelName = fileNameOf(config);
        return runtime.start(config).handle((status, error) -> {
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
        history.add(new ChatMessage("user", message));
        return chat.send(new ChatRequest(DEFAULT_MODEL, List.copyOf(history)))
                .whenComplete((response, error) -> {
                    if (error != null) {
                        lastError = messageOf(error);
                    } else {
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
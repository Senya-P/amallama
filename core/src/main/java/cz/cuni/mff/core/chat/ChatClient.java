package cz.cuni.mff.core.chat;

import java.util.concurrent.CompletableFuture;

/**
 * Represents a client that can send chat requests and receive responses.
 */
public interface ChatClient {
    /**
     * Sends a chat request and returns a future that will complete with the chat response.
     *
     * @param request the chat request to send
     * @return a CompletableFuture that will complete with the chat response
     */
    CompletableFuture<ChatResponse> send(ChatRequest request);
}

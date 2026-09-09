package cz.cuni.mff.core.chat;

import java.util.List;

/**
 * Represents a request to the chat API.
 */
public record ChatRequest(String model, List<ChatMessage> messages) {}

package cz.cuni.mff.core.chat;

import java.util.List;

/**
 * Represents a request to the chat API.
 * @param model the model to use
 * @param messages the conversation history
 */
public record ChatRequest(String model, List<ChatMessage> messages) {}

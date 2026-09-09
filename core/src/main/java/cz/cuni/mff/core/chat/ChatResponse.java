package cz.cuni.mff.core.chat;

/**
 * Represents a response from the chat API.
 */
public record ChatResponse(
    String content,
    String finishReason,
    int promptTokens,
    int completionTokens
) {}

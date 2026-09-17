package cz.cuni.mff.core.chat;

/**
 * Represents a response from the chat API.
 * @param content the reply text
 * @param finishReason the reason the generation finished
 * @param promptTokens tokens used by the prompt
 * @param completionTokens tokens used by the completion
 */
public record ChatResponse(
    String content,
    String finishReason,
    int promptTokens,
    int completionTokens
) {}

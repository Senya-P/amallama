package cz.cuni.mff.core.chat;

/**
 * Represents a message in a chat conversation.
 * @param role The role of the sender, e.g. {@code user} or {@code assistant}
 * @param content The message text
 */
public record ChatMessage(String role, String content) {}

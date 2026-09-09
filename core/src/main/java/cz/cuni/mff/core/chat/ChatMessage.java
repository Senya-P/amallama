package cz.cuni.mff.core.chat;

/**
 * Represents a message in a chat conversation.
 */
public record ChatMessage(String role, String content) {}

package cz.cuni.mff.core.chat;

/**
 * Represents an exception that occurs during chat operations.
 */
public class ChatException extends RuntimeException {
    public ChatException(String message) { super(message); }
}
package cz.cuni.mff.core.chat;

/**
 * Represents an exception that occurs during chat operations.
 */
public class ChatException extends RuntimeException {
    /**
     * Creates an exception with the given message.
     * @param message the error message
     */
    public ChatException(String message) { super(message); }
}
package cz.cuni.mff.core.chat;

import cz.cuni.mff.core.AppException;

/**
 * Represents an exception that occurs during chat operations. The message is what the UI shows.
 */
public class ChatException extends AppException {
    /**
     * Creates an exception with the given message.
     * @param message the error message
     */
    public ChatException(String message) {
        super(message);
    }

    /**
     * Creates an exception with the given message and underlying cause.
     * @param message the error message
     * @param cause the technical detail, for the log only
     */
    public ChatException(String message, Throwable cause) {
        super(message, cause);
    }
}
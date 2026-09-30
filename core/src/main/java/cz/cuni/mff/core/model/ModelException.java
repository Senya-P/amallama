package cz.cuni.mff.core.model;

import cz.cuni.mff.core.AppException;

/**
 * Represents an exception that occurs during model load or selection. The message is what the UI shows.
 */
public class ModelException extends AppException {
    /**
     * Creates an exception with the given message.
     * @param message the error message
     */
    public ModelException(String message) {
        super(message);
    }

    /**
     * Creates an exception with the given message and underlying cause.
     * @param message the error message
     * @param cause the technical detail, for the log only
     */
    public ModelException(String message, Throwable cause) {
        super(message, cause);
    }
}

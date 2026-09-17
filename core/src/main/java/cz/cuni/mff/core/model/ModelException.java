package cz.cuni.mff.core.model;

/**
 * Represents an exception that occurs during model load or selection.
 */
public class ModelException extends RuntimeException {
    /**
     * Creates an exception with the given message.
     * @param message the error message
     */
    public ModelException(String message) {
        super(message);
    }
}

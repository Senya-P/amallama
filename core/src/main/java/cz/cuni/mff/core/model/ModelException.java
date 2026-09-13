package cz.cuni.mff.core.model;

/**
 * Represents an exception that occurs during model load or selection.
 */
public class ModelException extends RuntimeException {
    public ModelException(String message) {
        super(message);
    }
}

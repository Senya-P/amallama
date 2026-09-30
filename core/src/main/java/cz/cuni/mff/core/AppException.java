package cz.cuni.mff.core;

/**
 * Base for exeptions that are shown to the user; the cause carries the technical detail for the log only.
 */
public abstract class AppException extends RuntimeException {

    /**
     * Creates a failure with a user-facing message.
     * @param message the short user-friendly message.
     */
    protected AppException(String message) {
        super(message);
    }

    /**
     * Creates a failure with a user-facing message and the underlying cause.
     * @param message the short user-friendly message.
     * @param cause the technical detail
     */
    protected AppException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Extracts the user-facing message from a failure chain.
     * @param error the failure
     * @param fallback the generic message to use when the chain carries no {@link AppException}
     * @return the user-facing message, never {@code null}
     */
    public static String userMessage(Throwable error, String fallback) {
        for (Throwable t = error; t != null && t.getCause() != t; t = t.getCause()) {
            if (t instanceof AppException) {
                return t.getMessage();
            }
        }
        return fallback;
    }
}
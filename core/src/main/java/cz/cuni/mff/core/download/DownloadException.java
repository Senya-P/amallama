package cz.cuni.mff.core.download;

import cz.cuni.mff.core.AppException;

/**
 * Represents an exception that occurs during model download. The message is what the UI shows.
 */
public class DownloadException extends AppException {
    /**
     * Creates an exception with the given message.
     * @param message the error message
     */
    public DownloadException(String message) {
        super(message);
    }

    /**
     * Creates an exception with the given message and underlying cause.
     * @param message the error message
     * @param cause the technical detail, for the log only
     */
    public DownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
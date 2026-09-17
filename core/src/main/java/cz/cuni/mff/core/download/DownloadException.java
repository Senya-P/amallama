package cz.cuni.mff.core.download;

/**
 * Represents an exception that occurs during model download.
 */
public class DownloadException extends RuntimeException {
    /**
     * Creates an exception with the given message.
     * @param message the error message
     */
    public DownloadException(String message) {
        super(message);
    }
}
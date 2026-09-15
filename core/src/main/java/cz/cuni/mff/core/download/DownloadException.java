package cz.cuni.mff.core.download;

/**
 * Represents an exception that occurs during model download.
 */
public class DownloadException extends RuntimeException {
    public DownloadException(String message) {
        super(message);
    }
}
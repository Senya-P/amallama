package cz.cuni.mff.core.monitor;

/**
 *  Represents an exception that occurs during monitoring.
 */
public final class MonitoringException extends RuntimeException {
    public MonitoringException(String message) { super(message); }
}

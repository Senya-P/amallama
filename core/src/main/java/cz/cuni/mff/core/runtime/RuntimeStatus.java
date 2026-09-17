package cz.cuni.mff.core.runtime;

/**
 * Lifecycle states of the inference backend process.
 */
public enum RuntimeStatus {
    /** The backend is not running. */
    STOPPED,
    /** The backend is starting up. */
    STARTING,
    /** The backend is running and ready. */
    RUNNING,
    /** The backend is shutting down. */
    STOPPING,
    /** The backend failed to start or crashed. */
    FAILED
}

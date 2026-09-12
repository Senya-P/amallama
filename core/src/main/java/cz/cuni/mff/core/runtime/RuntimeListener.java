package cz.cuni.mff.core.runtime;

/**
 * Receives runtime status change notifications.
 */
public interface RuntimeListener {
    /**
     * Called when the runtime status changes.
     *
     * @param status The new runtime status
     */
    void onStatusChanged(RuntimeStatus status);
}

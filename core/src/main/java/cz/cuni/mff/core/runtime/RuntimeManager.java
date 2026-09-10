package cz.cuni.mff.core.runtime;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for managing the lifecycle of a llm runtime.
 */
public interface RuntimeManager {
    /**
     * Starts the runtime with the given configuration.
     * @param config The configuration for the runtime.
     * @return A CompletableFuture that completes with the runtime status when the start operation is finished.
     */
    CompletableFuture<RuntimeStatus> start(RuntimeConfig config);
    /**
     * Stops the runtime.
     * @return A CompletableFuture that completes when the stop operation is finished.
     */
    CompletableFuture<Void> stop();
    /**
     * Returns the current status of the runtime.
     * @return The current runtime status.
     */
    RuntimeStatus status();
}

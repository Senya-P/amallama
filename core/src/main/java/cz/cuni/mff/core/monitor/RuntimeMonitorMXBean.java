package cz.cuni.mff.core.monitor;

import cz.cuni.mff.core.runtime.RuntimeStatus;

/**
 * Standard MXBean interface for runtime monitoring. 
 */
public interface RuntimeMonitorMXBean {
    /**
     * Returns the runtime status.
     * @return Runtime status {@link RuntimeStatus}
     */
    RuntimeStatus getStatus();
    /**
     * Returns the loaded model name.
     * @return The loaded model name
     */
    String getModelName();
    /**
     * Returns the last runtime error.
     * @return The last runtime error captured.
     */
    String getLastError();
    /**
     * Returns the number of layers offloaded to the GPU.
     * @return The number of layers offloaded to GPU, if available.
     */
    Integer getLayersOffloaded();
    /**
     * Returns the total number of GPU layers.
     * @return The total number of GPU layers, if available.
     */
    Integer getLayersTotal();
    /**
     * Returns the memory used by the backend.
     * @return The memory used in bytes, if available.
     */
    Long getMemoryUsedBytes();
    /**
     * Returns the free memory available to the backend.
     * @return The free memory in bytes, if available.
     */
    Long getMemoryAvailableBytes();
    /**
     * Returns the context size of the model.
     * @return The context size of the model, if available.
     */
    Integer getContextSize();
    /**
     * Returns whether the model was offloaded to the GPU.
     * @return true if the model was offloaded to GPU, false otherwise.
     */
    boolean isOffloadedToGpu();
}

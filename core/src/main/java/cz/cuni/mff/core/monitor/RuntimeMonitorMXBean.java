package cz.cuni.mff.core.monitor;

import cz.cuni.mff.core.runtime.RuntimeStatus;

/**
 * Standard MXBean interface for runtime monitoring. 
 */
public interface RuntimeMonitorMXBean {
    /**
     * 
     * @return Runtime status {@link RuntimeStatus}
     */
    RuntimeStatus getStatus();
    /**
     * @return The loaded model name
     */
    String getModelName();
    /**
     * @return The last runtime error captured.
     */
    String getLastError();
    /**
     * @return The number of layers offloaded to GPU, if available.
     */
    Integer getLayersOffloaded();
    /**
     * @return The total number of GPU layers, if available.
     */
    Integer getLayersTotal();
    /**
     * @return The memory used in bytes, if available.
     */
    Long getMemoryUsedBytes();
    /**
     * @return The free memory in bytes, if available.
     */
    Long getMemoryAvailableBytes();
    /**
     * @return The context size of the model, if available.
     */
    Integer getContextSize();
    /**
     * @return true if the model was offloaded to GPU, false otherwise.
     */
    boolean isOffloadedToGpu();
}

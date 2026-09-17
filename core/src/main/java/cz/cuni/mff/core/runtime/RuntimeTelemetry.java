package cz.cuni.mff.core.runtime;

/**
 * What the running backend actually reported, parsed from its output. null if not reported.
 *
 * @param layersOffloaded layers offloaded to the GPU
 * @param layersTotal total layers that could be offloaded
 * @param memory the backend's pre-load memory projection
 * @param contextSize the loaded context size in tokens
 */
public record RuntimeTelemetry(
    Integer layersOffloaded,
    Integer layersTotal,
    Memory memory,
    Integer contextSize
) {
    
    /**
     * The backend's pre-load memory projection
     * @param usedBytes
     * @param availableBytes bytes reported available at that location
     */
    public record Memory(long usedBytes, long availableBytes) {
    }

    public boolean offloadedToGpu() {
        return layersOffloaded != null && layersOffloaded > 0;
    }
}

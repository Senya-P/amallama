package cz.cuni.mff.core.runtime;

import cz.cuni.mff.core.hw.GpuInfo;

/**
 * The reason why the runtime plan chose its processing unit.
 */
public sealed interface Reason {

    /**
     * A GPU was found and the model fits in its free memory.
     * @param gpu the GPU the model will be offloaded to
     */
    record FullOffload(GpuInfo gpu) implements Reason {}

    /** No GPU was detected. */
    record NoGpu() implements Reason {}

    /** A GPU was detected but its free memory is unknown. */
    record VramUnknown() implements Reason {}

    /** A GPU was detected but the model does not fit. */
    record Insufficient() implements Reason {}

    /** The model file size could not be read, so the fit could not be decided. */
    record ModelSizeUnknown() implements Reason {}
}

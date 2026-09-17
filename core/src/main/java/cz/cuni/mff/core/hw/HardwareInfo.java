package cz.cuni.mff.core.hw;

import java.util.List;

/**
 * Represents information about the hardware.
 *
 * @param cpu Information about the CPU
 * @param gpus List of GPU information
 */
public record HardwareInfo(CpuInfo cpu, List<GpuInfo> gpus) {

    public static final HardwareInfo EMPTY = new HardwareInfo(new CpuInfo(0, 0), List.of());

    /**
     * Picks the GPU with the most free VRAM.
     * @return the GPU with the most free VRAM, or {@code null} if there are no GPUs
     */
    public GpuInfo primaryGpu() {
        return gpus.stream()
            .max((a, b) -> Long.compare(a.freeVram(), b.freeVram()))
            .orElse(null);
    }
}

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

    public GpuInfo primaryGpu() {
        return gpus.stream()
            .max((a, b) -> Long.compare(a.freeVram(), b.freeVram()))
            .orElse(null);
    }
}

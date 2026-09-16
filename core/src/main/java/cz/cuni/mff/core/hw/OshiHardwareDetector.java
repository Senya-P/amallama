package cz.cuni.mff.core.hw;

import java.util.List;

import oshi.hardware.CentralProcessor;
import oshi.hardware.GpuStats;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.spi.SystemInfoFactory;
import oshi.spi.SystemInfoProvider;

/**
 * Implementation of {@link HardwareDetector} using OSHI library.
 */
public class OshiHardwareDetector implements HardwareDetector {

    private final SystemInfoProvider provider;

    /**
     * Creates a new instance of {@link OshiHardwareDetector} using the default system info provider.
     */
    public OshiHardwareDetector() {
        this(SystemInfoFactory.create());
    }

    public OshiHardwareDetector(SystemInfoProvider provider) {
        this.provider = provider;
    }

    @Override
    public HardwareInfo detect() {
        HardwareAbstractionLayer hal = provider.getHardware();
        CentralProcessor cpu = hal.getProcessor();
        int physical = cpu.getPhysicalProcessorCount();
        int logical = cpu.getLogicalProcessorCount();
        List<GpuInfo> gpus = hal.getGraphicsCards().stream().map(OshiHardwareDetector::toGpuInfo).toList();
        return new HardwareInfo(new CpuInfo(logical, physical), gpus);
    }

    /**
     * Converts an OSHI {@link GraphicsCard} to a {@link GpuInfo}.
     * If the VRAM used cannot be determined, the free VRAM is set to -1.
     * @param card The OSHI graphics card
     * @return The corresponding GPU information
     */
    private static GpuInfo toGpuInfo(GraphicsCard card) {
        long total = card.getVRam();
        long used;
        try (GpuStats stats = card.createStatsSession()) {
            used = stats.getVramUsed();
        }
        long free = used >= 0 ? Math.max(0L, total - used) : -1L;
        return new GpuInfo(card.getName(), total, free);
    }

}

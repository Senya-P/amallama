package cz.cuni.mff.core.runtime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import cz.cuni.mff.core.hw.CpuInfo;
import cz.cuni.mff.core.hw.GpuInfo;
import cz.cuni.mff.core.hw.HardwareDetector;
import cz.cuni.mff.core.hw.HardwareInfo;
import cz.cuni.mff.core.model.LocalModel;

/**
 * Generates a runtime configuration based on the hardware information.
 */
public class HwAwareRuntimeConfigGenerator implements RuntimeConfigGenerator {

    private static final int FULL_OFFLOAD = 999;
    private static final int NO_OFFLOAD = 0;
    private static final long VRAM_RESERVE_BYTES = 1024 * 1024 * 1024;

    private final HardwareDetector hardwareDetector;

    public HwAwareRuntimeConfigGenerator(HardwareDetector hardwareDetector) {
        this.hardwareDetector = hardwareDetector;
    }
    @Override
    public RuntimeConfig generate(Path backendBinary, LocalModel model) {
        HardwareInfo hw = hardwareDetector.detect();
        int threads = getThreads(hw.cpu());
        int gpuLayers = getGpuLayers(hw.primaryGpu(), model.path());
        Path modelPath = model.selfContained() ? null : model.path();
        return RuntimeConfig.of(backendBinary, modelPath, 0, threads, gpuLayers); // TODO: context detection
    }

    private int getThreads(CpuInfo cpu) {
        return cpu.physicalCores() > 0 ? cpu.physicalCores() : cpu.logicalCores();
    }
    
    private int getGpuLayers(GpuInfo gpu, Path modelPath) {
        if (gpu == null) {
            return NO_OFFLOAD;
        }
        long freeVram = gpu.freeVram();
        long size = getModelSize(modelPath);
        if (size <= 0 || freeVram - VRAM_RESERVE_BYTES < size) {
            return NO_OFFLOAD;
        }
        return FULL_OFFLOAD;
    }
    private static long getModelSize(Path modelPath) {
        try {
            return Files.size(modelPath);
        } catch (IOException e) {
            return -1;
        }
    }
}

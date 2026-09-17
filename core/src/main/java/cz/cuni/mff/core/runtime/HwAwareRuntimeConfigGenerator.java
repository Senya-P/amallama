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
 * Generates a runtime plan based on the hardware information.
 */
public class HwAwareRuntimeConfigGenerator implements RuntimeConfigGenerator {

    private static final int FULL_OFFLOAD = 999;
    private static final int NO_OFFLOAD = 0;
    private static final long VRAM_RESERVE_BYTES = 1024 * 1024 * 1024;

    private final HardwareDetector hardwareDetector;

    /**
     * Creates a generator using the given hardware detector.
     * @param hardwareDetector the detector used to probe the machine's hardware
     */
    public HwAwareRuntimeConfigGenerator(HardwareDetector hardwareDetector) {
        this.hardwareDetector = hardwareDetector;
    }

    @Override
    public RuntimePlan generate(Path backendBinary, LocalModel model) {
        HardwareInfo hw = hardwareDetector.detect();
        int threads = getThreads(hw.cpu());
        GpuInfo gpu = hw.primaryGpu();
        long modelSize = getModelSize(model.path());
        Path modelPath = model.selfContained() ? null : model.path();

        Reason reason = provideReason(gpu, modelSize);
        int gpuLayers = reason instanceof Reason.FullOffload ? FULL_OFFLOAD : NO_OFFLOAD;
        RuntimeConfig config = RuntimeConfig.of(backendBinary, modelPath, 0, threads, gpuLayers); // TODO: context detection

        return new RuntimePlan(config, reason);
    }

    private static Reason provideReason(GpuInfo gpu, long modelSize) {
        if (gpu == null) {
            return new Reason.NoGpu();
        }
        if (gpu.freeVram() < 0) {
            return new Reason.VramUnknown();
        }
        if (modelSize <= 0) {
            return new Reason.ModelSizeUnknown();
        }
        if (gpu.freeVram() - VRAM_RESERVE_BYTES < modelSize) {
            return new Reason.Insufficient();
        }
        return new Reason.FullOffload(gpu);
    }

    private int getThreads(CpuInfo cpu) {
        return cpu.physicalCores() > 0 ? cpu.physicalCores() : cpu.logicalCores();
    }

    private static long getModelSize(Path modelPath) {
        try {
            return Files.size(modelPath);
        } catch (IOException e) {
            return -1;
        }
    }
}

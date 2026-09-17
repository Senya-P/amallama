package cz.cuni.mff.core.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import cz.cuni.mff.core.hw.CpuInfo;
import cz.cuni.mff.core.hw.GpuInfo;
import cz.cuni.mff.core.hw.HardwareDetector;
import cz.cuni.mff.core.hw.HardwareInfo;
import cz.cuni.mff.core.model.LocalModel;

class HwAwareRuntimeConfigGeneratorTest {

    private static final long GIB = 1024L * 1024 * 1024;
    private static final int FULL_OFFLOAD = 999;
    private static final Path BINARY = Path.of("llamafile");

    @Test
    void cpuOnlyWhenNoGpuIsDetected(@TempDir Path dir) throws IOException {
        LocalModel model = model(dir, "tiny.gguf", 16);
        HwAwareRuntimeConfigGenerator generator = generator(
            new CpuInfo(8, 4)
        );

        RuntimePlan plan = generator.generate(BINARY, model);

        assertInstanceOf(Reason.NoGpu.class, plan.reason());
        assertEquals(0, plan.config().gpuLayers());
    }

    @Test
    void cpuOnlyWhenVramIsUnknown(@TempDir Path dir) throws IOException {
        LocalModel model = model(dir, "tiny.gguf", 16);
        HwAwareRuntimeConfigGenerator generator = generator(
            new CpuInfo(8, 4), 
            new GpuInfo("GPU", 8 * GIB, -1)
        );

        RuntimePlan plan = generator.generate(BINARY, model);

        assertInstanceOf(Reason.VramUnknown.class, plan.reason());
        assertEquals(0, plan.config().gpuLayers());
    }

    @Test
    void cpuOnlyWhenModelSizeIsUnreadable(@TempDir Path dir) {
        LocalModel missing = LocalModel.of(dir.resolve("missing.gguf"));
        HwAwareRuntimeConfigGenerator generator = generator(
            new CpuInfo(8, 4),
            new GpuInfo("GPU", 8 * GIB, 8 * GIB)
        );

        RuntimePlan plan = generator.generate(BINARY, missing);

        assertInstanceOf(Reason.ModelSizeUnknown.class, plan.reason());
        assertEquals(0, plan.config().gpuLayers());
    }

    @Test
    void fullyOffloadsWhenModelFitsInVram(@TempDir Path dir) throws IOException {
        LocalModel model = model(dir, "tiny.gguf", 1024);
        HwAwareRuntimeConfigGenerator generator = generator(
            new CpuInfo(8, 4),
            new GpuInfo("GPU", 8 * GIB, 8 * GIB)
        );

        RuntimePlan plan = generator.generate(BINARY, model);

        assertInstanceOf(Reason.FullOffload.class, plan.reason());
        assertEquals(FULL_OFFLOAD, plan.config().gpuLayers());
    }

    @Test
    void offloadsWhenModelExactlyFitsAfterReserve(@TempDir Path dir) throws IOException {
        LocalModel model = model(dir, "tiny.gguf", 1024);
        HwAwareRuntimeConfigGenerator generator = generator(
            new CpuInfo(8, 4),
            new GpuInfo("GPU", 8 * GIB, GIB + 1024)
        );

        RuntimePlan plan = generator.generate(BINARY, model);

        assertInstanceOf(Reason.FullOffload.class, plan.reason());
    }

    @Test
    void staysOnCpuWhenModelDoesNotFit(@TempDir Path dir) throws IOException {
        LocalModel model = model(dir, "tiny.gguf", 1024);
        HwAwareRuntimeConfigGenerator generator = generator(
            new CpuInfo(8, 4), 
            new GpuInfo("GPU", GIB, GIB)
        );

        RuntimePlan plan = generator.generate(BINARY, model);

        assertInstanceOf(Reason.Insufficient.class, plan.reason());
        assertEquals(0, plan.config().gpuLayers());
    }

    @Test
    void usesPhysicalCoresForThreads(@TempDir Path dir) throws IOException {
        LocalModel model = model(dir, "tiny.gguf", 16);

        RuntimePlan plan = generator(
            new CpuInfo(16, 6)
        ).generate(BINARY, model);

        assertEquals(6, plan.config().threads());
    }

    @Test
    void fallsBackToLogicalCoresWhenPhysicalIsUnknown(@TempDir Path dir) throws IOException {
        LocalModel model = model(dir, "tiny.gguf", 16);

        RuntimePlan plan = generator(
            new CpuInfo(16, 0)
        ).generate(BINARY, model);

        assertEquals(16, plan.config().threads());
    }

    @Test
    void passesSeparateModelFileToBackend(@TempDir Path dir) throws IOException {
        LocalModel model = model(dir, "tiny.gguf", 16);

        RuntimePlan plan = generator(
            new CpuInfo(4, 2)
        ).generate(BINARY, model);

        assertEquals(model.path(), plan.config().modelPath());
    }

    @Test
    void selfContainedModelHasNoSeparateModelFile(@TempDir Path dir) throws IOException {
        LocalModel model = model(dir, "all-in-one.llamafile", 16);

        RuntimePlan plan = generator(
            new CpuInfo(4, 2)
        ).generate(BINARY, model);

        assertNull(plan.config().modelPath());
    }

    @Test
    void picksGpuWithMostFreeVramWhenSeveralArePresent(@TempDir Path dir) throws IOException {
        LocalModel model = model(dir, "tiny.gguf", 1024);
        GpuInfo tooSmall = new GpuInfo("Small", GIB, GIB);
        GpuInfo fits = new GpuInfo("Large", 16 * GIB, 8 * GIB);

        RuntimePlan plan = generator(
            new CpuInfo(8, 4), 
            tooSmall, 
            fits
        ).generate(BINARY, model);

        Reason.FullOffload reason = assertInstanceOf(Reason.FullOffload.class, plan.reason());
        assertEquals(fits, reason.gpu());
        assertEquals(FULL_OFFLOAD, plan.config().gpuLayers());
    }

    private static HwAwareRuntimeConfigGenerator generator(CpuInfo cpu, GpuInfo... gpus) {
        HardwareDetector detector = () -> new HardwareInfo(cpu, List.of(gpus));
        return new HwAwareRuntimeConfigGenerator(detector);
    }

    private static LocalModel model(Path dir, String name, int size) throws IOException {
        Path path = dir.resolve(name);
        Files.write(path, new byte[size]);
        return LocalModel.of(path);
    }
}

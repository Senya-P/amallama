package cz.cuni.mff.core.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RuntimeLogTest {

    private static final long MIB = 1024L * 1024L;

    @Test
    void telemetryIsNullUntilSomethingIsParsed() {
        RuntimeLog log = new RuntimeLog(10);

        log.append("listening on http://127.0.0.1:8080");

        assertNull(log.telemetry());
    }

    @Test
    void parsesOffloadedLayers() {
        RuntimeLog log = new RuntimeLog(10);

        log.append("llama_model_load: offloaded 33/33 layers to GPU");

        RuntimeTelemetry telemetry = log.telemetry();
        assertEquals(33, telemetry.layersOffloaded());
        assertEquals(33, telemetry.layersTotal());
        assertTrue(telemetry.offloadedToGpu());
    }

    @Test
    void parsesMemoryProjectionAsBytes() {
        RuntimeLog log = new RuntimeLog(10);

        log.append("llama_model_load: projected to use 1234 MiB of device memory "
                + "vs. 4096 MiB of free device memory");

        RuntimeTelemetry.Memory memory = log.telemetry().memory();
        assertEquals(1234 * MIB, memory.usedBytes());
        assertEquals(4096 * MIB, memory.availableBytes());
    }

    @Test
    void parsesContextSize() {
        RuntimeLog log = new RuntimeLog(10);

        log.append("n_ctx_slot = 2048");

        assertEquals(2048, log.telemetry().contextSize());
    }

    @Test
    void aggregatesFieldsAcrossLines() {
        RuntimeLog log = new RuntimeLog(10);
        log.append("llama_model_load: offloaded 20/40 layers to GPU");
        log.append("llama_model_load: projected to use 500 MiB of host memory "
                + "vs. 2000 MiB of total host memory");
        log.append("n_ctx_slot = 2048");

        RuntimeTelemetry telemetry = log.telemetry();
        assertEquals(20, telemetry.layersOffloaded());
        assertEquals(40, telemetry.layersTotal());
        assertEquals(500 * MIB, telemetry.memory().usedBytes());
        assertEquals(2000 * MIB, telemetry.memory().availableBytes());
        assertEquals(2048, telemetry.contextSize());
    }

    @Test
    void laterReportsOverwriteEarlierOnes() {
        RuntimeLog log = new RuntimeLog(10);
        log.append("n_ctx_slot = 2048");
        log.append("n_ctx_slot = 4096");

        assertEquals(4096, log.telemetry().contextSize());
    }
}

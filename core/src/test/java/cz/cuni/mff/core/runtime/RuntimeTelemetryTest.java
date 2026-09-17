package cz.cuni.mff.core.runtime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RuntimeTelemetryTest {

    @Test
    void positiveLayerCountMeansOffloaded() {
        assertTrue(new RuntimeTelemetry(33, 33, null, null).offloadedToGpu());
    }

    @Test
    void zeroLayersMeansNotOffloaded() {
        assertFalse(new RuntimeTelemetry(0, 33, null, null).offloadedToGpu());
    }

    @Test
    void missingLayerReportMeansNotOffloaded() {
        assertFalse(new RuntimeTelemetry(null, null, null, null).offloadedToGpu());
    }
}

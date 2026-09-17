package cz.cuni.mff.core.hw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

class HardwareInfoTest {

    private static final CpuInfo CPU = new CpuInfo(8, 4);

    @Test
    void hasNoPrimaryGpuWithoutGpus() {
        assertNull(new HardwareInfo(CPU, List.of()).primaryGpu());
        assertNull(HardwareInfo.EMPTY.primaryGpu());
    }

    @Test
    void picksGpuWithMostFreeVram() {
        GpuInfo small = new GpuInfo("Small", 100, 10);
        GpuInfo large = new GpuInfo("Large", 100, 90);

        assertEquals(large, new HardwareInfo(CPU, List.of(small, large)).primaryGpu());
    }

    @Test
    void unknownFreeVramLosesToKnown() {
        GpuInfo unknown = new GpuInfo("Unknown", 100, -1);
        GpuInfo known = new GpuInfo("Known", 100, 0);

        assertEquals(known, new HardwareInfo(CPU, List.of(unknown, known)).primaryGpu());
    }
}

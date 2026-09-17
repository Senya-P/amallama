package cz.cuni.mff.core.download;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DownloadProgressTest {

    @Test
    void reportsHalfProgress() {
        DownloadProgress progress = new DownloadProgress("tiny.gguf", 50, 100);

        assertEquals(0.5, progress.fraction());
        assertEquals(50.0, progress.percentage());
    }

    @Test
    void reportsCompleteProgress() {
        DownloadProgress progress = new DownloadProgress("tiny.gguf", 100, 100);

        assertEquals(1.0, progress.fraction());
        assertEquals(100.0, progress.percentage());
    }

    @Test
    void reportsZeroWhenTotalIsUnknown() {
        DownloadProgress progress = new DownloadProgress("tiny.gguf", 42, 0);

        assertEquals(0.0, progress.fraction());
        assertEquals(0.0, progress.percentage());
    }
}

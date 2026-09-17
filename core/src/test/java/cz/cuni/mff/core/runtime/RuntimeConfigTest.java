package cz.cuni.mff.core.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class RuntimeConfigTest {

    private static final Path BINARY = Path.of("llamafile");
    private static final Path MODEL = Path.of("tiny.gguf");

    @Test
    void buildsFullBackendCommandLine() {
        RuntimeConfig config = new RuntimeConfig(BINARY, MODEL, "127.0.0.1", 8080, 4096, 8, 999);

        assertEquals(List.of(
                BINARY.toString(),
                "--server",
                "-c", "4096",
                "-t", "8",
                "-ngl", "999",
                "-lv", "4",
                "--host", "127.0.0.1",
                "--port", "8080",
                "-m", MODEL.toString()
        ), config.toCommandLine());
    }

    @Test
    void omitsModelFlagForSelfContainedBackend() {
        RuntimeConfig config = new RuntimeConfig(BINARY, null, "127.0.0.1", 8080, 0, 4, 0);

        List<String> args = config.toCommandLine();

        assertFalse(args.contains("-m"));
        assertEquals(BINARY.toString(), args.get(0));
    }

    @Test
    void ofUsesDefaultHostAndPort() {
        RuntimeConfig config = RuntimeConfig.of(BINARY, null, 2048, 6, 0);

        assertEquals(RuntimeConfig.DEFAULT_HOST, config.host());
        assertEquals(RuntimeConfig.DEFAULT_PORT, config.port());
        assertEquals(2048, config.contextSize());
        assertEquals(6, config.threads());
        assertEquals(0, config.gpuLayers());
    }
}

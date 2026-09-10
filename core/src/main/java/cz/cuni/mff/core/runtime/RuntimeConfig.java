package cz.cuni.mff.core.runtime;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Configuration for the runtime environment.
 * 
 * RuntimeConfig
 * @param backendBinary The llamafile executable
 * @param modelPath The GGUF model file (null if not specified; the model from the llamafile executable will be used)
 * @param host Server bind address
 * @param port Server listen port
 * @param contextSize Context window size. 0 means to use the model default
 * @param threads Number of CPU threads to use during generation
 * @param gpuLayers Number of GPU layers (0 = CPU only)
 * @param temperature The temperature for text generation
 * @param topK The top-k value for text generation
 * @param topP The top-p value for text generation
 * 
*/
public record RuntimeConfig(
    Path backendBinary,   // the llamafile executable
    Path modelPath,       // GGUF model file 
    String host,          // 127.0.0.1
    int port,             // 8080
    int contextSize,      // -c, tokens
    int threads,          // -t, CPU threads
    int gpuLayers        // -ngl, 0 = CPU only
) {
    public List<String> toCommandLine() { 
        List<String> args = new ArrayList<>(List.of(
            backendBinary.toString(),
            "--server",
            "-c", String.valueOf(contextSize),
            "-t", String.valueOf(threads),
            "-ngl", String.valueOf(gpuLayers),
            "--host", host,
            "--port", String.valueOf(port)
        ));
        if (modelPath != null) {
            args.addAll(List.of("-m", modelPath.toString()));
        }
        return args;
    }
    public static RuntimeConfig of(Path backendBinary) {
        return of(backendBinary, null);
    }

    public static RuntimeConfig of(Path backendBinary, Path modelPath) {
        return new RuntimeConfig(backendBinary, modelPath,
            "127.0.0.1", 8080, 0,
            Runtime.getRuntime().availableProcessors(), 0
        );
    }
}

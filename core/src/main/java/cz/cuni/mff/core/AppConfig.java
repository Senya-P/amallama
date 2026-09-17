package cz.cuni.mff.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import cz.cuni.mff.core.model.LocalModel;
import cz.cuni.mff.core.model.ModelException;

/**
 * Represents the application configuration, including selected model and runtime binary.
 * Owns the models directory and config.json.
 */
public class AppConfig {
    private static final Path AMALLAMA_PATH = Path.of(System.getProperty("user.home"), ".amallama");
    private static final Path MODELS_PATH = AMALLAMA_PATH.resolve("models");
    private static final Path CONFIG_PATH = AMALLAMA_PATH.resolve("config.json");

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private Config config;

    private record Config(String model, String runtimeBinary) {}

    /**
     * Loads the configuration from config.json, creating the models directory if needed.
     */
    public AppConfig() {
        try {
            Files.createDirectories(MODELS_PATH);
        } catch (IOException e) {
            throw new ModelException("Cannot create models directory: " + MODELS_PATH + " — " + e.getMessage());
        }
        if (Files.exists(CONFIG_PATH)) {
            try {
                config = MAPPER.readValue(CONFIG_PATH.toFile(), Config.class);
            } catch (IOException e) {
                throw new ModelException("Cannot read config file: " + CONFIG_PATH + " — " + e.getMessage());
            }
        } else {
            config = new Config(null, null);
        }
    }


    /**
     * Scans the models directory for model files.
     * @return the models found in the models directory, sorted by name
     */
    public List<LocalModel> getModels() {
        try (var files = Files.list(MODELS_PATH)) {
            return files
                    .filter(AppConfig::isModelFile)
                    .map(LocalModel::of)
                    .sorted(Comparator.comparing(LocalModel::name))
                    .toList();
        } catch (IOException e) {
            throw new ModelException("Cannot scan models directory: " + MODELS_PATH + " — " + e.getMessage());
        }
    }

    /**
     * Returns the persisted model selection.
     * @return the persisted model if it still exists on disk, or {@code null} if none is selected
     */
    public LocalModel model() {
        Path path = resolve(config.model());
        return path != null && isModelFile(path) ? LocalModel.of(path) : null;
    }

    /**
     * Returns the runtime binary to use.
     * @return the selected runtime binary, or an autodetected one if none is selected
     */
    public Path runtimeBinary() {
        Path selected = resolve(config.runtimeBinary());
        return selected != null ? selected : autodetectRuntime();
    }

    /**
     * Persists the given binary as the runtime to use.
     * @param binary the runtime binary, or {@code null} to clear the selection
     */
    public void selectRuntimeBinary(Path binary) {
        String value = binary == null ? null : binary.toAbsolutePath().normalize().toString();
        config = new Config(config.model(), value);
        save();
    }

    /**
     * Persists the given model as the selected one.
     * @param model the model to select
     */
    public void selectModel(LocalModel model) {
        config = new Config(model.name(), config.runtimeBinary());
        save();
    }

    /**
     * Returns the models directory.
     * @return the directory models are downloaded into and scanned from
     */
    public Path modelsDirectory() {
        return MODELS_PATH;
    }

    private void save() {
        try {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(CONFIG_PATH.toFile(), config);
        } catch (IOException e) {
            throw new ModelException("Cannot write config file: " + CONFIG_PATH + " — " + e.getMessage());
        }
    }

    private static boolean isModelFile(Path p) {
        String name = p.getFileName().toString().toLowerCase();
        return name.contains(".llamafile") || name.endsWith(".gguf");
    }

    private Path resolve(String value) {
        if (value == null) {
            return null;
        }
        Path path = Path.of(value);
        if (!path.isAbsolute()) {
            path = MODELS_PATH.resolve(path);
        }
        return Files.exists(path) ? path : null;
    }

    private Path autodetectRuntime() {
        String binaryName = isWindows() ? "llamafile.exe" : "llamafile";
        Path llamafileBinary = MODELS_PATH.resolve(binaryName);
        if (Files.exists(llamafileBinary)) {
            return llamafileBinary;
        }
        return null;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}

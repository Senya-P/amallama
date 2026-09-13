package cz.cuni.mff.core.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AppConfig {
    private static final Path AMALLAMA_PATH = Path.of(System.getProperty("user.home"), ".amallama");
    private static final Path MODELS_PATH = AMALLAMA_PATH.resolve("models");
    private static final Path CONFIG_PATH = AMALLAMA_PATH.resolve("config.json");

    private final ObjectMapper mapper = new ObjectMapper();
    private Config config;

    private record Config(String model, String runtimeBinary) {}

    public AppConfig() {
        try {
            Files.createDirectories(MODELS_PATH);
        } catch (IOException e) {
            throw new ModelException("Cannot create models directory: " + MODELS_PATH + " — " + e.getMessage());
        }
        if (Files.exists(CONFIG_PATH)) {
            try {
                config = mapper.readValue(CONFIG_PATH.toFile(), Config.class);
            } catch (IOException e) {
                throw new ModelException("Cannot read config file: " + CONFIG_PATH + " — " + e.getMessage());
            }
        } else {
            config = new Config(null, null);
        }
    }


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

    public LocalModel model() {
        Path path = resolve(config.model());
        return path != null && isModelFile(path) ? LocalModel.of(path) : null;
    }

    public Path runtimeBinary() {
        Path selected = resolve(config.runtimeBinary());
        return selected != null ? selected : autodetectRuntime();
    }

    public void selectRuntimeBinary(Path binary) {
        String value = binary == null ? null : binary.toAbsolutePath().normalize().toString();
        config = new Config(config.model(), value);
        save();
    }

    public void selectModel(LocalModel model) {
        config = new Config(model.name(), config.runtimeBinary());
        save();
    }

    private void save() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(CONFIG_PATH.toFile(), config);
        } catch (IOException e) {
            throw new ModelException("Cannot write config file: " + CONFIG_PATH + " — " + e.getMessage());
        }
    }

    private static boolean isModelFile(Path p) {
        String name = p.getFileName().toString().toLowerCase();
        return name.endsWith(".llamafile") || name.endsWith(".gguf"); // replace with ???
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
        Path llamafileBinary = MODELS_PATH.resolve("llamafile");
        if (Files.exists(llamafileBinary)) {
            return llamafileBinary;
        }
        return null;
    }
}
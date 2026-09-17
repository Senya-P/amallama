package cz.cuni.mff.core.model;

import java.nio.file.Path;

/**
 * Represents a local model.
 *
 * @param name The model display name
 * @param path The absolute path to the model file
 * @param selfContained Whether the model has a runtime (in case of a llamafile)
 */
public record LocalModel(
    String name,
    Path path,
    boolean selfContained
) {
    /**
     * Creates a model from a file path, detecting whether it is self-contained.
     * @param path the model file
     * @return the model
     */
    public static LocalModel of(Path path) { 
        String name = path.getFileName().toString().toLowerCase();
        boolean selfContained = name.contains(".llamafile");
        return new LocalModel(
            path.getFileName().toString(), 
            path, 
            selfContained
        );
    }
}

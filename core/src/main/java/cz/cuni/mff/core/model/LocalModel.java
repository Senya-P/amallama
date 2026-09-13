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
    public static LocalModel of(Path path) { 
        boolean selfContained = path.getFileName().toString().endsWith(".llamafile");
        return new LocalModel(
            path.getFileName().toString(), 
            path, 
            selfContained
        );
    }
}

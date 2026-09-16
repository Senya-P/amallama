package cz.cuni.mff.core.runtime;

import java.nio.file.Path;

import cz.cuni.mff.core.model.LocalModel;

/**
 * Generates a runtime configuration for a given backend binary, model, and detected hardware information.
 */
public interface RuntimeConfigGenerator {
    /**
     * Generates a runtime configuration.
     *
     * @param backendBinary The path to the backend binary
     * @param model The local model to be used
     * @param hw The detected hardware information
     * @return A {@link RuntimeConfig} instance representing the generated configuration
     */
    RuntimeConfig generate(Path backendBinary, LocalModel model);
}

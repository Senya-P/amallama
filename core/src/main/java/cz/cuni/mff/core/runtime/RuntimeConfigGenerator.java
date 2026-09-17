package cz.cuni.mff.core.runtime;

import java.nio.file.Path;

import cz.cuni.mff.core.model.LocalModel;

/**
 * Generates a runtime plan for a given backend binary and model.
 */
public interface RuntimeConfigGenerator {
    /**
     * Generates a runtime plan.
     * @param backendBinary The path to the backend binary
     * @param model tThe local model to be used
     * @return the generated {@link RuntimePlan}
     */
    RuntimePlan generate(Path backendBinary, LocalModel model);
}

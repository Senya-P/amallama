package cz.cuni.mff.core.model;

import java.nio.file.Path;

import cz.cuni.mff.core.AppConfig;
import cz.cuni.mff.core.runtime.RuntimeConfigGenerator;
import cz.cuni.mff.core.runtime.RuntimePlan;

/**
 * Manages the selection of models.
 */
public final class ModelManager {
    private final AppConfig config;
    private final RuntimeConfigGenerator generator;

    /**
     * Creates a model manager.
     * @param config the application configuration
     * @param generator the runtime plan generator
     */
    public ModelManager(AppConfig config, RuntimeConfigGenerator generator) {
        this.config = config;
        this.generator = generator;
    }

    /**
     * Selects the model to start with: the persisted selection if it exists,
     * otherwise the first self-contained model found on disk.
     * @return the plan for the selected model, or {@code null} if no model is available
     */
    public RuntimePlan selectInitial() {
        try {
            LocalModel persisted = config.model();
            if (persisted != null) {
                return select(persisted);
            }
            return config.getModels().stream()
                    .filter(LocalModel::selfContained)
                    .findFirst()
                    .map(this::select)
                    .orElse(null);
        } catch (ModelException e) {
            return null;
        }
    }

    /**
     * Selects the given model and generates a runtime plan for it.
     * @param model the model to select
     * @return the generated runtime plan
     * @throws ModelException if the model needs a runtime binary and none is available
     */
    public RuntimePlan select(LocalModel model) {
        if (!model.selfContained() && config.runtimeBinary() == null) {
            throw new ModelException("No llamafile found to serve this model: " + model.name());
        }
        config.selectModel(model);  
        Path binary = model.selfContained() ? model.path() : config.runtimeBinary();
        return generator.generate(binary, model);
    }
}

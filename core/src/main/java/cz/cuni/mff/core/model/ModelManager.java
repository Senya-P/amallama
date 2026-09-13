package cz.cuni.mff.core.model;

import cz.cuni.mff.core.runtime.RuntimeConfig;

/**
 * Manages the selection of models.
 */
public final class ModelManager {
    private final AppConfig config;

    public ModelManager(AppConfig config) {
        this.config = config;
    }

    public ModelManager() {
        this(new AppConfig());
    }

    public RuntimeConfig selectInitial() {
        LocalModel persisted = config.model();
        if (persisted != null) {
            return select(persisted);
        }
        LocalModel auto = config.getModels().stream()
                .filter(LocalModel::selfContained)
                .findFirst()
                .orElse(null);
        return auto == null ? null : select(auto);
    }

    public RuntimeConfig select(LocalModel model) {
        if (!model.selfContained() && config.runtimeBinary() == null) {
            throw new ModelException("No llamafile found to serve this model: " + model.name());
        }
        config.selectModel(model);  
        return model.selfContained() ? RuntimeConfig.of(model.path()) 
            : RuntimeConfig.of(config.runtimeBinary(), model.path());
    }
}

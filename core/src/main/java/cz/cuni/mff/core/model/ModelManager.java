package cz.cuni.mff.core.model;

import java.nio.file.Path;

import cz.cuni.mff.core.AppConfig;
import cz.cuni.mff.core.runtime.RuntimeConfig;
import cz.cuni.mff.core.runtime.RuntimeConfigGenerator;

/**
 * Manages the selection of models.
 */
public final class ModelManager {
    private final AppConfig config;
    private final RuntimeConfigGenerator generator;

    public ModelManager(AppConfig config, RuntimeConfigGenerator generator) {
        this.config = config;
        this.generator = generator;
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
        Path binary = model.selfContained() ? model.path() : config.runtimeBinary();
        return generator.generate(binary, model);
    }
}

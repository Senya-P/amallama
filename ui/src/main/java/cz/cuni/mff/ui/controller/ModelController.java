package cz.cuni.mff.ui.controller;

import java.io.File;
import java.nio.file.Path;

import cz.cuni.mff.core.Session;
import cz.cuni.mff.core.model.LocalModel;
import cz.cuni.mff.core.model.ModelException;
import cz.cuni.mff.core.model.ModelManager;
import cz.cuni.mff.core.model.AppConfig;
import cz.cuni.mff.core.runtime.RuntimeStatus;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.stage.FileChooser;

public class ModelController {
    private final AppConfig config;
    private final ModelManager models;
    private final Session session;
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private Runnable onModelLoaded = () -> {};

    @FXML private ListView<LocalModel> modelList;
    @FXML private Label errorLabel;
    @FXML private Button loadButton;
    @FXML private Label runtimeLabel;

    public ModelController(AppConfig config, ModelManager models, Session session) {
        this.config = config;
        this.models = models;
        this.session = session;
    }

    public void setOnModelLoaded(Runnable onModelLoaded) {
        this.onModelLoaded = onModelLoaded;
    }

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        modelList.setPlaceholder(new Label("No models found in the models directory"));
        modelList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(LocalModel model, boolean empty) {
                super.updateItem(model, empty);
                setText(model == null || empty ? null : model.name());
            }
        });
        loading.set(isLoading(session.status()));
        loadButton.disableProperty().bind(
                modelList.getSelectionModel().selectedItemProperty().isNull().or(loading)
        );
        session.addListener(status -> Platform.runLater(() -> loading.set(isLoading(status))));
        refreshModelsList();
        refreshRuntime();
    }

    private static boolean isLoading(RuntimeStatus status) {
        return status == RuntimeStatus.STARTING || status == RuntimeStatus.STOPPING;
    }

    @FXML
    private void onRefresh() {
        refreshModelsList();
    }

    @FXML
    private void onLoad() {
        LocalModel model = modelList.getSelectionModel().getSelectedItem();
        if (model == null) {
            return;
        }
        try {
            session.restart(models.select(model));
            onModelLoaded.run();
            errorLabel.setVisible(false);
        } catch (ModelException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onBrowseRuntime() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select llamafile runtime binary");
        File file = chooser.showOpenDialog(runtimeLabel.getScene().getWindow());
        if (file != null) {
            config.selectRuntimeBinary(file.toPath());
            refreshRuntime();
        }
    }

    private void refreshRuntime() {
        Path runtime = config.runtimeBinary();
        runtimeLabel.setText("Runtime " + (runtime == null ? "—" : runtime.getFileName()));
        runtimeLabel.setTooltip(runtime == null ? null : new Tooltip(runtime.toString()));
    }

    private void refreshModelsList() {
        modelList.getItems().setAll(config.getModels());
        LocalModel selected = config.model();
        if (selected != null) {
            modelList.getSelectionModel().select(selected);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
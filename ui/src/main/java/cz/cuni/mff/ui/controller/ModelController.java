package cz.cuni.mff.ui.controller;

import java.io.File;
import java.nio.file.Path;

import cz.cuni.mff.core.AppConfig;
import cz.cuni.mff.core.Session;
import cz.cuni.mff.core.download.ModelDownloader;
import cz.cuni.mff.core.model.LocalModel;
import cz.cuni.mff.core.model.ModelManager;
import cz.cuni.mff.core.runtime.RuntimePlan;
import cz.cuni.mff.core.runtime.RuntimeStatus;
import cz.cuni.mff.core.runtime.RuntimeTelemetry;
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

/**
 * Controller for the model selection view.
 * Handles model loading, refreshing, and runtime selection.
 */
public class ModelController {
    private final AppConfig config;
    private final ModelManager models;
    private final Session session;
    private final ModelDownloader downloader;
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private Runnable onModelLoaded = () -> {};

    @FXML private ListView<LocalModel> modelList;
    @FXML private Button loadButton;
    @FXML private Label runtimeLabel;
    @FXML private Label deviceLabel;
    @FXML private Label usageLabel;
    @FXML private DownloadController downloadController;

    /**
     * @param config the application configuration
     * @param models the model manager
     * @param session the backend session
     * @param downloader the model downloader
     */
    public ModelController(AppConfig config, ModelManager models, Session session, ModelDownloader downloader) {
        this.config = config;
        this.models = models;
        this.session = session;
        this.downloader = downloader;
    }

    /**
     * Sets the callback invoked after a model is loaded.
     * @param onModelLoaded the callback
     */
    public void setOnModelLoaded(Runnable onModelLoaded) {
        this.onModelLoaded = onModelLoaded;
    }

    @FXML
    private void initialize() {
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
        session.addListener(status -> Platform.runLater(() -> {
            loading.set(isLoading(status));
            if (status == RuntimeStatus.RUNNING) {
                showRuntimeTelemetry();
            }
        }));
        downloadController.setDownloader(downloader);
        downloadController.setOnDownloaded(this::onDownloaded);
        refreshModelsList();
        refreshRuntime();
        showRuntimePlan(session.plan());
        if (session.status() == RuntimeStatus.RUNNING) {
            showRuntimeTelemetry();
        }
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
        RuntimePlan selected = models.select(model);
        session.restart(selected);
        showRuntimePlan(selected);
        onModelLoaded.run();
    }

    private void showRuntimePlan(RuntimePlan plan) {
        deviceLabel.setText(RuntimePlanText.deviceBlock(plan));
        usageLabel.setText("");
    }

    private void showRuntimeTelemetry() {
        RuntimeTelemetry telemetry = session.telemetry();
        usageLabel.setText(RuntimePlanText.usageLine(telemetry));
    }

    private void onDownloaded(Path target) {
        refreshModelsList();
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
}
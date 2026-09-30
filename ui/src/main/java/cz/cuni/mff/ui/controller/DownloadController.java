package cz.cuni.mff.ui.controller;

import java.nio.file.Path;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import cz.cuni.mff.core.AppException;
import cz.cuni.mff.core.download.DownloadProgress;
import cz.cuni.mff.core.download.ModelDownloader;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

/**
 * Download row of the Model view: direct-link download with progress.
 */
public class DownloadController {
    private final BooleanProperty downloading = new SimpleBooleanProperty(false);
    private ModelDownloader downloader;
    private Consumer<Path> onDownloaded = p -> {};
    private CompletableFuture<Path> currentDownload;
    private AtomicLong lastUpdate = new AtomicLong();

    @FXML private TextField urlField;
    @FXML private Button downloadButton;
    @FXML private Button cancelButton;
    @FXML private ProgressBar progressBar;
    @FXML private Label downloadStatus;

    /**
     * Sets the downloader used for direct-link downloads.
     * @param downloader the downloader
     */
    public void setDownloader(ModelDownloader downloader) {
        this.downloader = downloader;
    }

    /**
     * Sets the callback invoked when a download completes.
     * @param onDownloaded the callback receiving the downloaded file path
     */
    public void setOnDownloaded(Consumer<Path> onDownloaded) {
        this.onDownloaded = onDownloaded;
    }

    @FXML
    private void initialize() {
        downloadButton.disableProperty().bind(
            urlField.textProperty().isEmpty().or(downloading)
        );
        cancelButton.disableProperty().bind(downloading.not());
        // download and cancel alternate
        downloadButton.visibleProperty().bind(downloading.not());
        downloadButton.managedProperty().bind(downloading.not());
        cancelButton.visibleProperty().bind(downloading);
        cancelButton.managedProperty().bind(downloading);
        progressBar.visibleProperty().bind(downloading);
        progressBar.managedProperty().bind(downloading);
    }

    @FXML
    private void onDownload() {
        String url = urlField.getText().trim();
        if (url.isEmpty()) {
            return;
        }
        downloading.set(true);
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        downloadStatus.getStyleClass().remove("error-text");
        downloadStatus.setText("Downloading...");
        currentDownload = downloader.download(url, p -> {
            long now = System.nanoTime();
            if (now - lastUpdate.get() >= 100000000L) {
                lastUpdate.set(now);
                Platform.runLater(() -> updateProgress(p));
            }
        });
        currentDownload.whenComplete((target, error) -> 
            Platform.runLater(() -> finishDownload(target, error))
        );
    }

    @FXML
    private void onCancel() {
        if (currentDownload != null) {
            currentDownload.cancel(true);
        }
    }

    private void updateProgress(DownloadProgress progress) {
        progressBar.setProgress(progress.totalBytes() > 0
            ? progress.fraction()
            : ProgressIndicator.INDETERMINATE_PROGRESS
        );
        double downloadedMib = progress.downloadedBytes() / (1024.0 * 1024.0);
        double totalMib = progress.totalBytes() / (1024.0 * 1024.0);
        downloadStatus.setText(String.format("%.2f", downloadedMib) + " MiB / " 
            + (progress.totalBytes() <= 0 ? "?" : (String.format("%.2f", totalMib) 
            + " MiB (" + String.format("%.2f", progress.percentage()) + "%)"))
        );
    }

    private void finishDownload(Path target, Throwable error) {
        downloadStatus.getStyleClass().remove("error-text");
        downloading.set(false);
        progressBar.setProgress(0);
        if (error != null) {
            if (error instanceof CancellationException) {
                downloadStatus.setText("Download cancelled");
            } else {
                downloadStatus.getStyleClass().add("error-text");
                downloadStatus.setText(AppException.userMessage(error, "Download failed"));
            }
            return;
        }
        urlField.clear();
        downloadStatus.setText("Downloaded " + target.getFileName());
        onDownloaded.accept(target);
    }
}
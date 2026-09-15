package cz.cuni.mff.core.download;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Represents a model downloader that can download models from a given URL.
 */
public interface ModelDownloader {
    /**
     * Downloads a model from the specified URL and reports progress through the provided callback.
     *
     * @param url The URL of the model to download
     * @param onProgress A callback that receives download progress updates
     * @return a CompletableFuture that will complete with the path to the downloaded model file
     */
    CompletableFuture<Path> download(String url, Consumer<DownloadProgress> onProgress);
}

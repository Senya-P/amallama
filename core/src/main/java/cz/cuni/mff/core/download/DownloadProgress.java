package cz.cuni.mff.core.download;

/**
 * 
 * Represents the progress of a download operation.
 * @param fileName The name of the file being downloaded.
 * @param downloadedBytes The number of bytes that have been downloaded so far.
 * @param totalBytes The total number of bytes to be downloaded. If unknown, this value can be set to 0.
 */
public record DownloadProgress(String fileName, long downloadedBytes, long totalBytes) {
    /** 
     * @return Progress as a percentage. 
     * */
    public double percentage() {
        return totalBytes > 0 ? ((double) downloadedBytes / totalBytes * 100) : 0.0;
    }

    /** 
     * @return Progress as a fraction (0–1). 
     * */
    public double fraction() {
        return totalBytes > 0 ? ((double) downloadedBytes / totalBytes) : 0.0;
    }
}

package cz.cuni.mff.core.hw;

/**
 * Represents information about the GPU.
 *
 * @param name Name of the GPU
 * @param vramTotalBytes Total VRAM in bytes
 * @param vramFreeBytes Free VRAM in bytes, or -1 if unknown
 */
public record GpuInfo(String name, long vramTotalBytes, long vramFreeBytes) {

    public long freeVram() {
        return vramFreeBytes >= 0 ? vramFreeBytes : -1;
    }
}



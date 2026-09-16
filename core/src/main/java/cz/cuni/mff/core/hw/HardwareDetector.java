package cz.cuni.mff.core.hw;

/**
 * Interface for detecting hardware information.
 */
public interface HardwareDetector {
    /**
     * Detects the hardware information of the current system.
     *
     * @return The detected hardware information
     */
    HardwareInfo detect();
}

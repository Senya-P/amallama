package cz.cuni.mff.core.hw;

/**
 * Represents information about the CPU.
 *
 * @param logicalCores Number of logical cores
 * @param physicalCores Number of physical cores
 */
public record CpuInfo(int logicalCores, int physicalCores) {}

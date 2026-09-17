package cz.cuni.mff.ui.controller;

import java.util.ArrayList;
import java.util.List;

import cz.cuni.mff.core.hw.GpuInfo;
import cz.cuni.mff.core.runtime.Reason;
import cz.cuni.mff.core.runtime.RuntimePlan;
import cz.cuni.mff.core.runtime.RuntimeTelemetry;

/**
 * Maps the structured {@link RuntimePlan} and {@link RuntimeTelemetry} to display text.
 */
final class RuntimePlanText {

    private static final long MiB = 1024L * 1024L;

    private RuntimePlanText() {
    }

    /**
     * Chosen device and why, plus the GPU it will offload to in case of running on GPU. 
     */
    static String deviceBlock(RuntimePlan plan) {
        if (plan == null) {
            return "No runtime plan yet";
        }
        return switch (plan.reason()) {
            case Reason.FullOffload(GpuInfo gpu) -> "Running on: GPU\n" + gpu.name() + " · " + gpuMemory(gpu);
            case Reason.NoGpu _ -> "Running on: CPU · no GPU detected";
            case Reason.VramUnknown _ -> "Running on: CPU · GPU memory unknown";
            case Reason.Insufficient _ -> "Running on: CPU · model does not fit in GPU memory";
            case Reason.ModelSizeUnknown _ -> "Running on: CPU · model size could not be read";
        };
    }

    /** Observed memory usage and context, shown once the runtime reports them. */
    static String usageLine(RuntimeTelemetry telemetry) {
        if (telemetry == null) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        if (telemetry.memory() != null) {
            RuntimeTelemetry.Memory m = telemetry.memory();
            String kind = telemetry.offloadedToGpu() ? "VRAM" : "RAM";
            parts.add(String.format("%d / %d MiB %s", m.usedBytes() / MiB, m.availableBytes() / MiB, kind));
        }
        if (telemetry.contextSize() != null) {
            parts.add(telemetry.contextSize() + " token context");
        }
        return String.join(" · ", parts);
    }

    private static String gpuMemory(GpuInfo gpu) {
        return gpu.freeVram() < 0 ? "memory unknown" : (gpu.freeVram() / MiB) + " MiB free";
    }
}

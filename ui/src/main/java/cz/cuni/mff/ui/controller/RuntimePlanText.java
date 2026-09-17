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
        StringBuilder sb = new StringBuilder();
        boolean isGpu = plan.reason() instanceof Reason.FullOffload;
        sb.append("Running on: ").append(isGpu ? "GPU" : "CPU");
        if (!isGpu) {
            sb.append(" — ").append(reason(plan.reason()));
        }
        if (plan.reason() instanceof Reason.FullOffload full && full.gpu() != null) {
            GpuInfo gpu = full.gpu();
            sb.append("\n").append(gpu.name()).append(" · ").append(gpuMemory(gpu));
        }
        return sb.toString();
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

    private static String reason(Reason reason) {
        return switch (reason) {
            case Reason.FullOffload _ -> "fits in device memory";
            case Reason.NoGpu _ -> "no GPU detected";
            case Reason.VramUnknown _ -> "GPU memory unknown";
            case Reason.Insufficient _ -> "model does not fit in GPU memory";
            case Reason.ModelSizeUnknown _ -> "model size could not be read";
        };
    }
}

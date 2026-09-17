package cz.cuni.mff.core.runtime;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Log of the runtime process's output lines.
 * Each telemetry field is parsed independently and stays {@code null} until the backend actually reports it.
 */
public final class RuntimeLog {

    private static final Pattern OFFLOADED = Pattern.compile("offloaded (\\d+)/(\\d+) layers to GPU");
    private static final Pattern MEMORY = Pattern.compile("projected to use (\\d+) MiB of (host|device) memory"
                    + " vs\\. (\\d+) MiB of (?:free |total )?(host|device) memory");
    private static final Pattern CTX = Pattern.compile("n_ctx_slot = (\\d+)");

    private static final long MIB = 1024L * 1024L;

    private final int maxLines;
    private final ArrayDeque<String> lines = new ArrayDeque<>();

    private Integer layersOffloaded;
    private Integer layersTotal;
    private RuntimeTelemetry.Memory memory;
    private Integer contextSize;
    private boolean seen;

    public RuntimeLog(int maxLines) {
        this.maxLines = maxLines;
    }

    synchronized void append(String line) {
        if (lines.size() == maxLines) {
            lines.removeFirst();
        }
        lines.addLast(line);
        parse(line);
    }

    public synchronized List<String> tail(int n) {
        List<String> all = new ArrayList<>(lines);
        int from = Math.max(0, all.size() - n);
        return List.copyOf(all.subList(from, all.size()));
    }

    public synchronized RuntimeTelemetry telemetry() {
        return seen
                ? new RuntimeTelemetry(layersOffloaded, layersTotal, memory, contextSize)
                : null;
    }

    private void parse(String line) {
        Matcher m = OFFLOADED.matcher(line);
        if (m.find()) {
            layersOffloaded = Integer.parseInt(m.group(1));
            layersTotal = Integer.parseInt(m.group(2));
            seen = true;
        }
        m = MEMORY.matcher(line);
        if (m.find()) {
            memory = new RuntimeTelemetry.Memory(
                    Long.parseLong(m.group(1)) * MIB,
                    Long.parseLong(m.group(3)) * MIB);
            seen = true;
        }
        m = CTX.matcher(line);
        if (m.find()) {
            contextSize = Integer.parseInt(m.group(1));
            seen = true;
        }
    }
}

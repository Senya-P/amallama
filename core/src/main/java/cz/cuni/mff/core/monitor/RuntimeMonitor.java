package cz.cuni.mff.core.monitor;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

import cz.cuni.mff.core.Session;
import cz.cuni.mff.core.runtime.RuntimeStatus;
import cz.cuni.mff.core.runtime.RuntimeTelemetry;

/**
 * The facade over the runtime's status and telemetry.
 * Attributes are read live from {@link Session}.
 */
public final class RuntimeMonitor extends NotificationBroadcasterSupport implements RuntimeMonitorMXBean 
{

    static final String STATUS_NOTIFICATION = "cz.cuni.mff.amallama.runtime.status";
    private final Session session;

    /**
     * Creates a monitor exposing the given session.
     * @param session the session whose status and telemetry are exposed
     */
    public RuntimeMonitor(Session session) {
        this.session = session;
        session.addListener(this::onStatusChanged);
    }

    @Override
    public RuntimeStatus getStatus() {
        return session.status();
    }

    @Override
    public String getModelName() {
        return session.modelName();
    }

    @Override
    public String getLastError() {
        return session.lastError();
    }

    @Override
    public Integer getLayersOffloaded() {
        RuntimeTelemetry t = session.telemetry();
        return t == null ? null : t.layersOffloaded();
    }

    @Override
    public Integer getLayersTotal() {
        RuntimeTelemetry t = session.telemetry();
        return t == null ? null : t.layersTotal();
    }

    @Override
    public Long getMemoryUsedBytes() {
        RuntimeTelemetry t = session.telemetry();
        RuntimeTelemetry.Memory m = t == null ? null : t.memory();
        return m == null ? null : m.usedBytes();
    }

    @Override
    public Long getMemoryAvailableBytes() {
        RuntimeTelemetry t = session.telemetry();
        RuntimeTelemetry.Memory m = t == null ? null : t.memory();
        return m == null ? null : m.availableBytes();
    }

    @Override
    public Integer getContextSize() {
        RuntimeTelemetry t = session.telemetry();
        return t == null ? null : t.contextSize();
    }

    @Override
    public boolean isOffloadedToGpu() {
        RuntimeTelemetry t = session.telemetry();
        return t != null && t.offloadedToGpu();
    }

    private void onStatusChanged(RuntimeStatus status) {
        sendNotification(new Notification(STATUS_NOTIFICATION, this, 0, status.name()));
    }
}

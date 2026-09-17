package cz.cuni.mff.core.monitor;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.logging.Logger;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Registers the {@link RuntimeMonitorMXBean} on the platform MBean server and exposes it through a RMI connector.
 */
public final class MonitoringAgent {
    private static final Logger LOGGER = Logger.getLogger(MonitoringAgent.class.getName());
    private static final int DEFAULT_PORT = 9999;
    private static final String URL = "service:jmx:rmi://127.0.0.1:%d";
    private static final String OBJECT_NAME = "cz.cuni.mff.amallama:type=Runtime";

    private final RuntimeMonitorMXBean monitor;
    private final int port;
    private final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

    private JMXConnectorServer server;
    private ObjectName name;

    /**
     * Creates an agent publishing the given MXBean on the default port.
     * @param monitor The MXBean to publish
     */
    public MonitoringAgent(RuntimeMonitorMXBean monitor) {
        this(monitor, DEFAULT_PORT);
    }

    /**
     * Creates an agent publishing the given MXBean on the given port.
     * @param monitor The MXBean to publish
     * @param port The port the connector listens on
     */
    public MonitoringAgent(RuntimeMonitorMXBean monitor, int port) {
        this.monitor = monitor;
        this.port = port;
    }

    /**
     * Starts the connector.
     *
     * @throws IOException if the connector cannot be created or started
     * @throws JMException if the ObjectName is invalid or the MBean cannot be registered
     */
    public void start() throws IOException, JMException {
        System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        System.setProperty("java.rmi.server.randomIDs", "true");
        name = new ObjectName(OBJECT_NAME);
        mbs.registerMBean(monitor, name);
        server = JMXConnectorServerFactory.newJMXConnectorServer(
            new JMXServiceURL(String.format(URL, port)), null, mbs
        );
        server.start();
        LOGGER.info("Monitoring: " + server.getAddress());
    }

    /**
     * Starts the connector. Failures are logged but do not propagate, so
     * monitoring problems never take down the application.
     */
    public void tryStart() {
        try { start(); }
        catch (IOException | JMException e) {
            LOGGER.warning("Cannot start JMX runtime monitoring: " + e);
        }
    }

    /**
     * Stops the connector and unregisters the MBean.
     *
     * @throws IOException if the connector cannot be closed
     * @throws MBeanRegistrationException if the MBean cannot be unregistered
     */
    public void stop() throws IOException, MBeanRegistrationException {
        if (server != null) {
            server.stop();
            server = null;
        }
        if (name != null) {
            try {
                mbs.unregisterMBean(name);
            } catch (InstanceNotFoundException e) {}
        }
    }
}

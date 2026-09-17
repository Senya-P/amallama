package cz.cuni.mff.core.monitor;

import java.io.IOException;
import java.lang.management.ManagementFactory;

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
    private static final int DEFAULT_PORT = 9999;
    private static final String URL = "service:jmx:rmi://127.0.0.1:%d";
    private static final String OBJECT_NAME = "cz.cuni.mff.amallama:type=Runtime";

    private final RuntimeMonitorMXBean monitor;
    private final int port;
    private final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

    private JMXConnectorServer server;
    private ObjectName name;

    /**
     * @param monitor The MXBean to publish
     */
    public MonitoringAgent(RuntimeMonitorMXBean monitor) {
        this(monitor, DEFAULT_PORT);
    }

    /**
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
        System.out.println("Monitoring: " + server.getAddress());
    }

    /**
     * Starts the connector
     * @throws MonitoringException if it fails
     */
    public void tryStart() {
        try { start(); }
        catch (IOException | JMException e) {
            throw new MonitoringException("Cannot start JMX runtime monitoring: " + e);
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

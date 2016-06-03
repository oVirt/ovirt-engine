package org.ovirt.engine.core.bll.executor;

import java.lang.management.ManagementFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.executor.CommandController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class HystrixCommandController implements CommandController, CommandControllerMXBean {

    private static final Logger log = LoggerFactory.getLogger(HystrixCommandController.class);

    private boolean monitorActions = false;
    private boolean monitorQueries = false;
    private boolean monitorVdsBroker = false;
    private ObjectName objectName;
    private MBeanServer platformMBeanServer;

    @PostConstruct
    public void registerInJMX() {
        if (Config.getValue(ConfigValues.HystrixMonitoringEnabled))  {
            try {
                objectName = new ObjectName("HystrixCommandController:type=" + this.getClass().getName());
                platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
                platformMBeanServer.registerMBean(this, objectName);
                monitorAll(true);
            } catch (Exception e) {
                log.debug("Problem during registration of Monitoring into JMX: {}", e);
                throw new IllegalStateException("Problem during registration of Monitoring into JMX.", e);
            }
        }
    }

    @PreDestroy
    public void unregisterFromJMX() {
        if (Config.getValue(ConfigValues.HystrixMonitoringEnabled)) {
            try {
                platformMBeanServer.unregisterMBean(this.objectName);
            } catch (Exception e) {
                log.debug("Problem during unregistration of Monitoring into JMX: {}", e);
            }
        }
    }

    public boolean isMonitorActionsEnabled() {
        return monitorActions;
    }

    public boolean isMonitorQueriesEnabled() {
        return monitorQueries;
    }

    public boolean isMonitorVdsBrokerEnabled() {
        return monitorVdsBroker;
    }

    @Override
    public void monitorAll(boolean monitor) {
        monitorActions(monitor);
        monitorQueries(monitor);
        monitorVdsBroker(monitor);
    }

    @Override
    public void monitorActions(boolean monitor) {
        monitorActions = monitor;
    }

    @Override
    public void monitorQueries(boolean monitor) {
        monitorQueries = monitor;
    }

    @Override
    public void monitorVdsBroker(boolean monitor) {
        monitorVdsBroker = monitor;
    }
}

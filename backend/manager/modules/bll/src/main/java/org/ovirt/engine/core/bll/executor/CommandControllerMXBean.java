package org.ovirt.engine.core.bll.executor;

public interface CommandControllerMXBean {
    void monitorAll(boolean monitor);
    void monitorActions(boolean monitor);
    void monitorQueries(boolean monitor);
    void monitorVdsBroker(boolean monitor);

    boolean isMonitorActionsEnabled();
    boolean isMonitorQueriesEnabled();
    boolean isMonitorVdsBrokerEnabled();
}

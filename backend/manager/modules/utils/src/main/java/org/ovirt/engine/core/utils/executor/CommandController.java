package org.ovirt.engine.core.utils.executor;

public interface CommandController {

    boolean isMonitorActionsEnabled();
    boolean isMonitorQueriesEnabled();
    boolean isMonitorVdsBrokerEnabled();
}

package org.ovirt.engine.core.vdsbroker.monitoring;

public interface HostMonitoringInterface {
    void refresh();
    void afterRefreshTreatment();

    default boolean shouldReleaseMonitoringLockAfterRefresh() {
        return false;
    }
}

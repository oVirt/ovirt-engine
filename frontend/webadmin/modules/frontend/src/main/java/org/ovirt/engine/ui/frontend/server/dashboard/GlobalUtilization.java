package org.ovirt.engine.ui.frontend.server.dashboard;

/**
 * This class represents Resource Utilization on a Global level. It will contain aggregated data over all available
 * resources in the system.
 */
public class GlobalUtilization {
    private GlobalUtilizationResourceSummary cpu;
    private GlobalUtilizationResourceSummary memory;
    private GlobalUtilizationResourceSummary storage;

    public GlobalUtilizationResourceSummary getCpu() {
        return cpu;
    }

    public void setCpu(GlobalUtilizationResourceSummary cpu) {
        this.cpu = cpu;
    }

    public GlobalUtilizationResourceSummary getMemory() {
        return memory;
    }

    public void setMemory(GlobalUtilizationResourceSummary memory) {
        this.memory = memory;
    }

    public GlobalUtilizationResourceSummary getStorage() {
        return storage;
    }

    public void setStorage(GlobalUtilizationResourceSummary storage) {
        this.storage = storage;
    }

}

package org.ovirt.engine.ui.frontend.server.dashboard.models;

public class ResourcesTotal {
    private double memUsed;
    private double memTotal;
    private int cpuUsed;
    private int cpuTotal;

    /**
     * Get amount of memory used in Mb.
     * @return The amount of memory used.
     */
    public double getMemUsed() {
        return memUsed;
    }

    /**
     * Set the amount of memory used in Mb.
     * @param memUsed The amount of memory used.
     */
    public void setMemUsed(double memUsed) {
        this.memUsed = memUsed;
    }

    /**
     * Get the total amount of memory in Mb.
     * @return The total amount of memory
     */
    public double getMemTotal() {
        return memTotal;
    }

    /**
     * Set the total amount of memory used in Mb.
     * @param memTotal The total amount of memory.
     */
    public void setMemTotal(double memTotal) {
        this.memTotal = memTotal;
    }

    public int getCpuUsed() {
        return cpuUsed;
    }

    public void setCpuUsed(int cpuUsed) {
        this.cpuUsed = cpuUsed;
    }

    public int getCpuTotal() {
        return cpuTotal;
    }

    public void setCpuTotal(int cpuTotal) {
        this.cpuTotal = cpuTotal;
    }
}

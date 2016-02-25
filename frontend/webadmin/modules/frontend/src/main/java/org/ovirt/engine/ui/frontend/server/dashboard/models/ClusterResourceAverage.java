package org.ovirt.engine.ui.frontend.server.dashboard.models;

public class ClusterResourceAverage {
    private String name;
    private double cpuAverage;
    private double memAverage;

    public ClusterResourceAverage(String name, double cpuAverage, double memAverage) {
        super();
        this.name = name;
        this.cpuAverage = cpuAverage;
        this.memAverage = memAverage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCpuAverage() {
        return cpuAverage;
    }

    public void setCpuAverage(double cpuAverage) {
        this.cpuAverage = cpuAverage;
    }

    public double getMemoryAverage() {
        return memAverage;
    }

    public void setMemAverage(double memAverage) {
        this.memAverage = memAverage;
    }

}

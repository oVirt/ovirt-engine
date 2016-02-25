package org.ovirt.engine.ui.frontend.server.dashboard.models;

public class ResourceUsage {
    private long epoch;
    private double cpuValue;
    private double memValue;
    private double storageValue;

    public long getEpoch() {
        return epoch;
    }

    public void setEpoch(long epoch) {
        this.epoch = epoch;
    }

    public double getCpuValue() {
        return cpuValue;
    }

    public void setCpuValue(double value) {
        this.cpuValue = value;
    }

    public double getMemValue() {
        return memValue;
    }

    public void setMemValue(double memValue) {
        this.memValue = memValue;
    }

    public double getStorageValue() {
        return storageValue;
    }

    public void setStorageValue(double storageValue) {
        this.storageValue = storageValue;
    }

}

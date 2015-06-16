package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

/**
 * Object which represents a NUMA node statistics information
 *
 */
public class NumaNodeStatistics implements Serializable {

    private static final long serialVersionUID = 3274786304152401306L;

    private long memFree;

    private double cpuSys;

    private double cpuUser;

    private double cpuIdle;

    private int memUsagePercent;

    private int cpuUsagePercent;

    public NumaNodeStatistics() {
        memFree = 0L;
        cpuSys = 0.0;
        cpuUser = 0.0;
        cpuIdle = 0.0;
        memUsagePercent = 0;
        cpuUsagePercent = 0;
    }

    public long getMemFree() {
        return memFree;
    }

    public void setMemFree(long memFree) {
        this.memFree = memFree;
    }

    public double getCpuSys() {
        return cpuSys;
    }

    public void setCpuSys(double cpuSys) {
        this.cpuSys = cpuSys;
    }

    public double getCpuUser() {
        return cpuUser;
    }

    public void setCpuUser(double cpuUser) {
        this.cpuUser = cpuUser;
    }

    public double getCpuIdle() {
        return cpuIdle;
    }

    public void setCpuIdle(double cpuIdle) {
        this.cpuIdle = cpuIdle;
    }

    public int getMemUsagePercent() {
        return memUsagePercent;
    }

    public void setMemUsagePercent(int memUsagePercent) {
        this.memUsagePercent = memUsagePercent;
    }

    public int getCpuUsagePercent() {
        return cpuUsagePercent;
    }

    public void setCpuUsagePercent(int cpuUsagePercent) {
        this.cpuUsagePercent = cpuUsagePercent;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(cpuIdle);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(cpuSys);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + cpuUsagePercent;
        temp = Double.doubleToLongBits(cpuUser);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + (int) (memFree ^ (memFree >>> 32));
        result = prime * result + memUsagePercent;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NumaNodeStatistics other = (NumaNodeStatistics) obj;
        if (Double.doubleToLongBits(cpuIdle) != Double.doubleToLongBits(other.cpuIdle))
            return false;
        if (Double.doubleToLongBits(cpuSys) != Double.doubleToLongBits(other.cpuSys))
            return false;
        if (cpuUsagePercent != other.cpuUsagePercent)
            return false;
        if (Double.doubleToLongBits(cpuUser) != Double.doubleToLongBits(other.cpuUser))
            return false;
        if (memFree != other.memFree)
            return false;
        if (memUsagePercent != other.memUsagePercent)
            return false;
        return true;
    }

}

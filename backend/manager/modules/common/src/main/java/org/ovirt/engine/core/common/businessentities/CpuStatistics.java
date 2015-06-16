package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

/**
 * Object which represents host per cpu statistics information
 *
 */
public class CpuStatistics implements Serializable {

    private static final long serialVersionUID = 3274786304152401306L;

    private int cpuId;

    private double cpuSys;

    private double cpuUser;

    private double cpuIdle;

    private int cpuUsagePercent;

    public int getCpuId() {
        return cpuId;
    }

    public void setCpuId(int cpuId) {
        this.cpuId = cpuId;
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
        result = prime * result + cpuId;
        long temp;
        temp = Double.doubleToLongBits(cpuIdle);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(cpuSys);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + cpuUsagePercent;
        temp = Double.doubleToLongBits(cpuUser);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        CpuStatistics other = (CpuStatistics) obj;
        if (cpuId != other.cpuId)
            return false;
        if (Double.doubleToLongBits(cpuIdle) != Double.doubleToLongBits(other.cpuIdle))
            return false;
        if (Double.doubleToLongBits(cpuSys) != Double.doubleToLongBits(other.cpuSys))
            return false;
        if (cpuUsagePercent != other.cpuUsagePercent)
            return false;
        if (Double.doubleToLongBits(cpuUser) != Double.doubleToLongBits(other.cpuUser))
            return false;
        return true;
    }

}

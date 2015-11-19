package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

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
        return Objects.hash(
                cpuId,
                cpuIdle,
                cpuSys,
                cpuUsagePercent,
                cpuUser
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CpuStatistics)) {
            return false;
        }
        CpuStatistics other = (CpuStatistics) obj;
        return cpuId == other.cpuId
                && Objects.equals(cpuIdle, other.cpuIdle)
                && Objects.equals(cpuSys, other.cpuSys)
                && cpuUsagePercent == other.cpuUsagePercent
                && Objects.equals(cpuUser, other.cpuUser);
    }

}

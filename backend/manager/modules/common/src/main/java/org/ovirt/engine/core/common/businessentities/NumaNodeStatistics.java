package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Object which represents a NUMA node statistics information
 *
 */
@Embeddable
public class NumaNodeStatistics implements Serializable {

    private static final long serialVersionUID = 3274786304152401306L;

    @Column(name = "mem_free")
    private long memFree;

    @Column(name = "cpu_sys")
    private double cpuSys;

    @Column(name = "cpu_user")
    private double cpuUser;

    @Column(name = "cpu_idle")
    private double cpuIdle;

    @Column(name = "usage_mem_percent")
    private int memUsagePercent;

    @Column(name = "usage_cpu_percent")
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
        return Objects.hash(cpuIdle, cpuSys, cpuUser, cpuUsagePercent, memFree, memUsagePercent);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NumaNodeStatistics)) {
            return false;
        }
        NumaNodeStatistics other = (NumaNodeStatistics) obj;
        return Objects.equals(cpuIdle, other.cpuIdle)
                && Objects.equals(cpuSys, other.cpuSys)
                && Objects.equals(cpuUsagePercent, other.cpuUsagePercent)
                && Objects.equals(cpuUser, other.cpuUser)
                && Objects.equals(memFree, other.memFree)
                && Objects.equals(memUsagePercent, other.memUsagePercent);
    }

}

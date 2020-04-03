package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    private List<HugePage> hugePages;

    public NumaNodeStatistics() {
        memFree = 0L;
        cpuSys = 0.0;
        cpuUser = 0.0;
        cpuIdle = 0.0;
        memUsagePercent = 0;
        cpuUsagePercent = 0;
        hugePages = new ArrayList<>();
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

    public List<HugePage> getHugePages() {
        return hugePages;
    }

    public void setHugePages(List<HugePage> hugePages) {
        this.hugePages = hugePages;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                cpuIdle,
                cpuSys,
                cpuUsagePercent,
                cpuUser,
                memFree,
                memUsagePercent,
                hugePages
        );
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
                && cpuUsagePercent == other.cpuUsagePercent
                && Objects.equals(cpuUser, other.cpuUser)
                && memFree == other.memFree
                && memUsagePercent == other.memUsagePercent
                && Objects.equals(hugePages, other.hugePages);
    }

}

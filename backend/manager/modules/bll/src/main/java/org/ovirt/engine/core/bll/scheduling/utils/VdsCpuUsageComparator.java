package org.ovirt.engine.core.bll.scheduling.utils;

import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.common.businessentities.VDS;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator that compares the CPU usage of two hosts, with regard to the number of CPUs each host has and it's
 * strength.
 */
public class VdsCpuUsageComparator implements Comparator<VDS>, Serializable {
    boolean countThreadsAsCores;

    public VdsCpuUsageComparator(boolean countThreadsAsCores) {
        this.countThreadsAsCores = countThreadsAsCores;
    }

    @Override
    public int compare(VDS o1, VDS o2) {
        return Integer.valueOf(calculateCpuUsage(o1)).compareTo(calculateCpuUsage(o2));
    }

    private int calculateCpuUsage(VDS o1) {
        return o1.getUsageCpuPercent() * SlaValidator.getEffectiveCpuCores(o1, countThreadsAsCores) / o1.getVdsStrength();
    }
}

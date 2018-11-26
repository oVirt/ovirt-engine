package org.ovirt.engine.core.bll.scheduling.utils;

import java.io.Serializable;
import java.util.Comparator;

import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.common.businessentities.VDS;

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
        return Integer.compare(calculateCpuUsage(o1), calculateCpuUsage(o2));
    }

    private int calculateCpuUsage(VDS o1) {
        return o1.getUsageCpuPercent() * SlaValidator.getEffectiveCpuCores(o1, countThreadsAsCores);
    }
}

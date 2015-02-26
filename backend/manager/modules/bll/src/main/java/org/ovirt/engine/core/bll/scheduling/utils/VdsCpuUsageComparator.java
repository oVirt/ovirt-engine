package org.ovirt.engine.core.bll.scheduling.utils;

import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.common.businessentities.VDS;

import java.util.Comparator;

/**
 * Comparator that compares the CPU usage of two hosts, with regard to the number of CPUs each host has and it's
 * strength.
 */
public enum VdsCpuUsageComparator implements Comparator<VDS> {
    INSTANCE;

    @Override
    public int compare(VDS o1, VDS o2) {
        return Integer.valueOf(calculateCpuUsage(o1)).compareTo(calculateCpuUsage(o2));
    }

    private static int calculateCpuUsage(VDS o1) {
        return o1.getUsageCpuPercent() * SlaValidator.getEffectiveCpuCores(o1) / o1.getVdsStrength();
    }
}

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
    private final SlaValidator slaValidator;

    public VdsCpuUsageComparator(boolean countThreadsAsCores, SlaValidator slaValidator) {
        this.countThreadsAsCores = countThreadsAsCores;
        this.slaValidator = slaValidator;
    }

    @Override
    public int compare(VDS o1, VDS o2) {
        return Integer.compare(calculateCpuUsage(o1), calculateCpuUsage(o2));
    }

    private int calculateCpuUsage(VDS o1) {
        return o1.getUsageCpuPercent() * slaValidator.getEffectiveCpuCores(o1, countThreadsAsCores) / o1.getVdsStrength();
    }
}

package org.ovirt.engine.core.bll.scheduling.utils;

import java.io.Serializable;
import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

/**
 * Comparator that compares the CPU usage of two hosts, with regard to the number of CPUs each host has and it's
 * strength.
 */
public class VdsCpuUsageComparator implements Comparator<VDS>, Serializable {

    private static final long serialVersionUID = 1L;

    private ResourceManager resourceManager;

    private VdsCpuUnitPinningHelper vdsCpuUnitPinningHelper;

    private boolean countThreadsAsCores;

    public VdsCpuUsageComparator(
            ResourceManager resourceManager,
            VdsCpuUnitPinningHelper vdsCpuUnitPinningHelper,
            boolean countThreadsAsCores) {
        this.resourceManager = resourceManager;
        this.vdsCpuUnitPinningHelper = vdsCpuUnitPinningHelper;
        this.countThreadsAsCores = countThreadsAsCores;
    }

    @Override
    public int compare(VDS o1, VDS o2) {
        return Double.compare(calculateCpuUsage(o1), calculateCpuUsage(o2));
    }

    private double calculateCpuUsage(VDS host) {
        HostCpuLoadHelper cpuLoadHelper = new HostCpuLoadHelper(host,
                resourceManager,
                vdsCpuUnitPinningHelper,
                countThreadsAsCores);
        return cpuLoadHelper.getEffectiveSharedCpuLoad();
    }
}

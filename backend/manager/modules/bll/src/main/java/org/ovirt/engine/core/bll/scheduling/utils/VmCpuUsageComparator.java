package org.ovirt.engine.core.bll.scheduling.utils;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;

/**
 * Comparator that compares the CPU usage of two VMs, with regard to the number of CPUs each VM has.
 */
public enum VmCpuUsageComparator implements Comparator<VM> {
    INSTANCE;

    @Override
    public int compare(VM vm1, VM vm2) {
        return Integer.compare(calculateCpuUsage(vm1), calculateCpuUsage(vm2));
    }

    private static int calculateCpuUsage(VM o1) {
        return o1.getUsageCpuPercent() * VmCpuCountHelper.getDynamicNumOfCpu(o1);
    }
}

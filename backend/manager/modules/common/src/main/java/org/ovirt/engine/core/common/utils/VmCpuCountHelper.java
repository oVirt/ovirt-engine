package org.ovirt.engine.core.common.utils;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Version;

public class VmCpuCountHelper {
    /**
     * Computes the maximum allowed number of vCPUs that can be assigned
     * to a VM according to the specified compatibility level.
     *
     * @param vm The VM for which we want to know the maximum
     * @param compatibilityVersion The compatibility level
     * @return The maximum supported number of vCPUs
     */
    public static Integer calcMaxVCpu(VmBase vm, Version compatibilityVersion) {
        Integer maxSockets = Config.<Integer>getValue(
                ConfigValues.MaxNumOfVmSockets,
                compatibilityVersion.getValue());
        Integer maxVCpus = Config.<Integer>getValue(
                ConfigValues.MaxNumOfVmCpus,
                compatibilityVersion.getValue());

        int threadsPerCore = vm.getThreadsPerCpu();
        int cpuPerSocket = vm.getCpuPerSocket();
        maxVCpus = cpuPerSocket * threadsPerCore *
                Math.min(maxSockets, maxVCpus / (cpuPerSocket * threadsPerCore));
        return maxVCpus;
    }
}

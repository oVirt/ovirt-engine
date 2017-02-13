package org.ovirt.engine.core.common.utils;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VmCpuCountHelper {
    private static final int maxBitWidth = 8;
    private static final Logger log = LoggerFactory.getLogger(VmCpuCountHelper.class);

    private static int bitWidth(int n) {
        return n == 0 ? 0 : 32 - Integer.numberOfLeadingZeros(n - 1);
    }

    private static ArchitectureType architectureFamily(VM vm) {
        ArchitectureType clusterArchitecture = vm.getClusterArch();
        return clusterArchitecture == null ? null : clusterArchitecture.getFamily();
    }

    private static ArchitectureType architectureFamily(VmTemplate vmTemplate) {
        ArchitectureType clusterArchitecture = vmTemplate.getClusterArch();
        return clusterArchitecture == null ? null : clusterArchitecture.getFamily();
    }

    static Integer calcMaxVCpu(ArchitectureType architecture, Integer maxSockets, Integer maxVCpus,
                                      int threadsPerCore, int cpuPerSocket) {
        if (architecture == null || architecture == ArchitectureType.x86) {
            // As described in https://bugzilla.redhat.com/1406243#c13, the
            // maximum number of vCPUs is limited by thread and core numbers on
            // x86, to fit into an 8-bit APIC ID value organized by certain
            // rules. That limit is going to be removed once x2APIC is used.
            int oneSocketBitWidth = bitWidth(threadsPerCore) + bitWidth(cpuPerSocket);
            if (oneSocketBitWidth > maxBitWidth) {
                log.warn("{} cores with {} threads may be too many for the VM to be able to run",
                        cpuPerSocket, threadsPerCore);
            } else {
                int apicIdLimit = (int) Math.pow(2, 8 - oneSocketBitWidth);
                int apicVCpusLimit = cpuPerSocket * threadsPerCore * Math.min(maxSockets, apicIdLimit);
                if (apicVCpusLimit == 256) {
                    // Using the maximum 8-bit value may be unsafe under certain circumstances, see
                    // https://bugzilla.redhat.com/1406243#c17
                    // Note that maxVCpus must match the socket and thread counts.
                    apicVCpusLimit -= cpuPerSocket * threadsPerCore;
                }
                if (maxVCpus < apicVCpusLimit) {
                    // The value must be multiplication of cpuPerSocket * threadsPerCore.
                    maxVCpus = (maxVCpus / (cpuPerSocket * threadsPerCore)) * (cpuPerSocket * threadsPerCore);
                } else {
                    maxVCpus = apicVCpusLimit;
                }
            }
        } else {
            maxVCpus = cpuPerSocket * threadsPerCore *
                    Math.min(maxSockets, maxVCpus / (cpuPerSocket * threadsPerCore));
        }
        return maxVCpus;
    }

    /**
     * Computes the maximum allowed number of vCPUs that can be assigned
     * to a VM according to the specified compatibility level.
     *
     * @param vm The VM for which we want to know the maximum
     * @param compatibilityVersion The compatibility level
     * @param architecture Architecture family of the VM
     * @return The maximum supported number of vCPUs
     */
    private static Integer calcMaxVCpu(VmBase vm, Version compatibilityVersion, ArchitectureType architecture) {
        Integer maxSockets = Config.<Integer>getValue(
                ConfigValues.MaxNumOfVmSockets,
                compatibilityVersion.getValue());
        Integer maxVCpus = Config.<Integer>getValue(
                ConfigValues.MaxNumOfVmCpus,
                compatibilityVersion.getValue());

        int threadsPerCore = vm.getThreadsPerCpu();
        int cpuPerSocket = vm.getCpuPerSocket();
        return calcMaxVCpu(architecture, maxSockets, maxVCpus, threadsPerCore, cpuPerSocket);
    }

    /**
     * Computes the maximum allowed number of vCPUs that can be assigned
     * to a VM according to the specified compatibility level.
     *
     * @param vm                   The VM for which we want to know the maximum
     * @param compatibilityVersion The compatibility level
     * @return The maximum supported number of vCPUs
     */
    public static Integer calcMaxVCpu(VM vm, Version compatibilityVersion) {
        return calcMaxVCpu(vm.getStaticData(), compatibilityVersion, architectureFamily(vm));
    }

    /**
     * Computes the maximum allowed number of vCPUs that can be assigned
     * to a VM according to the specified compatibility level.
     *
     * @param vmTemplate The VM template for which we want to know the maximum
     * @param compatibilityVersion The compatibility level
     * @return The maximum supported number of vCPUs
     */
    public static Integer calcMaxVCpu(VmTemplate vmTemplate, Version compatibilityVersion) {
        return calcMaxVCpu(vmTemplate, compatibilityVersion, architectureFamily(vmTemplate));
    }

    /**
     * Validates CPU count configuration. It may not be possible to use
     * configurations with too many threads or cores.
     *
     * @param vm The VM for which we want to check the CPU count configuration
     * @return Whether the CPU count configuration is valid
     */
    public static boolean validateCpuCounts(VM vm) {
        ArchitectureType architecture = architectureFamily(vm);
        if (architecture == null || architecture == ArchitectureType.x86) {
            return bitWidth(vm.getThreadsPerCpu()) + bitWidth(vm.getCpuPerSocket()) <= maxBitWidth;
        }
        return true;
    }
}

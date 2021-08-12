package org.ovirt.engine.core.common.utils;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Version;

public class VmCommonUtils {

    /**
     * Check if any CPUs may and need to be hotplugged or unplugged when configuration of a running VM is
     * updated from <code>source</code> to <code>destination</code>.
     *
     * @param source current configuration of the VM
     * @param destination new configuration of the VM
     * @return true, if any CPUs are to be hotplugged, false otherwise
     */
    public static boolean isCpusToBeHotpluggedOrUnplugged(VM source, VM destination) {
        return source.getCpuPerSocket() == destination.getCpuPerSocket()
                && source.getNumOfSockets() != destination.getNumOfSockets()
                && source.getThreadsPerCpu() == destination.getThreadsPerCpu();
    }

    /**
     * Check if memory may and needs to be hotplugged when configuration of a running VM is
     * updated from <code>source</code> to <code>destination</code>.
     *
     * @param source current configuration of the VM
     * @param destination new configuration of the VM
     * @return true, if memory is to be hotplugged, false otherwise
     */
    public static boolean isMemoryToBeHotplugged(VM source, VM destination) {
        if (source.getMemSizeMb() >= destination.getMemSizeMb()) {
            return false;
        }
        ArchitectureType clusterArch = destination.getClusterArch();
        if (clusterArch == null) {
            clusterArch = source.getClusterArch();
        }
        if (clusterArch == null) {
            // Nothing known about the architecture, let's assume it's valid.
            // If it is not, it will fail later in VM update validation.
            return true;
        }
        final int hotplugMemorySizeFactor = clusterArch.getHotplugMemorySizeFactorMb();
        if ((destination.getMemSizeMb() - source.getMemSizeMb()) % hotplugMemorySizeFactor == 0) {
            return true;
        }
        return false;
    }

    /**
     * Note: backend only
     *
     * @see #maxMemorySizeWithHotplugInMb(int, Version)
     */
    public static int maxMemorySizeWithHotplugInMb(VM vm) {
        return maxMemorySizeWithHotplugInMb(vm.getOs(), vm.getCompatibilityVersion());
    }

    /**
     * Check if VM Lease changed and need to be hot plugged or hot unplugged when configuration of a running VM is
     * updated from <code>source</code> to <code>destination</code>.
     *
     * @param source current configuration of the VM
     * @param destination new configuration of the VM
     * @return true, if VM Lease is to be hotplugged, false otherwise
     */
    public static boolean isVmLeaseToBeHotPluggedOrUnplugged(VM source, VM destination) {
        return !Objects.equals(source.getLeaseStorageDomainId(), destination.getLeaseStorageDomainId());
    }

    /**
     * Return total maximum possible memory size for the given VM, including hotplugged memory.
     *
     * <p>Note: backend only</p>
     *
     * @param osId id of operating system
     * @param compatibilityVersion version of config value to query
     * @return the total possible memory size with hotplug
     */
    public static int maxMemorySizeWithHotplugInMb(int osId, Version compatibilityVersion) {
        final ConfigValues configValue = getMaxMemConfigValueByOsId(osId);
        return Config.<Integer>getValue(
            configValue,
            compatibilityVersion != null ? compatibilityVersion.getValue() : Version.getLast().getValue()
        );
    }

    private static ConfigValues getMaxMemConfigValueByOsId(int osId) {
         OsRepository osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);
         return osRepository.get64bitOss().contains(osId)
                 ? (osRepository.getOsArchitectures().get(osId).getFamily() == ArchitectureType.ppc
                        ? ConfigValues.VMPpc64BitMaxMemorySizeInMB
                        : ConfigValues.VM64BitMaxMemorySizeInMB)
                 : ConfigValues.VM32BitMaxMemorySizeInMB;
     }

     public static int getMaxMemorySizeDefault(int memorySize) {
        final int maxMemoryDefaultRatio = 4;
        return maxMemoryDefaultRatio * memorySize;
     }

     public static int calcMinMemory(int memory, int overcommit) {
         double overCommitFactor = 100.0 / overcommit;
         return (int) (memory * overCommitFactor);
     }
}

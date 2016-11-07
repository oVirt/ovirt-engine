package org.ovirt.engine.core.common.utils;

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
    public static boolean isCpusToBeHotplugged(VM source, VM destination) {
        return source.getCpuPerSocket() == destination.getCpuPerSocket()
                && source.getNumOfSockets() != destination.getNumOfSockets()
                && source.getThreadsPerCpu() == destination.getThreadsPerCpu();
    }

    /**
     * Check if memory may and needs to be hotplugged or unplugged when configuration of a running VM is
     * updated from <code>source</code> to <code>destination</code>.
     *
     * <b>Note:</b> this method may be used by both frontend and backend, that's why it does not call
     * FeatureSupported.hotUnplugMemory() by itself. It is up to caller to check this feature availability
     * and to pass a correct <tt>memoryUnplugSupported</tt> parameter.
     *
     * @param source current configuration of the VM
     * @param destination new configuration of the VM
     * @param memoryUnplugSupported true, if memory unplugging feature is implemented in the Engine, false otherwise
     * @return true, if memory is to be hotplugged, false otherwise
     */
    public static boolean isMemoryToBeHotplugged(VM source, VM destination, boolean memoryUnplugSupported) {
        return source.getMemSizeMb() < destination.getMemSizeMb()
                || memoryUnplugSupported && source.getMemSizeMb() > destination.getMemSizeMb();
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
        return compatibilityVersion != null
                ? Config.<Integer>getValue(configValue, compatibilityVersion.getValue())
                : Config.<Integer>getValue(configValue);
    }

    private static ConfigValues getMaxMemConfigValueByOsId(int osId) {
         OsRepository osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);
         return osRepository.get64bitOss().contains(osId)
                 ? (osRepository.getOsArchitectures().get(osId).getFamily() == ArchitectureType.ppc
                        ? ConfigValues.VMPpc64BitMaxMemorySizeInMB
                        : ConfigValues.VM64BitMaxMemorySizeInMB)
                 : ConfigValues.VM32BitMaxMemorySizeInMB;
     }

}

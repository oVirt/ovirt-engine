package org.ovirt.engine.core.common.utils;

import org.ovirt.engine.core.common.businessentities.VM;

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

}

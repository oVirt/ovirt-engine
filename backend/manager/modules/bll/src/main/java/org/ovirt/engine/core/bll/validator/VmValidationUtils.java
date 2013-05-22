package org.ovirt.engine.core.bll.validator;

import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.compat.Version;

public class VmValidationUtils {

    /**
     * Check if the memory size is within the correct limits (as per the configuration), taking into account the
     * OS type.
     *
     * @param osId The OS identifier.
     * @param memSizeInMB The memory size to validate.
     *
     * @return Is the memory within the configured limits or not.
     */
    public static boolean isMemorySizeLegal(int osId, int memSizeInMB, Version clusterVersion) {
        return memSizeInMB >= getMinMemorySizeInMb(osId,clusterVersion) && memSizeInMB <= getMaxMemorySizeInMb(osId, clusterVersion);
    }

    /**
     * Get the configured minimum VM memory size allowed.
     *
     * @return The minimum VM memory size allowed (as per configuration).
     */
    public static Integer getMinMemorySizeInMb(int osId, Version version) {
        return SimpleDependecyInjector.getInstance().get(OsRepository.class).getMinimumRam(osId, version);
    }

    /**
     * Get the configured maximum VM memory size for this OS type.
     *
     * @param osId The type of OS to get the maximum memory for.
     *
     * @return The maximum VM memory setting for this OS (as per configuration).
     */
    public static Integer getMaxMemorySizeInMb(int osId, Version clusterVersion) {
        return SimpleDependecyInjector.getInstance().get(OsRepository.class).getMaximumRam(osId, clusterVersion);
    }
}

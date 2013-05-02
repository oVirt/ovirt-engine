package org.ovirt.engine.core.bll.validator;

import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

public class VmValidationUtils {
    private static final String X64BIT = "x64";

    /**
     * Check if the memory size is within the correct limits (as per the configuration), taking into account the
     * OS type.
     *
     * @param osType The OS type.
     * @param memSizeInMB The memory size to validate.
     *
     * @return Is the memory within the configured limits or not.
     */
    public static boolean isMemorySizeLegal(VmOsType osType, int memSizeInMB, String clusterVersion) {
        return memSizeInMB >= getMinMemorySizeInMb() && memSizeInMB <= getMaxMemorySizeInMb(osType, clusterVersion);
    }

    /**
     * Get the configured minimum VM memory size allowed.
     *
     * @return The minimum VM memory size allowed (as per configuration).
     */
    public static Integer getMinMemorySizeInMb() {
        return Config.<Integer> GetValue(ConfigValues.VMMinMemorySizeInMB);
    }

    /**
     * Get the configured maximum VM memory size for this OS type.
     *
     * @param osType The type of OS to get the maximum memory for.
     *
     * @return The maximum VM memory setting for this OS (as per configuration).
     */
    public static Integer getMaxMemorySizeInMb(VmOsType osType, String clusterVersion) {
        if ((osType == VmOsType.Other || osType == VmOsType.OtherLinux)
                || osType.name().toLowerCase().endsWith(X64BIT)) {
            return Config.<Integer> GetValue(ConfigValues.VM64BitMaxMemorySizeInMB, clusterVersion);
        }
        return Config.<Integer> GetValue(ConfigValues.VM32BitMaxMemorySizeInMB);
    }
}

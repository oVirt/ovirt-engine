package org.ovirt.engine.core.common.utils;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.compat.Version;

public class VmDeviceCommonUtils {

    final static String NETWORK_CHAR = "N";
    final static String CDROM_CHAR = "D";
    final static String DRIVE_CHAR = "C";

    public static boolean isNetwork(VmDevice device) {
        return (device.getType().equals(VmDeviceType.getName(VmDeviceType.INTERFACE)));
    }

    public static boolean isDisk(VmDevice device) {
        return (device.getType().equals(VmDeviceType.getName(VmDeviceType.DISK)) && device.getDevice().equals(VmDeviceType.getName(VmDeviceType.DISK)));
    }

    public static boolean isCD(VmDevice device) {
        return (device.getType().equals(VmDeviceType.getName(VmDeviceType.DISK)) && device.getDevice().equals(VmDeviceType.getName(VmDeviceType.CDROM)));
    }

    /**
     * Computes old boot sequence enum value from the given list of devices.
     *
     * @param devices
     * @return
     */
    public static BootSequence getBootSequence(List<VmDevice> devices) {
        StringBuilder sb = new StringBuilder();
        BootSequence ret = BootSequence.C;
        for (VmDevice device : devices) {
            if (device.getBootOrder() > 0) {
                if (isNetwork(device) && sb.indexOf(NETWORK_CHAR) < 0) {
                    sb.append(NETWORK_CHAR);
                }
                if (isDisk(device) && sb.indexOf(DRIVE_CHAR) < 0) {
                    sb.append(DRIVE_CHAR);
                }
                if (isCD(device) && sb.indexOf(CDROM_CHAR) < 0) {
                    sb.append(CDROM_CHAR);
                }
                // maximum string is 3 characters, so, if reached , exit loop.
                if (sb.length() == 3) {
                    break;
                }
            }
        }

        for (BootSequence bs : BootSequence.values()) {
            if (bs.name().equals(sb.toString())) {
                ret = bs;
                break;
            }
        }
        return ret;
    }

    public static boolean isOldClusterVersion(VM vm) {
        Version version = vm.getvds_group_compatibility_version();
        return (!(version.getMajor() >= 3 && version.getMinor() >= 1));
    }
}

package org.ovirt.engine.core.common.utils;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.compat.Version;

public class VmDeviceCommonUtils {

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
        BootSequence ret = BootSequence.C;
        String seq = "";
        for (VmDevice device : devices) {
            if (device.getBootOrder() > 0) {
                if (isNetwork(device) && seq.indexOf('N') < 0) {
                    seq.concat("N");
                }
                if (isDisk(device) && seq.indexOf('C') < 0) {
                    seq.concat("C");
                }
                if (isCD(device) && seq.indexOf('D') < 0) {
                    seq.concat("D");
                }
                // maximum string is 3 characters, so, if reached , exit loop.
                if (seq.length() == 3) {
                    break;
                }
            }
        }

        for (BootSequence bs : BootSequence.values()) {
            if (bs.name().equals(seq)) {
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

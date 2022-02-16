package org.ovirt.engine.core.common.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.compat.Version;

public class MDevTypesUtils {

    public static final String MDEV_TYPE = "mdevType";
    public static final String NODISPLAY = "nodisplay";

    /**
     * Returns list of mdev device spec params
     *
     * @param vmDevices the VM devices
     * @param deviceType the particular requested type of the mdev device
     * @return the list of mdev devices
     */
    public static List<VmDevice> getMdevs(List<VmDevice> vmDevices, VmDeviceType deviceType) {
        return vmDevices.stream().filter(device -> device.getType() == VmDeviceGeneralType.MDEV
                && deviceType.getName().equals(device.getDevice())).collect(Collectors.toList());
    }

    public static boolean isMdevDisplayOnSupported(Version version) {
        // Nvidia vGPU VNC console is only supported on RHEL >= 7.6
        // See https://bugzilla.redhat.com/show_bug.cgi?id=1633623 for details and discussion
        return version.greaterOrEquals(Version.v4_3);
    }
}

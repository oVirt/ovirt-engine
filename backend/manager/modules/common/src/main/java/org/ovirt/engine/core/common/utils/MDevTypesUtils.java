package org.ovirt.engine.core.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MDevTypesUtils {

    private static final Logger log = LoggerFactory.getLogger(MDevTypesUtils.class);

    public static final String DEPRECATED_CUSTOM_PROPERTY_NAME = "mdev_type";
    public static final String MDEV_TYPE = "mdevType";
    public static final String DRIVER_PARAMETERS = "driverParams";
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

    public static List<VmDevice> convertDeprecatedCustomPropertyToVmDevices(String property, Guid vmId) {
        if (property.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            List<VmDevice> devices = new ArrayList<>();
            Boolean nodisplay = Boolean.FALSE;
            for (String type : property.split(",")) {
                if (type.equals(NODISPLAY)) {
                    nodisplay = Boolean.TRUE;
                } else {
                    VmDevice device = new VmDevice();
                    device.setId(new VmDeviceId(Guid.newGuid(), vmId));
                    device.setType(VmDeviceGeneralType.MDEV);
                    device.setDevice(VmDeviceType.VGPU.getName());
                    device.setAddress("");
                    device.setManaged(true);
                    device.setPlugged(true);
                    Map<String, Object> specParams = new HashMap<>();
                    specParams.put(MDevTypesUtils.MDEV_TYPE, type);
                    specParams.put(MDevTypesUtils.NODISPLAY, nodisplay);
                    device.setSpecParams(specParams);
                    devices.add(device);
                }
            }
            return devices;
        } catch (Exception e) {
            log.info("failed to parse deprecated custom property mdev_type: {}", property);
            return Collections.emptyList();
        }
    }
}

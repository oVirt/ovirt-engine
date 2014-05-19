package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.common.utils.VmDeviceType;

// todo - note  - now this is display type. because of os info, we don't change this now but in follow up
public enum DisplayType {
    vnc(VmDeviceType.VGA),
    qxl(VmDeviceType.QXL);

    private VmDeviceType defaultVmDeviceType;

    public int getValue() {
        return this.ordinal();
    }

    DisplayType(VmDeviceType defaultVmDeviceType) {
        this.defaultVmDeviceType = defaultVmDeviceType;
    }
    public static DisplayType forValue(int value) {
        return values()[value];
    }

    public VmDeviceType getDefaultVmDeviceType() {
        return defaultVmDeviceType;
    }

}

package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.common.utils.VmDeviceType;

public enum DisplayType {
    @Deprecated
    cirrus(VmDeviceType.CIRRUS),
    qxl(VmDeviceType.QXL),
    vga(VmDeviceType.VGA),
    bochs(VmDeviceType.BOCHS),
    none(null); // For Headless VM, this type means that the VM will run without any display (VIDEO) device

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

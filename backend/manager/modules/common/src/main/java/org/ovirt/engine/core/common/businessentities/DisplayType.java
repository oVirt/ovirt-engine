package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.common.utils.VmDeviceType;

public enum DisplayType {
    vnc(VmDeviceType.CIRRUS),
    qxl(VmDeviceType.QXL);

    private VmDeviceType vmDeviceType;

    public int getValue() {
        return this.ordinal();
    }

    DisplayType(VmDeviceType vmDeviceType) {
        this.vmDeviceType = vmDeviceType;
    }
    public static DisplayType forValue(int value) {
        return values()[value];
    }

    public VmDeviceType getVmDeviceType() {
        return vmDeviceType;
    }

}

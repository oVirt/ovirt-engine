package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.common.utils.VmDeviceType;

public enum GraphicsType {

    SPICE(VmDeviceType.SPICE),
    VNC(VmDeviceType.VNC);

    VmDeviceType devType;

    private GraphicsType(VmDeviceType type) {
        this.devType = type;
    }

    public static GraphicsType fromString(String s) { // valueOf is case-sensitive
        for (GraphicsType graphicsType : GraphicsType.values()) {
            if (graphicsType.toString().equalsIgnoreCase(s)) {
                return graphicsType;
            }
        }
        return null;
    }

    public VmDeviceType getCorrespondingDeviceType() {
        return devType;
    }

    public static GraphicsType fromVmDeviceType(VmDeviceType type) {
        for (GraphicsType graphicsType : GraphicsType.values()) {
            if (graphicsType.getCorrespondingDeviceType() == type) {
                return graphicsType;
            }
        }

        return null;
    }
}

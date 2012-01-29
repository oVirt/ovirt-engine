package org.ovirt.engine.core.common.utils;

public enum VmDeviceType {
    DISK,
    CDROM,
    INTERFACE,
    VIDEO,
    BRIDGE;

    public static String getName(VmDeviceType vmDEviceType) {
        return vmDEviceType.name().toLowerCase();
    }
}


package org.ovirt.engine.core.common.utils;

public enum VmDeviceType {
    FLOPPY,
    DISK,
    CDROM,
    INTERFACE,
    BRIDGE,
    VIDEO,
    QXL,
    CIRRUS,
    SOUND;

    public static String getName(VmDeviceType vmDEviceType) {
        return vmDEviceType.name().toLowerCase();
    }
}


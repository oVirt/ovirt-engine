package org.ovirt.engine.core.common.utils;

public enum VmDeviceType {
    FLOPPY("floppy", "14"),
    DISK("disk", "17"),
    CDROM("cdrom", "15"),
    INTERFACE("interface", "10"),
    BRIDGE("bridge"),
    VIDEO("video", "5"),
    QXL("qxl"),
    CIRRUS("cirrus"),
    SOUND("sound"),
    OTHER("other", "0"),
    UNKNOWN("unknown");

    private String name;
    private String ovfResourceType;

    VmDeviceType(String name) {
        this.name = name;
    }

    VmDeviceType(String name, String ovfResourceType) {
        this.name = name;
        this.ovfResourceType = ovfResourceType;
    }

    public String getName() {
        return name;
    }

    /**
     * This method maps OVF Resource Types to oVirt devices.
     *
     * @param resourceType
     * @return
     */
    public VmDeviceType getoVirtDevice(int resourceType) {
        for (VmDeviceType deviceType : values()) {
            if (deviceType.ovfResourceType != null && Integer.valueOf(deviceType.ovfResourceType) == resourceType) {
                return deviceType;
            }
        }
        return UNKNOWN;
    }
}


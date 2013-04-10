package org.ovirt.engine.core.common.utils;

public enum VmDeviceType {
    FLOPPY("floppy", "14"),
    DISK("disk", "17"),
    CDROM("cdrom", "15"),
    INTERFACE("interface", "10"),
    BRIDGE("bridge", "3"),
    VIDEO("video", "20"),
    USB("usb", "23"),
    CONTROLLER("controller", "23"),
    REDIR("redir", "23"),
    SPICEVMC("spicevmc", "23"),
    QXL("qxl"),
    CIRRUS("cirrus"),
    SOUND("sound"),
    ICH6("ich6"),
    AC97("ac97"),
    MEMBALLOON("memballoon"),
    SMARTCARD("smartcard"),
    BALLOON("balloon"),
    WATCHDOG("watchdog"),
    OTHER("other", "0"),
    UNKNOWN("unknown", "-1");

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
    public static VmDeviceType getoVirtDevice(int resourceType) {
        for (VmDeviceType deviceType : values()) {
            if (deviceType.ovfResourceType != null && Integer.valueOf(deviceType.ovfResourceType) == resourceType) {
                return deviceType;
            }
        }
        return UNKNOWN;
    }

    /**
     * gets sound device type for a given device name
     *
     * @param name
     * @return
     */
    public static VmDeviceType getSoundDeviceType(String name) {
        for (VmDeviceType deviceType : values()) {
            if (deviceType.getName().equals(name)) {
                return deviceType;
            }
        }
        return VmDeviceType.ICH6;
    }
}


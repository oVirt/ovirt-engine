package org.ovirt.engine.core.common.utils;

public enum VmDeviceType {
    FLOPPY("floppy", "14"),
    DISK("disk", "17"),
    LUN("lun"),
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
    VGA("vga"),
    SPICE("spice"),
    VNC("vnc"),
    SOUND("sound"),
    ICH6("ich6"),
    AC97("ac97"),
    MEMBALLOON("memballoon"),
    CHANNEL("channel"),
    SMARTCARD("smartcard"),
    BALLOON("balloon"),
    CONSOLE("console"),
    VIRTIO("virtio"),
    WATCHDOG("watchdog"),
    VIRTIOSCSI("virtio-scsi"),
    VIRTIOSERIAL("virtio-serial"),
    HOST_DEVICE("hostdev"),
    MEMORY("memory"),
    PCI("pci"),
    IDE("ide"),
    SATA("sata"),
    ICH9("ich9"),
    TPM("tpm"),
    BOCHS("bochs"),
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
     */
    public static VmDeviceType getoVirtDevice(int resourceType) {
        for (VmDeviceType deviceType : values()) {
            if (deviceType.ovfResourceType != null && Integer.parseInt(deviceType.ovfResourceType) == resourceType) {
                return deviceType;
            }
        }
        return UNKNOWN;
    }

    /**
     * gets sound device type for a given device name
     */
    public static VmDeviceType getSoundDeviceType(String name) {
        for (VmDeviceType deviceType : values()) {
            if (deviceType.getName().equals(name)) {
                return deviceType;
            }
        }
        return VmDeviceType.ICH6;
    }

    /**
     * gets device type for a given device name
     */
    public static VmDeviceType getByName(String name) {
        for (VmDeviceType vmDeviceType : values()) {
            if (name.equals(vmDeviceType.getName())) {
                return vmDeviceType;
            }
        }
        return null;
    }

}

package org.ovirt.engine.core.common.businessentities;

/**
 * https://libvirt.org/formatdomain.html#elementsCharConsole
 */
public enum ConsoleTargetType {
    /**
     * Default console type on most platforms
     */
    SERIAL("serial"),
    VIRTIO("virtio"),
    /**
     * Native console type for s390x
     */
    SCLP("sclp");

    public final String libvirtName;

    ConsoleTargetType(String libvirtName) {
        this.libvirtName = libvirtName;
    }

    public static ConsoleTargetType fromLibvirtName(String libvirtName) {
        for (ConsoleTargetType consoleTargetType : values()) {
            if (consoleTargetType.libvirtName.equals(libvirtName)) {
                return consoleTargetType;
            }
        }
        throw new RuntimeException("Unknown libvirt name '" + libvirtName + "'");
    }
}

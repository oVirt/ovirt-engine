package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.common.utils.VmDeviceType;

/**
 * General types of devices recognized by VDSM
 */
public enum VmDeviceGeneralType {
    /**
     * A hard disk, floppy or cdrom device
     */
    DISK,

    /**
     * A network interface
     */
    INTERFACE,

    /**
     * A video card
     */
    VIDEO,

    /**
     * A graphics framebuffer type
     */
    GRAPHICS,

    /**
     * A sound card or PC speaker
     */
    SOUND,

    /**
     * An internal controller that usually provides a bus
     */
    CONTROLLER,

    /**
     * A memory balloon device
     */
    BALLOON,

    /**
     * A host-guest communication channel
     */
    CHANNEL,

    /**
     * USB device redirection
     */
    REDIR,

    /**
     * USB device redirection channel
     */
    REDIRDEV,

    /**
     * A console device
     */
    CONSOLE,

    /**
     * A random number generator device
     */
    RNG,

    /**
     * A smartcard device
     */
    SMARTCARD,

    /**
     * A watchdog device
     */
    WATCHDOG,

    /**
     * A pass-through host device
     */
    HOSTDEV,

    /**
     * A memory device
     */
    MEMORY,

    /**
     * A TPM device
     */
    TPM,

    /**
     * Unknown device
     */
    UNKNOWN;

    /**
     * Database value
     */
    private String value;

    /**
     * Creates an instance and assigns database value
     */
    private VmDeviceGeneralType() {
        value = name().toLowerCase();
    }

    /**
     * Converts enum type to string value to save in database
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets database value. Should be used only for UNKNOWN type!!!
     */
    private void setValue(String value) {
        this.value = value;
    }

    /**
     * Converts string representation to enum value
     */
    public static VmDeviceGeneralType forValue(String value) {
        VmDeviceGeneralType type;
        try {
            type = VmDeviceGeneralType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            type = UNKNOWN;
            type.setValue(value);
        }
        return type;
    }

    /**
     * Converts {@code VmDeviceType} representation to enum value
     */
    public static VmDeviceGeneralType forValue(VmDeviceType value) {
        VmDeviceGeneralType type;
        switch (value) {
            case DISK:
            case CDROM:
            case FLOPPY:
                type = DISK;
                break;

            case INTERFACE:
            case BRIDGE:
                type = INTERFACE;
                break;

            case VIDEO:
            case CIRRUS:
            case QXL:
                type = VIDEO;
                break;

            case SOUND:
            case AC97:
            case ICH6:
                type = SOUND;
                break;

            case CONTROLLER:
            case USB:
            case VIRTIOSCSI:
            case VIRTIOSERIAL:
                type = CONTROLLER;
                break;

            case BALLOON:
            case MEMBALLOON:
                type = BALLOON;
                break;

            case REDIR:
            case SPICEVMC:
                type = REDIR;
                break;

            case CHANNEL:
                type = CHANNEL;
                break;

            case SMARTCARD:
                type = SMARTCARD;
                break;

            case WATCHDOG:
                type = WATCHDOG;
                break;

            case HOST_DEVICE:
                type = HOSTDEV;
                break;

            case TPM:
                type = TPM;
                break;

            default:
                // try to guess from String value
                type = forValue(value.getName());
                break;
        }
        return type;
    }
}

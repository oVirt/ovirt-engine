package org.ovirt.engine.core.common.businessentities;

public enum UsbPolicy {
    DISABLED(1),
    ENABLED_NATIVE(2);

    public static final String PRE_3_1_ENABLED = "Enabled";
    public static final String PRE_3_1_DISABLED = "Disabled";
    public static final String PRE_4_1_ENABLED_LEGACY = "ENABLED_LEGACY";

    private final int value;

    UsbPolicy(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static UsbPolicy forValue(int value) {
        for (UsbPolicy usbPolicy : values()) {
            if (usbPolicy.getValue() == value) {
                return usbPolicy;
            }
        }
        return DISABLED;
    }

    /*
     * This method is used in the OVF reader, to support old values
     */
    public static UsbPolicy forStringValue(String value) {
        UsbPolicy retVal = null;

        if (value.equalsIgnoreCase(PRE_3_1_ENABLED) ||
                PRE_4_1_ENABLED_LEGACY.equalsIgnoreCase(value) ||
                value.equalsIgnoreCase(ENABLED_NATIVE.name())) {
            retVal = ENABLED_NATIVE;
        } else if (value.equalsIgnoreCase(PRE_3_1_DISABLED) || value.equalsIgnoreCase(DISABLED.name())) {
            retVal = DISABLED;
        }
        return retVal;
    }
}

package org.ovirt.engine.core.common.businessentities;

public enum UsbPolicy {
    ENABLED_LEGACY,
    DISABLED,
    ENABLED_NATIVE;

    public static final String PRE_3_1_ENABLED = "Enabled";
    public static final String PRE_3_1_DISABLED = "Disabled";

    public int getValue() {
        return this.ordinal();
    }

    public static UsbPolicy forValue(int value) {
        return values()[value];
    }

    /*
     * This method is used in the OVF reader, to support old values
     */
    public static UsbPolicy forStringValue(String value) {
        UsbPolicy retVal = null;
        if (value.equalsIgnoreCase(PRE_3_1_ENABLED) || value.equalsIgnoreCase(ENABLED_LEGACY.name())) {
            retVal = ENABLED_LEGACY;
        } else if (value.equalsIgnoreCase(PRE_3_1_DISABLED) || value.equalsIgnoreCase(DISABLED.name())) {
            retVal = DISABLED;
        } else if (value.equalsIgnoreCase(ENABLED_NATIVE.name())) {
            retVal = ENABLED_NATIVE;
        }
        return retVal;
    }
}

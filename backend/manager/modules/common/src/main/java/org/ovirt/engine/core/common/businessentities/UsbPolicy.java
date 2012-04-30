package org.ovirt.engine.core.common.businessentities;

public enum UsbPolicy {
    ENABLED_LEGACY,
    DISABLED,
    ENABLED_NATIVE;

    private static String oldEnabledValue;
    private static String oldDisabledValue;

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
        if (value.equalsIgnoreCase(oldEnabledValue) || value.equalsIgnoreCase(ENABLED_LEGACY.name())) {
            retVal = ENABLED_LEGACY;
        } else if (value.equalsIgnoreCase(oldDisabledValue) || value.equalsIgnoreCase(DISABLED.name())) {
            retVal = DISABLED;
        } else if (value.equalsIgnoreCase(ENABLED_NATIVE.name())) {
            retVal = ENABLED_NATIVE;
        }
        return retVal;
    }
}

package org.ovirt.engine.core.common.businessentities;

public enum UsbPolicy {
    Enabled,
    Disabled;

    public int getValue() {
        return this.ordinal();
    }

    public static UsbPolicy forValue(int value) {
        return values()[value];
    }
}

package org.ovirt.engine.core.common.businessentities;

public enum SANState {

    OK,
    PARTIAL,
    UKNOWN;

    public int getValue() {
        return this.ordinal();
    }

    public static SANState forValue(int value) {
        return values()[value];
    }

}

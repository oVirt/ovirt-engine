package org.ovirt.engine.core.common.businessentities;

public enum ImageStatus {
    Unassigned,
    OK,
    LOCKED,
    INVALID,
    ILLEGAL;

    public int getValue() {
        return this.ordinal();
    }

    public static ImageStatus forValue(int value) {
        return values()[value];
    }

}

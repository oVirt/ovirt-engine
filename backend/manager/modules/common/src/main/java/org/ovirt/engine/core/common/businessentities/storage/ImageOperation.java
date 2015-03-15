package org.ovirt.engine.core.common.businessentities.storage;

public enum ImageOperation {
    Unassigned,
    Copy,
    Move;

    public int getValue() {
        return this.ordinal();
    }

    public static ImageOperation forValue(int value) {
        return values()[value];
    }
}

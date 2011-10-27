package org.ovirt.engine.core.searchbackend;

public enum PagingType {
    Range,
    Offset;

    public int getValue() {
        return this.ordinal();
    }

    public static PagingType forValue(int value) {
        return values()[value];
    }
}

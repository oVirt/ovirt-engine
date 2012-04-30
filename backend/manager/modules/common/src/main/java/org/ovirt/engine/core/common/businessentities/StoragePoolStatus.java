package org.ovirt.engine.core.common.businessentities;

public enum StoragePoolStatus {
    Uninitialized,
    Up,
    Maintanance,
    NotOperational,
    Problematic,
    Contend;

    public int getValue() {
        return this.ordinal();
    }

    public static StoragePoolStatus forValue(int value) {
        return values()[value];
    }
}

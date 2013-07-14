package org.ovirt.engine.core.common.businessentities;

public enum StoragePoolStatus implements Identifiable {
    Uninitialized,
    Up,
    Maintenance,
    NotOperational,
    NonResponsive,
    Contend;

    @Override
    public int getValue() {
        return this.ordinal();
    }

    public static StoragePoolStatus forValue(int value) {
        return values()[value];
    }
}

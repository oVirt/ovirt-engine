package org.ovirt.engine.core.common.businessentities;

public enum StorageDomainOwnerType {

    Unknown;

    public int getValue() {
        return this.ordinal();
    }

    public static StorageDomainOwnerType forValue(int value) {
        return values()[value];
    }
}

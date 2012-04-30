package org.ovirt.engine.core.common.businessentities;

public enum StorageDomainStatus {
    Unknown,
    Uninitialized,
    Unattached,
    Active,
    InActive,
    Locked,
    Maintenance;

    public int getValue() {
        return this.ordinal();
    }

    public static StorageDomainStatus forValue(int value) {
        return values()[value];
    }

}

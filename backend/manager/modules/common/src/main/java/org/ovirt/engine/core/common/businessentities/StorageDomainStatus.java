package org.ovirt.engine.core.common.businessentities;

public enum StorageDomainStatus {
    Unknown,
    Uninitialized,
    Unattached,
    Active,
    Inactive,
    Locked,
    Maintenance,
    PreparingForMaintenance;

    public int getValue() {
        return this.ordinal();
    }

    public static StorageDomainStatus forValue(int value) {
        return values()[value];
    }

    public boolean isStorageDomainInProcess() {
        return this == Locked || this == PreparingForMaintenance;
    }
}

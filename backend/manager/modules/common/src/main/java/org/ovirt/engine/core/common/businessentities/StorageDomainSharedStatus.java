package org.ovirt.engine.core.common.businessentities;

public enum StorageDomainSharedStatus {
    Unattached,
    Active,
    InActive,
    Mixed,
    Locked;

    public int getValue() {
        return this.ordinal();
    }

    public static StorageDomainSharedStatus forValue(int value) {
        return values()[value];
    }
}

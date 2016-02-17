package org.ovirt.engine.core.common.businessentities;

public enum StorageDomainSharedStatus implements Identifiable {
    // FIXME add ids and remove the ordinal impl of getValue
    Unattached,
    Active,
    Inactive,
    Mixed;

    @Override
    public int getValue() {
        return this.ordinal();
    }

    public static StorageDomainSharedStatus forValue(int value) {
        return values()[value];
    }
}

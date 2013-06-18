package org.ovirt.engine.core.common.businessentities;

public enum StorageDomainType {

    Master,
    Data,
    ISO,
    ImportExport,
    Image,
    Unknown;

    public int getValue() {
        return this.ordinal();
    }

    public static StorageDomainType forValue(int value) {
        return values()[value];
    }

    public boolean isDataDomain() {
        return (this == Data || this == Master);
    }
}

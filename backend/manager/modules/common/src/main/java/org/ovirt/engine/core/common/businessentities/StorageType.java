package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

public enum StorageType implements Identifiable {
    UNKNOWN(0, false),
    NFS(1, true),
    FCP(2, true),
    ISCSI(3, true),
    LOCALFS(4, true),
    POSIXFS(6, true),
    GLUSTERFS(7, true),
    GLANCE(8, true),
    // CIFS(5)
    ALL(-1, false);

    private int value;
    // this member is indicating whether then enum value represents an actual storage type
    private boolean isConcreteStorageType;

    private static java.util.HashMap<Integer, StorageType> mappings = new HashMap<Integer, StorageType>();

    static {
        for (StorageType storageType : values()) {
            mappings.put(storageType.getValue(), storageType);
        }
    }

    private StorageType(int value, boolean isConcreteStorageType) {
        this.value = value;
        this.isConcreteStorageType = isConcreteStorageType;
    }

    @Override
    public int getValue() {
        return this.value;
    }

    public boolean isConcreteStorageType() {
        return this.isConcreteStorageType;
    }

    public static StorageType forValue(int value) {
        return mappings.get(value);
    }

    public boolean isFileDomain() {
        return this == NFS || this == POSIXFS || this == LOCALFS || this == GLUSTERFS || this == GLANCE;
    }

    public boolean isBlockDomain() {
        return this == FCP || this == ISCSI;
    }

}

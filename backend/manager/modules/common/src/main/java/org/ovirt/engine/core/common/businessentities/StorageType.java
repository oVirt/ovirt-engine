package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

public enum StorageType implements Identifiable {

    UNKNOWN(0),
    NFS(1),
    FCP(2),
    ISCSI(3),
    LOCALFS(4),
    POSIXFS(6),
    GLUSTERFS(7),
    GLANCE(8);

    private int value;

    private static final HashMap<Integer, StorageType> mappings = new HashMap<Integer, StorageType>();

    static {
        for (StorageType storageType : values()) {
            mappings.put(storageType.getValue(), storageType);
        }
    }

    private StorageType(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return this.value;
    }

    public boolean isConcreteStorageType() {
        return this != UNKNOWN;
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

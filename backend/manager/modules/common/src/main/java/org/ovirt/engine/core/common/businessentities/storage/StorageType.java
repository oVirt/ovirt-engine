package org.ovirt.engine.core.common.businessentities.storage;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Identifiable;

public enum StorageType implements Identifiable {

    UNKNOWN(0, Subtype.NONE),
    NFS(1, Subtype.FILE),
    FCP(2, Subtype.BLOCK),
    ISCSI(3, Subtype.BLOCK),
    LOCALFS(4, Subtype.FILE),
    POSIXFS(6, Subtype.FILE),
    GLUSTERFS(7, Subtype.FILE),
    GLANCE(8, Subtype.FILE),
    CINDER(9, Subtype.OPENSTACK),
    MANAGED_BLOCK_STORAGE(10, Subtype.OPENSTACK),
    UNMANAGED(11, Subtype.KUBERNETES);

    public enum Subtype { NONE, FILE, BLOCK, OPENSTACK, KUBERNETES }

    private int value;
    private Subtype subtype;

    private static final Map<Integer, StorageType> mappings = new HashMap<>();

    static {
        for (StorageType storageType : values()) {
            mappings.put(storageType.getValue(), storageType);
        }
    }

    StorageType(int value, Subtype subtype) {
        this.value = value;
        this.subtype = subtype;
    }

    @Override
    public int getValue() {
        return this.value;
    }

    public Subtype getStorageSubtype() {
        return subtype;
    }

    public boolean isConcreteStorageType() {
        return subtype != Subtype.NONE;
    }

    public static StorageType forValue(int value) {
        return mappings.get(value);
    }

    public boolean isFileDomain() {
        return subtype == Subtype.FILE;
    }

    public boolean isBlockDomain() {
        return subtype == Subtype.BLOCK;
    }

    public boolean isInternal() {
        return isFileDomain() || isBlockDomain();
    }

    public boolean isLocal() {
        return this == LOCALFS;
    }

    public boolean isShared() {
        return !isLocal();
    }

    public boolean isOpenStackDomain() {
        return this == GLANCE || this == CINDER;
    }

    public boolean isCinderDomain() {
        return this == CINDER;
    }

    public boolean isManagedBlockStorage() {
        return this == MANAGED_BLOCK_STORAGE;
    }
}

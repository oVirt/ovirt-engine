package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class StoragePoolIsoMap implements BusinessEntityWithStatus<StoragePoolIsoMapId, StorageDomainStatus> {

    private static final long serialVersionUID = -2829958589095415567L;

    public StoragePoolIsoMap() {
        id = new StoragePoolIsoMapId();
    }

    public StoragePoolIsoMap(Guid storage_id, Guid storage_pool_id, StorageDomainStatus status) {
        this();
        setStorageId(storage_id);
        setStoragePoolId(storage_pool_id);
        this.setStatus(status);
    }

    private StoragePoolIsoMapId id;

    @Override
    public StoragePoolIsoMapId getId() {
        return this.id;
    }

    @Override
    public void setId(StoragePoolIsoMapId id) {
        this.id = id;
    }

    public Guid getStorageId() {
        return id.getStorageId();
    }

    public void setStorageId(Guid value) {
        id.setStorageId(value);
    }

    public Guid getStoragePoolId() {
        return this.id.getStoragePoolId();
    }

    public void setStoragePoolId(Guid value) {
        this.id.setStoragePoolId(value);
    }

    private Integer persistentStorageDomainStatus;

    @Override
    public StorageDomainStatus getStatus() {
        if (persistentStorageDomainStatus == null) {
            return null;
        }
        return StorageDomainStatus.forValue(persistentStorageDomainStatus);
    }

    @Override
    public void setStatus(StorageDomainStatus value) {
        if (value == null) {
            persistentStorageDomainStatus = null;
        } else {
            persistentStorageDomainStatus = value.getValue();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                persistentStorageDomainStatus
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StoragePoolIsoMap)) {
            return false;
        }
        StoragePoolIsoMap other = (StoragePoolIsoMap) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(persistentStorageDomainStatus, other.persistentStorageDomainStatus);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", getId())
                .append("status", getStatus())
                .build();
    }
}

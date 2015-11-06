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
        setstorage_id(storage_id);
        setstorage_pool_id(storage_pool_id);
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

    public Guid getstorage_id() {
        return id.getStorageId();
    }

    public void setstorage_id(Guid value) {
        id.setStorageId(value);
    }

    public Guid getstorage_pool_id() {
        return this.id.getStoragePoolId();
    }

    public void setstorage_pool_id(Guid value) {
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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result
              + ((persistentStorageDomainStatus == null) ? 0 : persistentStorageDomainStatus.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StoragePoolIsoMap other = (StoragePoolIsoMap) obj;
        return (Objects.equals(id, other.id)
                && Objects.equals(persistentStorageDomainStatus, other.persistentStorageDomainStatus));
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", getId())
                .append("status", getStatus())
                .build();
    }
}

package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class StoragePoolIsoMap implements BusinessEntity<StoragePoolIsoMapId> {

    private static final long serialVersionUID = -2829958589095415567L;

    public StoragePoolIsoMap() {
    }

    public StoragePoolIsoMap(Guid storage_id, Guid storage_pool_id, StorageDomainStatus status) {
        setstorage_id(storage_id);
        setstorage_pool_id(storage_pool_id);
        this.setstatus(status);
    }

    private StoragePoolIsoMapId id = new StoragePoolIsoMapId();

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

    public NGuid getstorage_pool_id() {
        return this.id.getStoragePoolId();
    }

    public void setstorage_pool_id(NGuid value) {
        this.id.setStoragePoolId(value);
    }

    private Integer persistentStorageDomainStatus = null;

    public StorageDomainStatus getstatus() {
        if (persistentStorageDomainStatus == null) {
            return null;
        }
        return StorageDomainStatus.forValue(persistentStorageDomainStatus);
    }

    public void setstatus(StorageDomainStatus value) {
        if (value == null) {
            persistentStorageDomainStatus = null;
        } else {
            persistentStorageDomainStatus = value.getValue();
        }
    }

    private Integer persistentOwner = StorageDomainOwnerType.Unknown.getValue();

    public StorageDomainOwnerType getowner() {
        if (persistentOwner == null) {
            return null;
        }
        return StorageDomainOwnerType.forValue(persistentOwner);
    }

    public void setowner(StorageDomainOwnerType value) {
        if (value == null) {
            persistentOwner = null;
        } else {
            persistentOwner = value.getValue();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((persistentOwner == null) ? 0 : persistentOwner.hashCode());
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
        return (ObjectUtils.objectsEqual(id, other.id)
                && ObjectUtils.objectsEqual(persistentOwner, other.persistentOwner)
                && ObjectUtils.objectsEqual(persistentStorageDomainStatus, other.persistentStorageDomainStatus));
    }
}

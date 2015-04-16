package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class StoragePoolIsoMapId implements Serializable {
    private static final long serialVersionUID = -3579958698510291360L;

    private Guid storageId;

    private Guid storagePoolId;

    public StoragePoolIsoMapId() {
    }

    public StoragePoolIsoMapId(Guid storageId, Guid storagePoolId) {
        this.storageId = storageId;
        this.storagePoolId = storagePoolId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((storageId == null) ? 0 : storageId.hashCode());
        result = prime * result + ((storagePoolId == null) ? 0 : storagePoolId.hashCode());
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
        StoragePoolIsoMapId other = (StoragePoolIsoMapId) obj;
        return (ObjectUtils.objectsEqual(storageId, other.storageId)
                && ObjectUtils.objectsEqual(storagePoolId, other.storagePoolId));
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("storagePoolId", getStoragePoolId())
                .append("storageId", getStorageId())
                .build();
    }

    public Guid getStorageId() {
        return storageId;
    }

    public void setStorageId(Guid storageId) {
        this.storageId = storageId;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }
}

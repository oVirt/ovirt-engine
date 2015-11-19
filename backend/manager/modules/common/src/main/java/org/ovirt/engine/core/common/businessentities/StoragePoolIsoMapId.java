package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

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
        return Objects.hash(
                storageId,
                storagePoolId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StoragePoolIsoMapId)) {
            return false;
        }
        StoragePoolIsoMapId other = (StoragePoolIsoMapId) obj;
        return Objects.equals(storageId, other.storageId)
                && Objects.equals(storagePoolId, other.storagePoolId);
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

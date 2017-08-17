package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class StoragePoolParametersBase extends ActionParametersBase {
    private static final long serialVersionUID = 8118928386101354539L;

    private Guid vdsId;
    private Guid storagePoolId;
    private boolean forceDelete;

    public StoragePoolParametersBase() {
        storagePoolId = Guid.Empty;
    }

    public StoragePoolParametersBase(Guid storagePoolId) {
        setStoragePoolId(storagePoolId);
    }

    public StoragePoolParametersBase(Guid storagePoolId, Guid vdsId) {
        this(storagePoolId);
        this.vdsId = vdsId;
    }

    public StoragePoolParametersBase(StoragePoolParametersBase other) {
        this.vdsId = other.vdsId;
        this.storagePoolId = other.storagePoolId;
        this.forceDelete = other.forceDelete;
    }

    public Guid getVdsId() {
        return vdsId;
    }

    public void setVdsId(Guid value) {
        vdsId = value;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid value) {
        storagePoolId = value;
    }

    public boolean isForceDelete() {
        return forceDelete;
    }

    public void setForceDelete(boolean forceDelete) {
        this.forceDelete = forceDelete;
    }
}

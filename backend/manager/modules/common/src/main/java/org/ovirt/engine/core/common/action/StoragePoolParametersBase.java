package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class StoragePoolParametersBase extends VdcActionParametersBase {
    private static final long serialVersionUID = 8118928386101354539L;

    private Guid vdsId;
    private Guid storagePoolId = new Guid();
    private boolean suppressCheck;
    private boolean forceDelete;

    public StoragePoolParametersBase() {
    }

    public StoragePoolParametersBase(Guid storagePoolId) {
        setStoragePoolId(storagePoolId);
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

    public boolean getSuppressCheck() {
        return suppressCheck;
    }

    public void setSuppressCheck(boolean value) {
        suppressCheck = value;
    }

    public boolean getForceDelete() {
        return forceDelete;
    }

    public void setForceDelete(boolean forceDelete) {
        this.forceDelete = forceDelete;
    }
}

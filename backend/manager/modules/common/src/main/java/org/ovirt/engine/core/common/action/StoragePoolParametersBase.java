package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class StoragePoolParametersBase extends VdcActionParametersBase {
    private static final long serialVersionUID = 8118928386101354539L;

    private Guid vdsId;
    private Guid storagePoolId = Guid.Empty;
    private boolean forceDelete;

    public StoragePoolParametersBase() {
    }

    public StoragePoolParametersBase(Guid storagePoolId) {
        setStoragePoolId(storagePoolId);
    }

    public StoragePoolParametersBase(Guid storagePoolId, Guid vdsId) {
        this(storagePoolId);
        this.vdsId = vdsId;
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

    public boolean getForceDelete() {
        return forceDelete;
    }

    public void setForceDelete(boolean forceDelete) {
        this.forceDelete = forceDelete;
    }
}

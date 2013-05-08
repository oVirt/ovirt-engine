package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class StoragePoolQueryParametersBase extends VdcQueryParametersBase {
    private static final long serialVersionUID = 7256579055993119209L;

    private Guid privateStoragePoolId = Guid.Empty;

    public Guid getStoragePoolId() {
        return privateStoragePoolId;
    }

    private void setStoragePoolId(Guid value) {
        privateStoragePoolId = value;
    }

    public StoragePoolQueryParametersBase(Guid storagePoolId) {
        setStoragePoolId(storagePoolId);
    }

    public StoragePoolQueryParametersBase() {
    }
}

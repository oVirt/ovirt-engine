package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetAllAttachableDisks extends VdcQueryParametersBase {

    private static final long serialVersionUID = 155490543085422118L;

    private Guid storagePoolId;

    public GetAllAttachableDisks() {
    }

    public GetAllAttachableDisks(Guid storagePoolId) {
        this.setStoragePoolId(storagePoolId);
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

}

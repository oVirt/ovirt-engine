package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetAllImagesListByStoragePoolIdParameters extends GetAllImagesListParametersBase {
    private static final long serialVersionUID = 6098440434536241071L;

    public GetAllImagesListByStoragePoolIdParameters() {
    }

    public GetAllImagesListByStoragePoolIdParameters(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    private Guid storagePoolId = new Guid();

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid value) {
        storagePoolId = value;
    }
}

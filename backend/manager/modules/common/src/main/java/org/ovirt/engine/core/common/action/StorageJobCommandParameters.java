package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class StorageJobCommandParameters extends ImagesActionsParametersBase {
    private Guid storageJobId;

    public StorageJobCommandParameters() {
    }

    public StorageJobCommandParameters(Guid imageId) {
        super(imageId);
    }

    public Guid getStorageJobId() {
        return storageJobId;
    }

    public void setStorageJobId(Guid storageJobId) {
        this.storageJobId = storageJobId;
    }
}

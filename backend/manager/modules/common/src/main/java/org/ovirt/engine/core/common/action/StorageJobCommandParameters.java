package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class StorageJobCommandParameters extends ImagesActionsParametersBase implements HostJobCommandParameters {
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

    @JsonIgnore
    @Override
    public Guid getHostJobId() {
        return getStorageJobId();
    }
}

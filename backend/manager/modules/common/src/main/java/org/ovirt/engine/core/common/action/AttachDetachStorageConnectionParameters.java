package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class AttachDetachStorageConnectionParameters extends StorageDomainParametersBase {

    private String storageConnectionId;

    public String getStorageConnectionId() {
        return storageConnectionId;
    }

    public void setStorageConnectionId(String storageConnectionId) {
        this.storageConnectionId = storageConnectionId;
    }

    public AttachDetachStorageConnectionParameters() {
    }

    public AttachDetachStorageConnectionParameters(Guid storageDomainId, String storageConnectionId) {
        super(storageDomainId);
        setStorageConnectionId(storageConnectionId);
    }
}

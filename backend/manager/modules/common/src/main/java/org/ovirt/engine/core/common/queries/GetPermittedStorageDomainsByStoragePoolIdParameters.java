package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetPermittedStorageDomainsByStoragePoolIdParameters extends GetEntitiesWithPermittedActionParameters {
    private static final long serialVersionUID = -4755625680567272859L;
    private Guid storagePoolId;

    public GetPermittedStorageDomainsByStoragePoolIdParameters() {
    }

    public void setStoragePoolId(Guid id) {
        this.storagePoolId = id;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

}

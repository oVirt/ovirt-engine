package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetUnregisteredDisksQueryParameters extends StorageDomainQueryParametersBase {
    private static final long serialVersionUID = -4297079748697573496L;

    private Guid storagePoolId;

    public GetUnregisteredDisksQueryParameters() {
    }

    public GetUnregisteredDisksQueryParameters(Guid storageDomainId, Guid storagePoolId) {
        super(storageDomainId);
        this.storagePoolId = storagePoolId;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }
}

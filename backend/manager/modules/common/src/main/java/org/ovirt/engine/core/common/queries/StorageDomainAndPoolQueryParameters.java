package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class StorageDomainAndPoolQueryParameters extends StorageDomainQueryParametersBase {
    private static final long serialVersionUID = -1397159559995940530L;

    private Guid privateStoragePoolId;

    public Guid getStoragePoolId() {
        return privateStoragePoolId;
    }

    private void setStoragePoolId(Guid value) {
        privateStoragePoolId = value;
    }

    public StorageDomainAndPoolQueryParameters(Guid storageDomainId, Guid storagePoolId) {
        super(storageDomainId);
        setStoragePoolId(storagePoolId);
    }

    public StorageDomainAndPoolQueryParameters() {
        privateStoragePoolId = Guid.Empty;
    }
}

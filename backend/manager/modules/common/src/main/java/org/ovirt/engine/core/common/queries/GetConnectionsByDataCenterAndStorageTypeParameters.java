package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;

public class GetConnectionsByDataCenterAndStorageTypeParameters extends IdQueryParameters {

    private static final long serialVersionUID = 3630182261969029480L;

    private StorageType storageType;

    public GetConnectionsByDataCenterAndStorageTypeParameters() {}

    public GetConnectionsByDataCenterAndStorageTypeParameters(Guid dataCenterId, StorageType storageType) {
        super(dataCenterId);
        this.storageType = storageType;
    }

    public StorageType getStorageType() {
        return storageType;
    }
}

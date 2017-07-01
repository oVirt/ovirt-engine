package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.storage.StorageType;

public class GetStorageDomainDefaultWipeAfterDeleteParameters extends QueryParametersBase {

    private static final long serialVersionUID = 6469435734937894858L;
    private StorageType storageType;

    public GetStorageDomainDefaultWipeAfterDeleteParameters() {}

    public GetStorageDomainDefaultWipeAfterDeleteParameters(StorageType storageType) {
        this.storageType = storageType;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }
}

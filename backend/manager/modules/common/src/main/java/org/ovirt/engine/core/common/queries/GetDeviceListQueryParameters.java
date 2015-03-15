package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;

public class GetDeviceListQueryParameters extends VdsIdParametersBase {
    private static final long serialVersionUID = -3909252459169512472L;
    private StorageType privateStorageType;

    public StorageType getStorageType() {
        return privateStorageType;
    }

    private void setStorageType(StorageType value) {
        privateStorageType = value;
    }

    public GetDeviceListQueryParameters(Guid vdsId, StorageType storageType) {
        super(vdsId);
        setStorageType(storageType);
    }

    public GetDeviceListQueryParameters() {
        privateStorageType = StorageType.UNKNOWN;
    }
}

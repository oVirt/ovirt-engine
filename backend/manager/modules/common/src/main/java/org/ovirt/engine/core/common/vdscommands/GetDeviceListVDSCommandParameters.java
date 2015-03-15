package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;

public class GetDeviceListVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    public GetDeviceListVDSCommandParameters(Guid vdsId, StorageType storageType) {
        super(vdsId);
        setStorageType(storageType);
    }

    private StorageType privateStorageType;

    public StorageType getStorageType() {
        return privateStorageType;
    }

    private void setStorageType(StorageType value) {
        privateStorageType = value;
    }

    public GetDeviceListVDSCommandParameters() {
        privateStorageType = StorageType.UNKNOWN;
    }

    @Override
    public String toString() {
        return String.format("%s, storageType=%s", super.toString(), getStorageType());
    }
}

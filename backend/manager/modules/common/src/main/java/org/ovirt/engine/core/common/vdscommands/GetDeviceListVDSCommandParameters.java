package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
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
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("storageType", getStorageType());
    }
}

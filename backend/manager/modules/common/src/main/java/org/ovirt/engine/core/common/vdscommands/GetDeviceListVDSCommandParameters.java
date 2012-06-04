package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.compat.Guid;

public class GetDeviceListVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    public GetDeviceListVDSCommandParameters(Guid vdsId, StorageType storageType) {
        super(vdsId);
        setStorageType(storageType);
    }

    public GetDeviceListVDSCommandParameters(Guid vdsId, StorageType storageType, boolean filteringLUNsEnabled) {
        super(vdsId);
        setStorageType(storageType);
        setFilteringLUNsEnabled(filteringLUNsEnabled);
    }

    private StorageType privateStorageType = StorageType.forValue(0);

    public StorageType getStorageType() {
        return privateStorageType;
    }

    private void setStorageType(StorageType value) {
        privateStorageType = value;
    }

    private boolean filteringLUNsEnabled = false;

    public boolean isFilteringLUNsEnabled() {
        return filteringLUNsEnabled;
    }

    public void setFilteringLUNsEnabled(boolean filteringLUNsEnabled) {
        this.filteringLUNsEnabled = filteringLUNsEnabled;
    }

    public GetDeviceListVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, storageType=%s", super.toString(), getStorageType());
    }
}

package org.ovirt.engine.core.common.vdscommands;

import java.util.Set;

import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class GetDeviceListVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    public GetDeviceListVDSCommandParameters(Guid vdsId, StorageType storageType) {
        this(vdsId, storageType, false, null);
    }

    public GetDeviceListVDSCommandParameters(Guid vdsId, StorageType storageType, boolean checkStatus, Set lunIds) {
        super(vdsId);
        setStorageType(storageType);
        setLunIds(lunIds);
        setCheckStatus(checkStatus);
    }

    private StorageType privateStorageType;

    public StorageType getStorageType() {
        return privateStorageType;
    }

    private void setStorageType(StorageType value) {
        privateStorageType = value;
    }

    private Set<String> privateLunIds;

    public Set<String> getLunIds() {
        return privateLunIds;
    }

    public void setLunIds(Set<String> value) {
        privateLunIds = value;
    }

    private boolean checkStatus;

    public boolean isCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(boolean checkStatus) {
        this.checkStatus = checkStatus;
    }

    public GetDeviceListVDSCommandParameters() {
        privateStorageType = StorageType.UNKNOWN;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("storageType", getStorageType())
                .append("checkStatus", isCheckStatus())
                .append("lunIds", getLunIds());
    }
}

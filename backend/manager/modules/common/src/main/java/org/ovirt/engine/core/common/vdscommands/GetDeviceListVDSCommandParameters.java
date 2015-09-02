package org.ovirt.engine.core.common.vdscommands;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class GetDeviceListVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    public GetDeviceListVDSCommandParameters(Guid vdsId, StorageType storageType) {
        this(vdsId, storageType, false, null);
    }

    public GetDeviceListVDSCommandParameters(Guid vdsId, StorageType storageType, boolean checkStatus, List lunIds) {
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

    private List<String> privateLunIds;

    public List<String> getLunIds() {
        return privateLunIds;
    }

    public void setLunIds(List<String> value) {
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

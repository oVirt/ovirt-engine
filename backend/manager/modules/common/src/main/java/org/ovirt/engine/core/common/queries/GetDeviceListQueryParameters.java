package org.ovirt.engine.core.common.queries;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;

public class GetDeviceListQueryParameters extends IdQueryParameters {
    private static final long serialVersionUID = -3909252459169512472L;
    private StorageType privateStorageType;
    private boolean checkStatus;
    private List<String> lunIds;

    public StorageType getStorageType() {
        return privateStorageType;
    }

    private void setStorageType(StorageType value) {
        privateStorageType = value;
    }

    public boolean isCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(boolean checkStatus) {
        this.checkStatus = checkStatus;
    }

    public List<String> getLunIds() {
        return lunIds;
    }

    public void setLunIds(List<String> lunIds) {
        this.lunIds = lunIds;
    }

    public GetDeviceListQueryParameters(Guid vdsId, StorageType storageType, boolean checkStatus, List<String> lunIds) {
        super(vdsId);
        setStorageType(storageType);
        setCheckStatus(checkStatus);
        setLunIds(lunIds);
    }

    public GetDeviceListQueryParameters() {
        this(null, StorageType.UNKNOWN, false, null);
    }
}

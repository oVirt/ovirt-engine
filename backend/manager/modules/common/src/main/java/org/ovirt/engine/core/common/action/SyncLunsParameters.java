package org.ovirt.engine.core.common.action;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.compat.Guid;

public class SyncLunsParameters extends StoragePoolParametersBase {

    private static final long serialVersionUID = 6054070767854809959L;

    private List<LUNs> deviceList;

    public SyncLunsParameters() {
        this(null);
    }

    public SyncLunsParameters(Guid storagePoolId) {
        this(storagePoolId, null);
    }

    public SyncLunsParameters(Guid storagePoolId, List<LUNs> deviceList) {
        super(storagePoolId);
        this.deviceList = deviceList;
    }

    public List<LUNs> getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(List<LUNs> deviceList) {
        this.deviceList = deviceList;
    }
}

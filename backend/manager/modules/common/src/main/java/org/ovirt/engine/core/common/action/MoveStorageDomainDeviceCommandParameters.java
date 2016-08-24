package org.ovirt.engine.core.common.action;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public class MoveStorageDomainDeviceCommandParameters extends StorageJobCommandParameters {
    private String srcDeviceId;
    private List<String> dstDevicesIds;

    public MoveStorageDomainDeviceCommandParameters() {
    }

    public MoveStorageDomainDeviceCommandParameters(Guid storageDomainId,
            String srcDeviceId,
            List<String> dstDevicesIds) {
        this.srcDeviceId = srcDeviceId;
        this.dstDevicesIds = dstDevicesIds;
        setStorageDomainId(storageDomainId);
    }

    public MoveStorageDomainDeviceCommandParameters(Guid storageDomainId,
                                                    String srcDeviceId) {
        this(storageDomainId, srcDeviceId, null);
    }

    public String getSrcDeviceId() {
        return srcDeviceId;
    }

    public void setSrcDeviceId(String srcDeviceId) {
        this.srcDeviceId = srcDeviceId;
    }

    public List<String> getDstDevicesIds() {
        return dstDevicesIds;
    }

    public void setDstDevicesIds(List<String> dstDevicesIds) {
        this.dstDevicesIds = dstDevicesIds;
    }
}

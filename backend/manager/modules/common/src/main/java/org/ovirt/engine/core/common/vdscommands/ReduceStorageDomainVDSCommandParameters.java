package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class ReduceStorageDomainVDSCommandParameters extends StorageJobVdsCommandParameters {
    private String deviceId;

    public ReduceStorageDomainVDSCommandParameters() {
    }

    public ReduceStorageDomainVDSCommandParameters(Guid jobId, Guid storageDomainId, String deviceId) {
        super(storageDomainId, jobId);
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb).append("deviceId", deviceId);
    }
}

package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ReduceStorageDomainCommandParameters extends StorageJobCommandParameters {
    private String deviceId;

    public ReduceStorageDomainCommandParameters() {
    }

    public ReduceStorageDomainCommandParameters(Guid storageDomain, String deviceId) {
        this.deviceId = deviceId;
        setStorageDomainId(storageDomain);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}

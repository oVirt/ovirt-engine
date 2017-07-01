package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class HostDeviceParameters extends QueryParametersBase {

    private Guid hostId;
    private String deviceName;

    public HostDeviceParameters() {
    }

    public HostDeviceParameters(Guid hostId, String deviceName) {
        this.hostId = hostId;
        this.deviceName = deviceName;
    }

    public Guid getHostId() {
        return hostId;
    }

    public void setHostId(Guid hostId) {
        this.hostId = hostId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}

package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class VmHostDeviceQueryParameters extends IdQueryParameters {

    private String deviceName;

    public VmHostDeviceQueryParameters() {
    }

    public VmHostDeviceQueryParameters(Guid vmId, String deviceName) {
        super(vmId);
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }
}

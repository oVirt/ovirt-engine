package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.Guid;

public class VmHostDevice extends VmDevice {

    public VmHostDevice() {
        setType(VmDeviceGeneralType.HOSTDEV);
        setAddress("");
        setIsManaged(true);
        setIsPlugged(true);
        setId(new VmDeviceId());
    }

    public VmHostDevice(VmDevice device) {
        this();
        setId(device.getId());
        setSpecParams(device.getSpecParams());
    }

    public VmHostDevice(Guid vmId, HostDevice device) {
        this();
        setDeviceId(Guid.newGuid());
        setVmId(vmId);
        setDevice(device.getDeviceName());
    }
}

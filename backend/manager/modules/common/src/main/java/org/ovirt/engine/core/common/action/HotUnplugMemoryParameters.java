package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.compat.Guid;

public class HotUnplugMemoryParameters extends VmOperationParameterBase {

    private Guid deviceId;

    public HotUnplugMemoryParameters(VmDeviceId vmDeviceId) {
        super(vmDeviceId.getVmId());
        this.deviceId = vmDeviceId.getDeviceId();
    }

    /** Just to please GWT */
    protected HotUnplugMemoryParameters() {
    }

    public Guid getDeviceId() {
        return deviceId;
    }
}

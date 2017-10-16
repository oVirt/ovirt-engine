package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.VmDeviceId;

public class HotUnplugMemoryWithoutVmUpdateParameters extends HotUnplugMemoryParameters {

    private int minMemoryMb;

    public HotUnplugMemoryWithoutVmUpdateParameters(VmDeviceId vmDeviceId, int minMemoryMb) {
        super(vmDeviceId);
        this.minMemoryMb = minMemoryMb;
    }

    /** Just to please GWT */
    private HotUnplugMemoryWithoutVmUpdateParameters() {
    }

    public int getMinMemoryMb() {
        return minMemoryMb;
    }
}

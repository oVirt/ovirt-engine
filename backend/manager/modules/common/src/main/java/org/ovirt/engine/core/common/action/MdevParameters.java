package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.VmDevice;

public class MdevParameters extends ActionParametersBase {

    private boolean isVm = true;
    private VmDevice device;

    public MdevParameters() {
    }

    public MdevParameters(VmDevice device, boolean isVm) {
        this.device = device;
        this.isVm = isVm;
    }

    public boolean isVm() {
        return isVm;
    }

    public VmDevice getDevice() {
        return device;
    }

}

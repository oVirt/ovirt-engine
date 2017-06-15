package org.ovirt.engine.core.common.action;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.VmRngDevice;

public class RngDeviceParameters extends ActionParametersBase {

    private boolean isVm;

    @NotNull(message = "ACTION_TYPE_RNG_MUST_BE_SPECIFIED")
    private VmRngDevice rngDevice;

    public RngDeviceParameters() {
        isVm = true;
    }

    public RngDeviceParameters(VmRngDevice rngDevice, boolean vm) {
        this.rngDevice = rngDevice;
        isVm = vm;
    }

    public VmRngDevice getRngDevice() {
        return rngDevice;
    }

    public boolean isVm() {
        return isVm;
    }

    public void setVm(boolean vm) {
        isVm = vm;
    }
}

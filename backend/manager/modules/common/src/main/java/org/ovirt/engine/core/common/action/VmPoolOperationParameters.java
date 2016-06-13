package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.VmPool;

public class VmPoolOperationParameters extends VmPoolParametersBase {
    private static final long serialVersionUID = -3290070106369322418L;

    @Valid
    private VmPool vmPool;

    public VmPoolOperationParameters() {
    }

    public VmPoolOperationParameters(VmPool vmPool) {
        super(vmPool.getVmPoolId());
        String description = vmPool.getVmPoolDescription();
        vmPool.setVmPoolDescription(description != null ? description : "");
        this.vmPool = vmPool;
    }

    public VmPool getVmPool() {
        return vmPool;
    }

    public void setVmPool(VmPool vmPool) {
        this.vmPool = vmPool;
    }

}

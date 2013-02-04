package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.VmPool;

public class VmPoolOperationParameters extends VmPoolParametersBase {
    private static final long serialVersionUID = -3290070106369322418L;
    @Valid
    private VmPool _vmPool;

    public VmPoolOperationParameters(VmPool vm_pools) {
        super(vm_pools.getVmPoolId());
        String tempVar = vm_pools.getVmPoolDescription();
        vm_pools.setVmPoolDescription((tempVar != null) ? tempVar : "");
        _vmPool = vm_pools;
    }

    public VmPool getVmPool() {
        return _vmPool;
    }

    public VmPoolOperationParameters() {
    }
}

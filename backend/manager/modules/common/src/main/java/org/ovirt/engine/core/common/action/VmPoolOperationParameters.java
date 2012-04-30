package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.vm_pools;

public class VmPoolOperationParameters extends VmPoolParametersBase {
    private static final long serialVersionUID = -3290070106369322418L;
    @Valid
    private vm_pools _vmPool;

    public VmPoolOperationParameters(vm_pools vm_pools) {
        super(vm_pools.getvm_pool_id());
        String tempVar = vm_pools.getvm_pool_description();
        vm_pools.setvm_pool_description((tempVar != null) ? tempVar : "");
        _vmPool = vm_pools;
    }

    public vm_pools getVmPool() {
        return _vmPool;
    }

    public VmPoolOperationParameters() {
    }
}

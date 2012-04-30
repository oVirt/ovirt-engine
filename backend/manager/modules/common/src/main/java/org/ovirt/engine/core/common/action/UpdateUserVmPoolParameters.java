package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;

public class UpdateUserVmPoolParameters extends VmPoolSimpleUserParameters {
    private static final long serialVersionUID = -95547459697168818L;
    private time_lease_vm_pool_map _userPoolMap;

    public UpdateUserVmPoolParameters(time_lease_vm_pool_map map) {
        super(map.getvm_pool_id(), map.getid());
        _userPoolMap = map;
    }

    public time_lease_vm_pool_map getUserPoolMap() {
        return _userPoolMap;
    }

    public UpdateUserVmPoolParameters() {
    }
}

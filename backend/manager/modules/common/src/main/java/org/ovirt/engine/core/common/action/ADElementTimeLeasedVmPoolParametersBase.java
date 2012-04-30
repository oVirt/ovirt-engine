package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;

public class ADElementTimeLeasedVmPoolParametersBase extends VmPoolToAdElementParameters {
    private static final long serialVersionUID = -1930036899201922902L;
    private time_lease_vm_pool_map _map;

    public ADElementTimeLeasedVmPoolParametersBase(time_lease_vm_pool_map map) {
        super(map.getid(), map.getvm_pool_id());
        _map = map;
    }

    public time_lease_vm_pool_map getTimeLeasedVmPoolMap() {
        return _map;
    }

    public ADElementTimeLeasedVmPoolParametersBase() {
    }
}

package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.users.*;

public class AttachUserToTimeLeasedPoolParameters extends VmPoolUserParameters {
    private static final long serialVersionUID = -2625973275633657557L;
    private time_lease_vm_pool_map _userPoolMap;

    public AttachUserToTimeLeasedPoolParameters(Guid vmPoolId, VdcUser user, time_lease_vm_pool_map map) {
        super(vmPoolId, user, false);
        _userPoolMap = map;
    }

    public time_lease_vm_pool_map getUserPoolMap() {
        return _userPoolMap;
    }

    public AttachUserToTimeLeasedPoolParameters() {
    }
}

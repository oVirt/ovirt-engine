package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;

public class AttachAdGroupTimeLeasedPoolCommandParameters extends ADElementTimeLeasedVmPoolParametersBase {
    private static final long serialVersionUID = 484523812183776047L;
    private ad_groups _adGroup;

    public AttachAdGroupTimeLeasedPoolCommandParameters(ad_groups group, time_lease_vm_pool_map map) {
        super(map);
        _adGroup = group;
    }

    public ad_groups getAdGroup() {
        return _adGroup;
    }

    public AttachAdGroupTimeLeasedPoolCommandParameters() {
    }
}

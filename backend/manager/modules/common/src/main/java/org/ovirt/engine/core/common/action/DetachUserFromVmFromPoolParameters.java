package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class DetachUserFromVmFromPoolParameters extends VmPoolUserParameters {

    private static final long serialVersionUID = 2255305819152411560L;

    private boolean restoreStateless;
    public DetachUserFromVmFromPoolParameters(Guid vmPoolId, Guid userId, Guid vmId, boolean restoreStateless) {
        super(vmPoolId, userId, vmId);
        this.restoreStateless = restoreStateless;
    }

    DetachUserFromVmFromPoolParameters() {
        super();
    }

    public boolean getIsRestoreStateless() {
        return restoreStateless;
    }
}

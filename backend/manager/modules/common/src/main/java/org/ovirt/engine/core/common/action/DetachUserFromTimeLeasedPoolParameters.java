package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;

public class DetachUserFromTimeLeasedPoolParameters extends VmPoolSimpleUserParameters {
    private static final long serialVersionUID = 7826859699180843171L;
    private boolean _isInternal;

    public DetachUserFromTimeLeasedPoolParameters(Guid vmPoolId, Guid userId, boolean isInternal) {
        super(vmPoolId, userId);
        _isInternal = isInternal;
    }

    public boolean getIsInternal() {
        return _isInternal;
    }

    public DetachUserFromTimeLeasedPoolParameters() {
    }
}

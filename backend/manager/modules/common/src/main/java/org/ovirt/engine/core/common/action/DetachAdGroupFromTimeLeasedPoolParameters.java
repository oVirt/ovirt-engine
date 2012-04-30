package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;

public class DetachAdGroupFromTimeLeasedPoolParameters extends VmPoolToAdElementParameters {
    private static final long serialVersionUID = -6203708281726089292L;
    private boolean _isInternal;

    public DetachAdGroupFromTimeLeasedPoolParameters(Guid adElementId, Guid vmPoolId, boolean isInternal) {
        super(adElementId, vmPoolId);
        _isInternal = isInternal;
    }

    public boolean getIsInternal() {
        return _isInternal;
    }

    public DetachAdGroupFromTimeLeasedPoolParameters() {
    }
}

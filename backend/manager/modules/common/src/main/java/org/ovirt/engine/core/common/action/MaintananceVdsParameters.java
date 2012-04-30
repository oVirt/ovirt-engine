package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;

public class MaintananceVdsParameters extends VdsActionParameters {
    private static final long serialVersionUID = -962696566094119431L;
    private boolean _isInternal;

    public MaintananceVdsParameters(Guid vdsId, boolean isInternal) {
        super(vdsId);
        _isInternal = isInternal;
    }

    public boolean getIsInternal() {
        return _isInternal;
    }

    public MaintananceVdsParameters() {
    }
}

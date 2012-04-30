package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;

public class VmPoolToAdElementParameters extends AdElementParametersBase {
    private static final long serialVersionUID = 8877429811876287907L;
    private Guid _vmPoolId;

    public VmPoolToAdElementParameters(Guid adElementId, Guid vmPoolId) {
        super(adElementId);
        _vmPoolId = vmPoolId;
    }

    public Guid getVmPoolId() {
        return _vmPoolId;
    }

    public VmPoolToAdElementParameters() {
    }
}

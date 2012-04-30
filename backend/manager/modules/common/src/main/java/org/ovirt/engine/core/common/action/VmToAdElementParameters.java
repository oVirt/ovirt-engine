package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;

public class VmToAdElementParameters extends AdElementParametersBase {
    private static final long serialVersionUID = -7146905191066527540L;
    private Guid _vmId = new Guid();

    public VmToAdElementParameters(Guid adElementId, Guid vmId) {
        super(adElementId);
        _vmId = vmId;
    }

    public Guid getVmId() {
        return _vmId;
    }

    public VmToAdElementParameters() {
    }
}

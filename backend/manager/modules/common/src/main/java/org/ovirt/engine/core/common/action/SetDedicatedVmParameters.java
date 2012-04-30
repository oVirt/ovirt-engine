package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;

public class SetDedicatedVmParameters extends VmOperationParameterBase {
    private static final long serialVersionUID = 8019813360303861204L;
    private Guid _vdsId;

    public SetDedicatedVmParameters(Guid vmId, Guid vdsId) {
        super(vmId);
        _vdsId = vdsId;
    }

    public Guid getVdsId() {
        return _vdsId;
    }

    public SetDedicatedVmParameters() {
    }
}

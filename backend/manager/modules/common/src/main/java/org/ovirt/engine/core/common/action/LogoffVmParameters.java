package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;

public class LogoffVmParameters extends VmOperationParameterBase {
    private static final long serialVersionUID = 937720357356448415L;
    private boolean _force;

    public LogoffVmParameters(Guid vmId, boolean force) {
        super(vmId);
        _force = force;
    }

    public boolean getForce() {
        return _force;
    }

    public LogoffVmParameters() {
    }
}

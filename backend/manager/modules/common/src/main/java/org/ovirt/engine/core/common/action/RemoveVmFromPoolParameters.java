package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;

public class RemoveVmFromPoolParameters extends VmPoolParametersBase {
    private static final long serialVersionUID = -9051384517712295806L;
    private Guid _vmId = new Guid();

    public RemoveVmFromPoolParameters(Guid vmId) {
        super(Guid.Empty);
        _vmId = vmId;
    }

    public Guid getVmId() {
        return _vmId;
    }

    public RemoveVmFromPoolParameters() {
    }
}

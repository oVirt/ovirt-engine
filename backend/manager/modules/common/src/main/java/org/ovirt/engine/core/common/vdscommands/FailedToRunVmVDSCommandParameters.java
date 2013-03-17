package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class FailedToRunVmVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    public FailedToRunVmVDSCommandParameters(Guid vdsId) {
        super(vdsId);
    }

    public FailedToRunVmVDSCommandParameters() {
    }
}

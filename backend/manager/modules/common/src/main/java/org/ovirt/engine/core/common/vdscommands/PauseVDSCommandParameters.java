package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class PauseVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    public PauseVDSCommandParameters(Guid vdsId, Guid vmId) {
        super(vdsId, vmId);
    }

    public PauseVDSCommandParameters() {
    }
}

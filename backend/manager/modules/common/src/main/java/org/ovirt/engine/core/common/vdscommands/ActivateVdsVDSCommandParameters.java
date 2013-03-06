package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class ActivateVdsVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    public ActivateVdsVDSCommandParameters(Guid vdsId) {
        super(vdsId);
    }

    public ActivateVdsVDSCommandParameters() {
    }
}

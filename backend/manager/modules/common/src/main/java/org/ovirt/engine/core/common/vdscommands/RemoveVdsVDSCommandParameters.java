package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class RemoveVdsVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    public RemoveVdsVDSCommandParameters(Guid vdsId) {
        super(vdsId);
    }

    public RemoveVdsVDSCommandParameters() {
    }
}

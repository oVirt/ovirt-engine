package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class AddVdsVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    public AddVdsVDSCommandParameters(Guid vdsId) {
        super(vdsId);
    }

    public AddVdsVDSCommandParameters() {
    }
}

package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class UpdateVdsVMsClearedVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    public UpdateVdsVMsClearedVDSCommandParameters(Guid vdsId) {
        super(vdsId);
    }

    public UpdateVdsVMsClearedVDSCommandParameters() {
    }
}

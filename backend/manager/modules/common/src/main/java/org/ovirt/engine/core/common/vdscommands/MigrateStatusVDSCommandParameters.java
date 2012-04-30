package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class MigrateStatusVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
     public MigrateStatusVDSCommandParameters(Guid vdsId, Guid vmId) {
        super(vdsId, vmId);
    }

    public MigrateStatusVDSCommandParameters() {
    }
}

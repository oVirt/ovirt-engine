package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class ResumeVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    public ResumeVDSCommandParameters(Guid vdsId, Guid vmId) {
        super(vdsId, vmId);
    }

    public ResumeVDSCommandParameters() {
    }
}

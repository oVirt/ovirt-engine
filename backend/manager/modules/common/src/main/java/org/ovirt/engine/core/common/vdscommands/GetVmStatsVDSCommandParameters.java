package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class GetVmStatsVDSCommandParameters extends VdsAndVmIDVDSParametersBase {

    public GetVmStatsVDSCommandParameters(Guid vdsId, Guid vmId) {
        super(vdsId, vmId);
    }

    public GetVmStatsVDSCommandParameters() {
    }

}

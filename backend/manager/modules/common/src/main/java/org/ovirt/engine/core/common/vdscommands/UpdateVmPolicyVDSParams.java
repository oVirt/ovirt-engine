package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class UpdateVmPolicyVDSParams extends VdsAndVmIDVDSParametersBase {

    private int cpuLimit;

    private UpdateVmPolicyVDSParams() {
    }

    public UpdateVmPolicyVDSParams(Guid vdsId, Guid vmId, int cpuLimit) {
        super(vdsId, vmId);
        this.cpuLimit = cpuLimit;
    }

    public int getCpuLimit() {
        return cpuLimit;
    }

}

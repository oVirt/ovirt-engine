package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class VmSlaPolicyParameters extends VmManagementParametersBase {

    private static final long serialVersionUID = 3918909396931144459L;
    private int cpuLimit;

    public VmSlaPolicyParameters() {
    }

    public VmSlaPolicyParameters(Guid vmId, int cpuLimit) {
        setVmId(vmId);
        this.cpuLimit = cpuLimit;
    }

    public int getCpuLimit() {
        return cpuLimit;
    }

}

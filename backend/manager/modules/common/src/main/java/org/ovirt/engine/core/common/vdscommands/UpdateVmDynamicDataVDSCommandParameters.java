package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.compat.Guid;

public class UpdateVmDynamicDataVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private VmDynamic privateVmDynamic;

    public VmDynamic getVmDynamic() {
        return privateVmDynamic;
    }

    private void setVmDynamic(VmDynamic value) {
        privateVmDynamic = value;
    }

    public UpdateVmDynamicDataVDSCommandParameters(Guid vdsId, VmDynamic vmDynamic) {
        super(vdsId);
        setVmDynamic(vmDynamic);
    }

    public UpdateVmDynamicDataVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, vmDynamic=%s", super.toString(), getVmDynamic());
    }
}

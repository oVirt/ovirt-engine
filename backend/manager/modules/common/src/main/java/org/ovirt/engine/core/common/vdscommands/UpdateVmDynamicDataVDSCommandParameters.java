package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VmDynamic;

public class UpdateVmDynamicDataVDSCommandParameters extends VdsAndVmIDVDSParametersBase{
    private VmDynamic privateVmDynamic;

    public VmDynamic getVmDynamic() {
        return privateVmDynamic;
    }

    private void setVmDynamic(VmDynamic value) {
        privateVmDynamic = value;
    }

    public UpdateVmDynamicDataVDSCommandParameters(VmDynamic vmDynamic) {
        setVmDynamic(vmDynamic);
    }

    public UpdateVmDynamicDataVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, vmDynamic=%s", super.toString(), getVmDynamic());
    }
}

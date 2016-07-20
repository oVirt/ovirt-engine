package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class UpdateVmDynamicDataVDSCommandParameters extends VdsAndVmIDVDSParametersBase{
    private VmDynamic privateVmDynamic;

    public VmDynamic getVmDynamic() {
        return privateVmDynamic;
    }

    private void setVmDynamic(VmDynamic value) {
        privateVmDynamic = value;
    }

    public UpdateVmDynamicDataVDSCommandParameters(VmDynamic vmDynamic) {
        super(null, vmDynamic.getId());
        setVmDynamic(vmDynamic);
    }

    public UpdateVmDynamicDataVDSCommandParameters() {
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("vmDynamic", getVmDynamic());
    }
}

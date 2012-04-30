package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VdsDynamic;

public class UpdateVdsDynamicDataVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private VdsDynamic privateVdsDynamic;

    public VdsDynamic getVdsDynamic() {
        return privateVdsDynamic;
    }

    private void setVdsDynamic(VdsDynamic value) {
        privateVdsDynamic = value;
    }

    public UpdateVdsDynamicDataVDSCommandParameters(VdsDynamic vdsDynamic) {
        super(vdsDynamic.getId());
        setVdsDynamic(vdsDynamic);
    }

    public UpdateVdsDynamicDataVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, vdsDynamic=%s", super.toString(), getVdsDynamic());
    }
}

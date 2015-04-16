package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class VdsIdAndVdsVDSCommandParametersBase extends VdsIdVDSCommandParametersBase {
    private VDS privateVds;

    public VDS getVds() {
        return privateVds;
    }

    public void setVds(VDS value) {
        privateVds = value;
    }

    public VdsIdAndVdsVDSCommandParametersBase(VDS vds) {
        super(vds.getId());
        setVds(vds);
    }

    public VdsIdAndVdsVDSCommandParametersBase() {
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("vds", getVds());
    }
}

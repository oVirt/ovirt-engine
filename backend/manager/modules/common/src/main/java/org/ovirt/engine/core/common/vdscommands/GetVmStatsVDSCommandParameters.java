package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.Guid;

public class GetVmStatsVDSCommandParameters extends VdsAndVmIDVDSParametersBase {

    private VDS vds;

    public GetVmStatsVDSCommandParameters(VDS vds, Guid vmId) {
        super(vds.getvds_id(), vmId);
        this.vds = vds;
    }

    /**
     * @return the vds
     */
    public VDS getVds() {
        return vds;
    }
}

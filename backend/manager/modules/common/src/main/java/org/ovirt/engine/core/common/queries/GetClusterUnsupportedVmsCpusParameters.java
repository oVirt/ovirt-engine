package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetClusterUnsupportedVmsCpusParameters extends VdcQueryParametersBase {

    private static final long serialVersionUID = -8663922280262494306L;

    private Guid vdsGroupId;
    private String newCpuName;

    public GetClusterUnsupportedVmsCpusParameters(Guid vdsGroupId, String newCpuName) {
        this.vdsGroupId = vdsGroupId;
        this.newCpuName = newCpuName;
    }

    public GetClusterUnsupportedVmsCpusParameters() {
    }

    public Guid getVdsGroupId() {
        return vdsGroupId;
    }

    public String getNewCpuName() {
        return newCpuName;
    }
}

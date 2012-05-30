package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetAllVmSnapshotsByVmIdParameters extends VdcQueryParametersBase {

    private static final long serialVersionUID = -1357252664326554046L;

    private Guid vmId = new Guid();

    public GetAllVmSnapshotsByVmIdParameters() {
    }

    public GetAllVmSnapshotsByVmIdParameters(Guid vmId) {
        this.vmId = vmId;
    }

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }
}

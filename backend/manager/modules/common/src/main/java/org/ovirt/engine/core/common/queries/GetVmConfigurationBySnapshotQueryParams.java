package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetVmConfigurationBySnapshotQueryParams extends VdcQueryParametersBase {

    private Guid snapshotId;

    public GetVmConfigurationBySnapshotQueryParams(Guid snapshotId) {
        this.snapshotId = snapshotId;
    }

    public Guid getSnapshotId() {
        return snapshotId;
    }
    public void setSnapshotId(Guid snapshotId) {
        this.snapshotId = snapshotId;
    }
}

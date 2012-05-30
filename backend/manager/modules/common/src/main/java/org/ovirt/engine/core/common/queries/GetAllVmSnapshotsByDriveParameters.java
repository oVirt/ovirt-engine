package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

@Deprecated
public class GetAllVmSnapshotsByDriveParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -3768508134295864787L;

    public GetAllVmSnapshotsByDriveParameters(Guid id, String drive) {
        _id = id;
        _drive = drive;
    }

    private Guid _id = new Guid();
    private String _drive;

    public Guid getId() {
        return _id;
    }

    public String getDrive() {
        return _drive;
    }

    public GetAllVmSnapshotsByDriveParameters() {
    }
}

package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetAllVmSnapshotsParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -788238010985994689L;

    public GetAllVmSnapshotsParameters(Guid id) {
        _id = id;
    }

    private Guid _id = new Guid();

    public Guid getId() {
        return _id;
    }

    public GetAllVmSnapshotsParameters() {
    }
}

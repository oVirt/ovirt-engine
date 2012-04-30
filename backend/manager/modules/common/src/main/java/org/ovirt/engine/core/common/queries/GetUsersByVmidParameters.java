package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetUsersByVmidParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 8439269144799226825L;

    public GetUsersByVmidParameters(Guid id) {
        _id = id;
    }

    private Guid _id = new Guid();

    public Guid getVmId() {
        return _id;
    }

    public GetUsersByVmidParameters() {
    }
}

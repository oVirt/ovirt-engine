package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetVmsByUseridParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -5292104747871231090L;

    public GetVmsByUseridParameters(Guid id) {
        _id = id;
    }

    private Guid _id = new Guid();

    public Guid getUserId() {
        return _id;
    }

    public GetVmsByUseridParameters() {
    }
}

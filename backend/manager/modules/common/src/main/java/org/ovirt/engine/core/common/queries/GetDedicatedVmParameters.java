package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetDedicatedVmParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 1967442501042226669L;

    public GetDedicatedVmParameters(Guid id) {
        _id = id;
    }

    private Guid _id;

    public Guid getId() {
        return _id;
    }

    public GetDedicatedVmParameters() {
    }
}

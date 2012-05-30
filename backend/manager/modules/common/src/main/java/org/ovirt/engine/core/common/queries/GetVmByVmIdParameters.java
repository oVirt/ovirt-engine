package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetVmByVmIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -3232978226860645746L;

    public GetVmByVmIdParameters(Guid id) {
        _id = id;
    }

    private Guid _id = new Guid();

    public Guid getId() {
        return _id;
    }

    public GetVmByVmIdParameters() {
    }
}

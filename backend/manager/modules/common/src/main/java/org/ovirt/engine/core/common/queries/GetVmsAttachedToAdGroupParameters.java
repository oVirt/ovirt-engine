package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetVmsAttachedToAdGroupParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 6577838883414480497L;

    public GetVmsAttachedToAdGroupParameters(Guid id) {
        _id = id;
    }

    private Guid _id = new Guid();

    public Guid getId() {
        return _id;
    }

    public GetVmsAttachedToAdGroupParameters() {
    }
}

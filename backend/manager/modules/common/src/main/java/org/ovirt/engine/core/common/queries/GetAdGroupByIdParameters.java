package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetAdGroupByIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -6861829313936687098L;

    public GetAdGroupByIdParameters(Guid id) {
        _id = id;
    }

    private Guid _id = new Guid();

    public Guid getId() {
        return _id;
    }

    public GetAdGroupByIdParameters() {
    }
}

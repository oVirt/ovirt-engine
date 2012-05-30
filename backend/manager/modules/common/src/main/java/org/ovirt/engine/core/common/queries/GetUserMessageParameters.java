package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetUserMessageParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 6354989284577351155L;

    public GetUserMessageParameters(Guid id) {
        _id = id;
    }

    private Guid _id = new Guid();

    public Guid getId() {
        return _id;
    }

    public GetUserMessageParameters() {
    }
}

package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetMessagesByIdParametersBase extends VdcQueryParametersBase {
    private static final long serialVersionUID = -4356340634971511306L;

    public GetMessagesByIdParametersBase(Guid id) {
        _id = id;
    }

    private Guid _id = new Guid();

    public Guid getId() {
        return _id;
    }

    public GetMessagesByIdParametersBase() {
    }
}

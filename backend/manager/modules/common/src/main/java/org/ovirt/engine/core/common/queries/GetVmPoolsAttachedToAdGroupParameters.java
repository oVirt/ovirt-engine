package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetVmPoolsAttachedToAdGroupParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 1760314255980107591L;

    public GetVmPoolsAttachedToAdGroupParameters(Guid id) {
        _id = id;
    }

    private Guid _id = new Guid();

    public Guid getId() {
        return _id;
    }

    public GetVmPoolsAttachedToAdGroupParameters() {
    }
}

package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetVmsRunningOnVDSParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 2940416694903953509L;

    public GetVmsRunningOnVDSParameters(Guid id) {
        _id = id;
    }

    private Guid _id;

    public Guid getId() {
        return _id;
    }

    public GetVmsRunningOnVDSParameters() {
    }
}

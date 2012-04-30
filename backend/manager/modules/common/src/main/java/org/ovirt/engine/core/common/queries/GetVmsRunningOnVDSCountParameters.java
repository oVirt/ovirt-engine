package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetVmsRunningOnVDSCountParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 2045250638895753689L;

    public GetVmsRunningOnVDSCountParameters(Guid id) {
        _id = id;
    }

    private Guid _id;

    public Guid getId() {
        return _id;
    }

    public GetVmsRunningOnVDSCountParameters() {
    }
}

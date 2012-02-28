package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetAllVmPoolsAttachedToUserParameters extends VdcUserQueryParametersBase {
    private static final long serialVersionUID = -6835998142489522597L;

    public GetAllVmPoolsAttachedToUserParameters() {
    }

    public GetAllVmPoolsAttachedToUserParameters(Guid userId) {
        super(userId);
    }
}

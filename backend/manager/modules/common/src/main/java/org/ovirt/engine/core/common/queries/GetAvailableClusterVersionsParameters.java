package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetAvailableClusterVersionsParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -4823052256384638102L;
    private Guid privateVdsGroupId;

    public Guid getVdsGroupId() {
        return privateVdsGroupId;
    }

    public void setVdsGroupId(Guid value) {
        privateVdsGroupId = value;
    }

    public GetAvailableClusterVersionsParameters() {
    }
}

package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetVmdataByPoolIdParameters extends VdcQueryParametersBase {

    private static final long serialVersionUID = 6054241300085801302L;

    private Guid id = new Guid();

    public GetVmdataByPoolIdParameters(Guid vmPoolId) {
        this.id = vmPoolId;
    }

    public Guid getId() {
        return id;
    }
}

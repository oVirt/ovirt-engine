package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetVmsRunningOnOrMigratingToVdsParameters extends VdcQueryParametersBase {

    private static final long serialVersionUID = 5129010056629518621L;

    private Guid id;

    public GetVmsRunningOnOrMigratingToVdsParameters() {
    }

    public GetVmsRunningOnOrMigratingToVdsParameters(Guid id) {
        this.id = id;
    }

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }

}

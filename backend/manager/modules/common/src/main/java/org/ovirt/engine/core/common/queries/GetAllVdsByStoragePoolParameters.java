package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetAllVdsByStoragePoolParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 8729202396958351850L;

    public GetAllVdsByStoragePoolParameters() {
    }

    public GetAllVdsByStoragePoolParameters(Guid spId) {
        this.spId = spId;
    }

    private Guid spId;

    public Guid getStoragePoolId() {
        return spId;
    }
}

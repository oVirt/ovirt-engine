package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetVmPoolByIdParametersBase extends VdcQueryParametersBase {
    private static final long serialVersionUID = -4229590551595438086L;

    public GetVmPoolByIdParametersBase(Guid poolId) {
        _poolId = poolId;
    }

    private Guid _poolId;

    public Guid getPoolId() {
        return _poolId;
    }

    public GetVmPoolByIdParametersBase() {
    }
}

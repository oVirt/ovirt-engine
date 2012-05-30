package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetVmPoolsMapByVmPoolIdParameters extends GetVmPoolByIdParametersBase {
    private static final long serialVersionUID = -2006515844211778283L;

    public GetVmPoolsMapByVmPoolIdParameters(Guid poolId) {
        super(poolId);
    }

    public GetVmPoolsMapByVmPoolIdParameters() {
    }
}

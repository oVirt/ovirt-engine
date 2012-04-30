package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetUserVmByUserIdAndByVmGuidParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 3575536561865155699L;

    public GetUserVmByUserIdAndByVmGuidParameters(Guid userId, Guid vmId) {
        _userId = userId;
        _vmId = vmId;
    }

    private Guid _userId = new Guid();
    private Guid _vmId = new Guid();

    public Guid getUserId() {
        return _userId;
    }

    public Guid getVmId() {
        return _vmId;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetUserVmByUserIdAndByVmGuidParameters() {
    }
}

package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetVdsByVdsIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -8412934198071264334L;

    public GetVdsByVdsIdParameters(Guid vdsId) {
        _vdsId = vdsId;
    }

    private Guid _vdsId;

    public Guid getVdsId() {
        return _vdsId;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        switch (queryType) {
        case GetVdsInterfacesByVdsId:
            return RegisterableQueryReturnDataType.LIST_IQUERYABLE;
        default:
            return RegisterableQueryReturnDataType.IQUERYABLE;
        }
    }

    public GetVdsByVdsIdParameters() {
    }
}

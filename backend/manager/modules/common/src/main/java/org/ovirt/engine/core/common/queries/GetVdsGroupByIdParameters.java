package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetVdsGroupByIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 5426689982548074276L;

    public GetVdsGroupByIdParameters(Guid vdsId) {
        _vdsId = vdsId;
    }

    private Guid _vdsId;

    public Guid getVdsId() {
        return _vdsId;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.IQUERYABLE;
    }

    public GetVdsGroupByIdParameters() {
    }
}

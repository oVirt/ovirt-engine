package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetDbUserByUserIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -940823391332892259L;

    public GetDbUserByUserIdParameters(Guid id) {
        _id = id;
    }

    private Guid _id = new Guid();

    public Guid getUserId() {
        return _id;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.IQUERYABLE;
    }

    public GetDbUserByUserIdParameters() {
    }
}

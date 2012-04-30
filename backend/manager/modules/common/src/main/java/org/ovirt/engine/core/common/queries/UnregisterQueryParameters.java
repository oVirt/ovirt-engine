package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class UnregisterQueryParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 1416061531708794625L;

    private Guid privateQueryID = new Guid();

    public Guid getQueryID() {
        return privateQueryID;
    }

    protected void setQueryID(Guid value) {
        privateQueryID = value;
    }

    public UnregisterQueryParameters(Guid queryID) {
        setQueryID(queryID);
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public UnregisterQueryParameters() {
    }
}

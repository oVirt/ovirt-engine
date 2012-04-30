package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class RegisterQueryParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -1794521524205136814L;

    private Guid privateQueryID = new Guid();

    public Guid getQueryID() {
        return privateQueryID;
    }

    protected void setQueryID(Guid value) {
        privateQueryID = value;
    }

    private VdcQueryType privateQueryType = VdcQueryType.forValue(0);

    public VdcQueryType getQueryType() {
        return privateQueryType;
    }

    protected void setQueryType(VdcQueryType value) {
        privateQueryType = value;
    }

    private VdcQueryParametersBase privateQueryParams;

    public VdcQueryParametersBase getQueryParams() {
        return privateQueryParams;
    }

    protected void setQueryParams(VdcQueryParametersBase value) {
        privateQueryParams = value;
    }

    public RegisterQueryParameters(Guid queryID, VdcQueryType queryType, VdcQueryParametersBase queryParams) {
        setQueryID(queryID);
        setQueryType(queryType);
        setQueryParams(queryParams);
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public RegisterQueryParameters() {
    }
}

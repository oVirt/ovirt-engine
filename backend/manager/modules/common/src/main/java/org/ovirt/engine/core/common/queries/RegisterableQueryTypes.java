package org.ovirt.engine.core.common.queries;

public final class RegisterableQueryTypes {
    public static RegisterableQueryReturnDataType GetReturnedDataTypeByQueryType(VdcQueryType queryType,
            VdcQueryParametersBase queryParams) {
        if (queryType == VdcQueryType.Search) {
            return RegisterableQueryReturnDataType.SEARCH;
        }

        return queryParams.GetReturnedDataTypeByVdcQueryType(queryType);
    }

    public RegisterableQueryTypes() {
    }
}

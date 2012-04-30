package org.ovirt.engine.core.common.queries;

public class GetResourceUsageParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 1804179976690860124L;

    public GetResourceUsageParameters(String resourceName) {
        _resourceName = resourceName;
    }

    private String _resourceName;

    public String getResourceName() {
        return _resourceName;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetResourceUsageParameters() {
    }
}

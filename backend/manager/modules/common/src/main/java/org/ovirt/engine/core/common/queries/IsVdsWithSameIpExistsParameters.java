package org.ovirt.engine.core.common.queries;

public class IsVdsWithSameIpExistsParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 3967554280743440995L;

    public IsVdsWithSameIpExistsParameters(String ipAddress) {
        _ipAddress = ipAddress;
    }

    private String _ipAddress;

    public String getIpAddress() {
        return _ipAddress;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public IsVdsWithSameIpExistsParameters() {
    }
}

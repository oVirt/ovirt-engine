package org.ovirt.engine.core.common.queries;

public class GetPowerClientByClientInfoParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 1049519488813110436L;

    public GetPowerClientByClientInfoParameters(String clientIp) {
        _clientIp = clientIp;
    }

    private String _clientIp;

    public String getClientIp() {
        return _clientIp;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.IQUERYABLE;
    }

    public GetPowerClientByClientInfoParameters() {
    }
}

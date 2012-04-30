package org.ovirt.engine.core.common.queries;

public class IsVdsWithSameNameExistParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 7065542492217271851L;

    public IsVdsWithSameNameExistParameters(String vdsName) {
        _vdsName = vdsName;
    }

    private String _vdsName;

    public String getVmName() {
        return _vdsName;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public IsVdsWithSameNameExistParameters() {
    }
}

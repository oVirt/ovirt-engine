package org.ovirt.engine.core.common.queries;

public class IsVmTemlateWithSameNameExistParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -6485460471694366238L;

    public IsVmTemlateWithSameNameExistParameters(String name) {
        _name = name;
    }

    private String _name;

    public String getName() {
        return _name;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public IsVmTemlateWithSameNameExistParameters() {
    }
}

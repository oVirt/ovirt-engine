package org.ovirt.engine.core.common.queries;

public class IsVmPoolWithSameNameExistsParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -3533344997052757929L;

    public IsVmPoolWithSameNameExistsParameters(String vmPoolName) {
        _vmPoolName = vmPoolName;
    }

    private String _vmPoolName;

    public String getVmPoolName() {
        return _vmPoolName;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public IsVmPoolWithSameNameExistsParameters() {
    }
}

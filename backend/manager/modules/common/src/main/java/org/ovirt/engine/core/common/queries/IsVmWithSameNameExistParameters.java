package org.ovirt.engine.core.common.queries;

public class IsVmWithSameNameExistParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -329586454172262561L;

    public IsVmWithSameNameExistParameters(String vmName) {
        _vmName = vmName;
    }

    private String _vmName;

    public String getVmName() {
        return _vmName;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public IsVmWithSameNameExistParameters() {
    }
}

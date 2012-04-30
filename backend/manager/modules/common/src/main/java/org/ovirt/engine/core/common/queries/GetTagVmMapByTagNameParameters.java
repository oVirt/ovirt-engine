package org.ovirt.engine.core.common.queries;

public class GetTagVmMapByTagNameParameters extends GetTagByTagNameParametersBase {
    private static final long serialVersionUID = -3851616645160264609L;

    public GetTagVmMapByTagNameParameters(String tagName) {
        super(tagName);
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetTagVmMapByTagNameParameters() {
    }
}

package org.ovirt.engine.core.common.queries;

public class GetTagUserMapByTagNameParameters extends GetTagByTagNameParametersBase {
    private static final long serialVersionUID = -338757167090549338L;

    public GetTagUserMapByTagNameParameters(String tagName) {
        super(tagName);
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetTagUserMapByTagNameParameters() {
    }
}

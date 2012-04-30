package org.ovirt.engine.core.common.queries;

public class GetTagByTagNameParameters extends GetTagByTagNameParametersBase {
    private static final long serialVersionUID = -3972142919748561620L;

    public GetTagByTagNameParameters(String tagName) {
        super(tagName);
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetTagByTagNameParameters() {
    }
}

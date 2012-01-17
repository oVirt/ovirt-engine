package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetTagUserGroupMapByTagNameParameters")
public class GetTagUserGroupMapByTagNameParameters extends GetTagByTagNameParametersBase {
    private static final long serialVersionUID = -4848218011737609408L;

    public GetTagUserGroupMapByTagNameParameters(String tagName) {
        super(tagName);
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetTagUserGroupMapByTagNameParameters() {
    }
}

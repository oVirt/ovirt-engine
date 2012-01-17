package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetTagVdsMapByTagNameParameters")
public class GetTagVdsMapByTagNameParameters extends GetTagByTagNameParametersBase {
    private static final long serialVersionUID = -2896819836634242313L;

    public GetTagVdsMapByTagNameParameters(String tagName) {
        super(tagName);
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetTagVdsMapByTagNameParameters() {
    }
}

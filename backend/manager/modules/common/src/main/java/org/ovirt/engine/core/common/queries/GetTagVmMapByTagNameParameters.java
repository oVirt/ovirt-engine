package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetTagVmMapByTagNameParameters")
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

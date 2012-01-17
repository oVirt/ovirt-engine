package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetTagsByUserIdParameters")
public class GetTagsByUserIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -6658285833163439520L;

    public GetTagsByUserIdParameters(String userId) {
        _userId = userId;
    }

    @XmlElement(name = "UserId")
    private String _userId;

    public String getUserId() {
        return _userId;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetTagsByUserIdParameters() {
    }
}

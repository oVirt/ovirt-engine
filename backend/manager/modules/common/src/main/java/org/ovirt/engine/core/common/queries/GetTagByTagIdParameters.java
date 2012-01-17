package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetTagByTagIdParameters")
public class GetTagByTagIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -4371399673035908432L;

    public GetTagByTagIdParameters(Guid tagId) {
        _tagId = tagId;
    }

    @XmlElement(name = "TagId")
    private Guid _tagId;

    public Guid getTagId() {
        return _tagId;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetTagByTagIdParameters() {
    }
}

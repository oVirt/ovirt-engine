package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetTagByTagNameParametersBase")
public class GetTagByTagNameParametersBase extends VdcQueryParametersBase {
    private static final long serialVersionUID = -4620515574262550994L;

    public GetTagByTagNameParametersBase(String tagName) {
        _tagName = tagName;
    }

    @XmlElement(name = "TagName")
    private String _tagName;

    public String getTagName() {
        return _tagName;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetTagByTagNameParametersBase() {
    }
}

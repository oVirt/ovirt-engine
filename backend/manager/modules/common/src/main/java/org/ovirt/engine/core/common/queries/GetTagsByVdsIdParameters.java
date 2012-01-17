package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetTagsByVdsIdParameters")
public class GetTagsByVdsIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 2616882989867228100L;

    public GetTagsByVdsIdParameters(String vdsId) {
        _vdsId = vdsId;
    }

    @XmlElement(name = "VdsId")
    private String _vdsId;

    public String getVdsId() {
        return _vdsId;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetTagsByVdsIdParameters() {
    }
}

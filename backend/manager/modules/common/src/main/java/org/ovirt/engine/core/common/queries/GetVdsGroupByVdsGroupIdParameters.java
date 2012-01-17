package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetVdsGroupByVdsGroupIdParameters")
public class GetVdsGroupByVdsGroupIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -3812474779897884090L;

    public GetVdsGroupByVdsGroupIdParameters(Guid vdsGroupId) {
        _vdsGroupId = vdsGroupId;
    }

    @XmlElement(name = "VdsGroupId")
    private Guid _vdsGroupId;

    public Guid getVdsGroupId() {
        return _vdsGroupId;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.IQUERYABLE;
    }

    public GetVdsGroupByVdsGroupIdParameters() {
    }
}

package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetVdsByVdsIdParameters")
public class GetVdsByVdsIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -8412934198071264334L;

    public GetVdsByVdsIdParameters(Guid vdsId) {
        _vdsId = vdsId;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "VdsId")
    private Guid _vdsId;

    public Guid getVdsId() {
        return _vdsId;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        switch (queryType) {
        case GetVdsInterfacesByVdsId:
            return RegisterableQueryReturnDataType.LIST_IQUERYABLE;
        default:
            return RegisterableQueryReturnDataType.IQUERYABLE;
        }
    }

    public GetVdsByVdsIdParameters() {
    }
}

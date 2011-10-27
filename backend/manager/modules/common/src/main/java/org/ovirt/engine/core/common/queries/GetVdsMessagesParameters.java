package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetVdsMessagesParameters")
public class GetVdsMessagesParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -7522465676706160504L;

    public GetVdsMessagesParameters(int vdsId) {
        _vdsId = vdsId;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "VdsId")
    private int _vdsId;

    public int getVdsId() {
        return _vdsId;
    }

    public GetVdsMessagesParameters() {
    }
}

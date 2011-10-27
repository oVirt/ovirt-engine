package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetDeviceInfoVDSCommandParameters")
public class GetDeviceInfoVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    public GetDeviceInfoVDSCommandParameters(Guid vdsId, String lunId) {
        super(vdsId);
        setLUNID(lunId);
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "LUNID")
    private String privateLUNID;

    public String getLUNID() {
        return privateLUNID;
    }

    private void setLUNID(String value) {
        privateLUNID = value;
    }

    public GetDeviceInfoVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, LUNID=%s", super.toString(), getLUNID());
    }

}

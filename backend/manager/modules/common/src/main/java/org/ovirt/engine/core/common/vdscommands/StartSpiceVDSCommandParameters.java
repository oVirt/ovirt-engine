package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "StartSpiceVDSCommandParameters")
public class StartSpiceVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    private String _vdsIp;
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    private String _ticket;
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    private int _guestPort;

    public StartSpiceVDSCommandParameters(Guid vdsId, String vdsIp, int guestPort, String ticket) {
        super(vdsId);
        _vdsIp = vdsIp;
        _ticket = ticket;
        _guestPort = guestPort;
    }

    public String getTicket() {
        return _ticket;
    }

    public int getGuestPort() {
        return _guestPort;
    }

    public String getVdsIp() {
        return _vdsIp;
    }

    public StartSpiceVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, ticket=%s, guestPort=%s, vdsIp=%s",
                super.toString(),
                getTicket(),
                getGuestPort(),
                getVdsIp());
    }
}

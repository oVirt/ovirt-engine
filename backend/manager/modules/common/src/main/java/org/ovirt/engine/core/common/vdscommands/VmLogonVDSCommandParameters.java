package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmLogonVDSCommandParameters")
public class VmLogonVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    private String _domain;
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    private String _password;
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    private String _userName;

    public VmLogonVDSCommandParameters(Guid vdsId, Guid vmId, String domain, String userName, String password) {
        super(vdsId, vmId);
        _domain = domain;
        _password = password;
        _userName = userName;
    }

    public String getDomain() {
        return _domain;
    }

    public String getPassword() {
        return _password;
    }

    public String getUserName() {
        return _userName;
    }

    public VmLogonVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, domain=%s, password=%s, userName=%s",
                super.toString(),
                getDomain(),
                getPassword() == null ? null : "******",
                getUserName());
    }
}

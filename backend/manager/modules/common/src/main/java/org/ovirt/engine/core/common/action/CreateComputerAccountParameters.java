package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "CreateComputerAccountParameters")
public class CreateComputerAccountParameters extends VdcActionParametersBase implements java.io.Serializable {

    private static final long serialVersionUID = 5715983315771665602L;

    @XmlElement
    private String _path;

    @XmlElement(name = "VmId")
    private Guid _vmId = new Guid();

    @XmlElement
    private String _userName;

    @XmlElement
    private String _userPassword;

    @XmlElement
    private String _domain;

    public CreateComputerAccountParameters(String path, Guid vmId, String userName, String userPassword, String domain) {
        _path = path;
        _vmId = vmId;
        _userName = userName;
        _userPassword = userPassword;
        _domain = domain;
    }

    public String getPath() {
        return _path;
    }

    public Guid getVmId() {
        return _vmId;
    }

    public String getUserName() {
        return _userName;
    }

    public String getUserPassword() {
        return _userPassword;
    }

    public String getDomain() {
        return _domain;
    }

    public CreateComputerAccountParameters() {
    }
}

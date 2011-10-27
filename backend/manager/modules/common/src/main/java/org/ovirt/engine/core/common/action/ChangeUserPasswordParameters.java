package org.ovirt.engine.core.common.action;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ChangeUserPasswordParameters")
public class ChangeUserPasswordParameters extends VdcActionParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = -1289433335701261260L;

    @XmlElement
    private String _userName;

    @XmlElement
    private String _userPassword;

    @XmlElement
    private String _newPassword;

    @XmlElement
    private String _domain;

    public ChangeUserPasswordParameters(String userName, String userPassword, String newPassword, String domain) {
        _userName = userName;
        _userPassword = userPassword;
        _newPassword = newPassword;
        _domain = domain;
    }

    public String getUserName() {
        return _userName;
    }

    public String getUserPassword() {
        return _userPassword;
    }

    public String getNewPassword() {
        return _newPassword;
    }

    public String getDomain() {
        return _domain;
    }

    public ChangeUserPasswordParameters() {
    }
}

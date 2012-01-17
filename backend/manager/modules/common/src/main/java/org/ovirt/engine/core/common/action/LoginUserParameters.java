package org.ovirt.engine.core.common.action;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "LoginUserParameters")
public class LoginUserParameters extends VdcActionParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = -1660445011620552804L;

    @XmlElement(name = "userName")
    private String _userName;

    @XmlElement(name = "userPassword")
    private String _userPassword;

    @XmlElement(name = "domain")
    private String _domain;

    @XmlElement(name = "os")
    private String _os;

    @XmlElement(name = "browser")
    private String _browser;

    @XmlElement(name = "clientType")
    private String _clientType;

    @XmlElement(name = "ActionType")
    private VdcActionType _actionType = VdcActionType.forValue(0);

    public LoginUserParameters(String userName, String userPassword, String domain, String os, String browser,
            String clientType) {
        _actionType = VdcActionType.LoginUser;
        _userName = userName;
        _userPassword = userPassword;
        _domain = domain.trim();
        _os = os;
        _browser = browser;
        _clientType = clientType;
    }

    public LoginUserParameters() {
        _actionType = VdcActionType.LoginUser;
    }

    public String getUserName() {
        return _userName;
    }

    public void setUserName(String value) {
        _userName = value;
    }

    public String getUserPassword() {
        return _userPassword;
    }

    public String getDomain() {
        return _domain;
    }

    public String getOs() {
        return _os;
    }

    public String getBrowser() {
        return _browser;
    }

    public String getClientType() {
        return _clientType;
    }

    @XmlElement
    public VdcActionType getActionType() {
        return _actionType;
    }

    public void setActionType(VdcActionType value) {
        _actionType = value;
    }

    @XmlElement(name = "IsAdmin")
    private boolean privateIsAdmin;

    public boolean getIsAdmin() {
        return privateIsAdmin;
    }

    public void setIsAdmin(boolean value) {
        privateIsAdmin = value;
    }

}

package org.ovirt.engine.core.common.action;

public class LoginUserParameters extends VdcActionParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = -1660445011620552804L;

    private String _userName;

    private String _userPassword;

    private String _domain;

    private String _os;

    private String _browser;

    private String _clientType;

    private VdcActionType _actionType;

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

    public VdcActionType getActionType() {
        return _actionType;
    }

    public void setActionType(VdcActionType value) {
        _actionType = value;
    }

    private boolean privateIsAdmin;

    public boolean getIsAdmin() {
        return privateIsAdmin;
    }

    public void setIsAdmin(boolean value) {
        privateIsAdmin = value;
    }

}

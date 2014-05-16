package org.ovirt.engine.core.common.action;

import java.io.Serializable;

public class LoginUserParameters extends VdcActionParametersBase implements Serializable {
    private static final long serialVersionUID = -1660445011620552804L;

    private String profileName;
    private String loginName;
    private String password;
    private VdcActionType actionType;
    private boolean isAdmin;

    public LoginUserParameters(String profileName, String loginName, String password) {
        this.profileName = profileName;
        this.loginName = loginName;
        this.password = password;
        actionType = VdcActionType.LoginUser;
    }

    public LoginUserParameters() {
        actionType = VdcActionType.LoginUser;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String value) {
        loginName = value;
    }

    @ShouldNotBeLogged
    public String getPassword() {
        return password;
    }

    public String getProfileName() {
        return profileName;
    }

    public VdcActionType getActionType() {
        return actionType;
    }

    public void setActionType(VdcActionType value) {
        actionType = value;
    }


    public boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean value) {
        isAdmin = value;
    }

}

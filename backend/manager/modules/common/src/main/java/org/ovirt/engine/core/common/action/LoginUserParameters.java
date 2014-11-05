package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.aaa.AuthType;


public class LoginUserParameters extends VdcActionParametersBase implements Serializable {
    private static final long serialVersionUID = -1660445011620552804L;

    private static class AuthenticationInformation implements Serializable {
        private static final long serialVersionUID = -3144932226242786617L;
        private String loginName;
        private String password;
        private transient Object authRecord;
        private AuthType authType;
    }

    private AuthenticationInformation authInfo;

    private String profileName;
    private VdcActionType actionType;
    private boolean isAdmin;

    public LoginUserParameters(String profileName, String loginName, String password) {
        this.profileName = profileName;
        this.authInfo = new AuthenticationInformation();
        this.authInfo.loginName = loginName;
        this.authInfo.password = password;
        this.authInfo.authType = AuthType.CREDENTIALS;
        actionType = VdcActionType.LoginUser;

    }

    public LoginUserParameters() {
        actionType = VdcActionType.LoginUser;
    }

    public LoginUserParameters(String profileName, Object authRecord, String password, AuthType authType) {
        this(profileName, authRecord, password, VdcActionType.LoginUser, authType);
    }

    public LoginUserParameters(
            String profileName,
            Object authRecord,
            String password,
            VdcActionType vdcActionType,
            AuthType authType
            ) {
        this.authInfo = new AuthenticationInformation();
        this.authInfo.authRecord = authRecord;
        this.authInfo.authType = AuthType.CREDENTIALS;
        this.profileName = profileName;
        this.actionType = vdcActionType;
        this.authInfo.password = password;

    }

    public String getLoginName() {
        return authInfo.loginName;
    }

    public void setLoginName(String value) {
        authInfo.loginName = value;
    }

    @ShouldNotBeLogged
    public String getPassword() {
        return authInfo.password;
    }

    public String getProfileName() {
        return profileName;
    }

    public Object getAuthRecord() {
        return authInfo.authRecord;
    }

    public AuthType getAuthType() {
        return authInfo.authType;
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

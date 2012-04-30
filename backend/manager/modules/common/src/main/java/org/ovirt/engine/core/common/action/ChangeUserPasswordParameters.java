package org.ovirt.engine.core.common.action;

public class ChangeUserPasswordParameters extends VdcActionParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = -1289433335701261260L;

    private String _userName;

    private String _userPassword;

    private String _newPassword;

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

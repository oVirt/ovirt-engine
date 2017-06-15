package org.ovirt.engine.core.common.action;

public class TerminateSessionsForTokenParameters extends ActionParametersBase {

    private static final long serialVersionUID = -5485237639188940638L;
    /**
     * Database id of a session of a user to logout
     */
    private String ssoAccessToken;

    public TerminateSessionsForTokenParameters() {}

    public TerminateSessionsForTokenParameters(String ssoAccessToken) {
        this.ssoAccessToken = ssoAccessToken;
    }

    public void setSsoAccessToken(String ssoAccessToken) {
        this.ssoAccessToken = ssoAccessToken;
    }

    public String getSsoAccessToken() {
        return ssoAccessToken;
    }
}

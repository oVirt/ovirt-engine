package org.ovirt.engine.core.common.action;

import java.io.Serializable;

public class VdcLoginReturnValueBase extends VdcReturnValueBase implements Serializable {
    private static final long serialVersionUID = 9209472242567186348L;

    private LoginResult _loginResult;
    private String engineSessionId;

    public LoginResult getLoginResult() {
        return _loginResult;
    }

    public void setLoginResult(LoginResult value) {
        _loginResult = value;
    }

    public VdcLoginReturnValueBase() {
        _loginResult = LoginResult.Autheticated;
    }

    public void setSessionId(String engineSessionId) {
        this.engineSessionId = engineSessionId;
    }

    public String getSessionId() {
        return engineSessionId;
    }

}

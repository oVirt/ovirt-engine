package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdcLoginReturnValueBase", namespace = "http://service.engine.ovirt.org")
public class VdcLoginReturnValueBase extends VdcReturnValueBase implements Serializable {
    private static final long serialVersionUID = 9209472242567186348L;

    @XmlElement(name = "LoginResult")
    private LoginResult _loginResult = LoginResult.forValue(0);

    public LoginResult getLoginResult() {
        return _loginResult;
    }

    public void setLoginResult(LoginResult value) {
        _loginResult = value;
    }

    public VdcLoginReturnValueBase() {
    }
}

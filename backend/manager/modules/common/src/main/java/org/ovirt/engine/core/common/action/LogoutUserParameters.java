package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "LogoutUserParameters")
public class LogoutUserParameters extends VdcActionParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = -8545136602971701926L;
    @XmlElement(name = "UserId")
    private Guid _userId = new Guid();

    public LogoutUserParameters(Guid userId) {
        _userId = userId;
    }

    public Guid getUserId() {
        return _userId;
    }

    public LogoutUserParameters() {
    }
}

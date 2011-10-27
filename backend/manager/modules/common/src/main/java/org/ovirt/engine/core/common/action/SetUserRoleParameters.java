package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.users.*;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SetUserRoleParameters")
public class SetUserRoleParameters extends VdcActionParametersBase {
    private static final long serialVersionUID = -4994018566274083013L;

    @XmlElement(name = "User")
    private VdcUser _user;

    @XmlElement(name = "Role")
    private VdcRole _role = VdcRole.forValue(0);

    public SetUserRoleParameters(VdcUser user, VdcRole role) {
        _user = user;
        _role = role;
    }

    public VdcUser getUser() {
        return _user;
    }

    public VdcRole getRole() {
        return _role;
    }

    public SetUserRoleParameters() {
    }
}

package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.users.*;

public class SetUserRoleParameters extends VdcActionParametersBase {
    private static final long serialVersionUID = -4994018566274083013L;

    private VdcUser _user;

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

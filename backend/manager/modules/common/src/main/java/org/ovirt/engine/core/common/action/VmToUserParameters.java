package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;

import org.ovirt.engine.core.common.users.*;

public class VmToUserParameters extends VmToAdElementParameters {
    private static final long serialVersionUID = -4014070637785795975L;
    private VdcUser _user;

    public VmToUserParameters(VdcUser user, Guid vmId) {
        super(user.getUserId(), vmId);
        _user = user;
    }

    public VdcUser getUser() {
        return _user;
    }

    public VmToUserParameters() {
    }
}

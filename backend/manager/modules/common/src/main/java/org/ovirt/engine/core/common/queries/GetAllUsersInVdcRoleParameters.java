package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.*;

public class GetAllUsersInVdcRoleParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 4075283289991368556L;

    public GetAllUsersInVdcRoleParameters(VdcRole id) {
        _id = id;
    }

    private VdcRole _id = VdcRole.forValue(0);

    public VdcRole getId() {
        return _id;
    }

    public GetAllUsersInVdcRoleParameters() {
    }
}

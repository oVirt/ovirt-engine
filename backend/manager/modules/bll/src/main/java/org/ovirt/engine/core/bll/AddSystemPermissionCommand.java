package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;

public class AddSystemPermissionCommand<T extends PermissionsOperationsParametes> extends AddPermissionCommand<T> {

    public AddSystemPermissionCommand(T parameters) {
        super(parameters);
        parameters.getPermission().setObjectId(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID);
        parameters.getPermission().setObjectType(VdcObjectType.System);
    }

}

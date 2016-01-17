package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;

public class RemoveSystemPermissionCommand<T extends PermissionsOperationsParameters> extends RemovePermissionCommand<T> {

    public RemoveSystemPermissionCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        parameters.getPermission().setObjectId(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID);
        parameters.getPermission().setObjectType(VdcObjectType.System);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_SYSTEM_PERMISSION : AuditLogType.USER_REMOVE_SYSTEM_PERMISSION_FAILED;
    }

}

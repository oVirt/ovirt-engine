package org.ovirt.engine.core.bll.aaa;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DbGroupDao;
import org.ovirt.engine.core.dao.DbUserDao;
import org.ovirt.engine.core.dao.PermissionDao;

public class RemoveGroupCommand<T extends IdParameters> extends AdGroupsHandlingCommandBase<T> {

    @Inject
    private PermissionDao permissionDao;
    @Inject
    private DbUserDao dbUserDao;
    @Inject
    private DbGroupDao dbGroupDao;
    @Inject
    private MultiLevelAdministrationHandler multiLevelAdministrationHandler;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    public RemoveGroupCommand(Guid commandId) {
        super(commandId);
    }

    public RemoveGroupCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        // Get the identifier of the group from the parameters:
        Guid id = getParameters().getId();

        // Remove the permissions of the group:
        // TODO: This should be done without invoking the command to avoid the overhead.
        for (Permission permission : permissionDao.getAllDirectPermissionsForAdElement(id)) {
            PermissionsOperationsParameters param = new PermissionsOperationsParameters(permission);
            param.setSessionId(getParameters().getSessionId());
            runInternalActionWithTasksContext(ActionType.RemovePermission, param);
        }

        // Remove the group itself:
        dbGroupDao.remove(id);

        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded()? AuditLogType.USER_REMOVE_AD_GROUP : AuditLogType.USER_REMOVE_AD_GROUP_FAILED;
    }

    @Override
    protected boolean validate() {
        // Get the identifier of the group from the parameters:
        Guid id = getParameters().getId();

        // Check that the group being removed isn't the last remaining group
        // of super users:
        if (isLastSuperUserGroup(id)) {
            addValidationMessage(EngineMessage.ERROR_CANNOT_REMOVE_LAST_SUPER_USER_ROLE);
            return false;
        }

        // Check that the group being removed isn't the everyone group:
        if (MultiLevelAdministrationHandler.EVERYONE_OBJECT_ID.equals(id)) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_CANNOT_REMOVE_BUILTIN_GROUP_EVERYONE);
            return false;
        }

        return true;
    }

    protected boolean isLastSuperUserGroup(Guid groupId) {
        return multiLevelAdministrationHandler.isLastSuperUserGroup(groupId);
    }
}

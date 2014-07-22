package org.ovirt.engine.core.bll.aaa;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.PredefinedUsers;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveUserCommand<T extends IdParameters> extends UserCommandBase<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected RemoveUserCommand(Guid commandId) {
        super(commandId);
    }

    public RemoveUserCommand(T parameters) {
        super(parameters);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_ADUSER : AuditLogType.USER_FAILED_REMOVE_ADUSER;

    }

    @Override
    protected void executeCommand() {
        // Get the identifier of the user to be removed from the parameters:
        Guid id = getParameters().getId();

        // Delete all the permissions of the user:
        // TODO: This should be done without invoking the command to avoid the overhead.
        for (Permissions permission : DbFacade.getInstance()
                .getPermissionDao()
                .getAllDirectPermissionsForAdElement(id)) {
            PermissionsOperationsParameters tempVar = new PermissionsOperationsParameters(permission);
            tempVar.setShouldBeLogged(false);
            runInternalActionWithTasksContext(VdcActionType.RemovePermission, tempVar);
        }

        // Delete the user itself:
        getDbUserDAO().remove(id);

        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        // Get the identifier of the user to be removed:
        Guid id = getParameters().getId();

        // Check that we are not trying to remove the built-in admin user:
        if (PredefinedUsers.ADMIN_USER.getId().equals(id)) {
            addCanDoActionMessage(VdcBllMessages.USER_CANNOT_REMOVE_ADMIN_USER);
            return false;
        }

        // Check that the current user isn't trying to remove himself:
        if (getCurrentUser().getId().equals(id)) {
            addCanDoActionMessage(VdcBllMessages.USER_CANNOT_REMOVE_HIMSELF);
            return false;
        }

        // Check that the user exists in the database:
        DbUser dbUser = getDbUserDAO().get(id);
        if (dbUser == null) {
            addCanDoActionMessage(VdcBllMessages.USER_MUST_EXIST_IN_DB);
            return false;
        }

        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__USER);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        // Not needed for admin operations.
        return Collections.singletonList(new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System,
                getActionType().getActionGroup()));
    }
}

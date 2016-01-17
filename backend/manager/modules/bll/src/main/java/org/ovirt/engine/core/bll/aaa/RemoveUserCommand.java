package org.ovirt.engine.core.bll.aaa;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveUserCommand<T extends IdParameters> extends UserCommandBase<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    public RemoveUserCommand(Guid commandId) {
        super(commandId);
    }

    public RemoveUserCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
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
        for (Permission permission : DbFacade.getInstance()
                .getPermissionDao()
                .getAllDirectPermissionsForAdElement(id)) {
            PermissionsOperationsParameters tempVar = new PermissionsOperationsParameters(permission);
            tempVar.setShouldBeLogged(false);
            runInternalActionWithTasksContext(VdcActionType.RemovePermission, tempVar);
        }

        // Delete the user itself:
        getDbUserDao().remove(id);

        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        // Get the identifier of the user to be removed:
        Guid id = getParameters().getId();

        // Check that the current user isn't trying to remove himself:
        if (getCurrentUser().getId().equals(id)) {
            addValidationMessage(EngineMessage.USER_CANNOT_REMOVE_HIMSELF);
            return false;
        }

        // Check that the user exists in the database:
        DbUser dbUser = getDbUserDao().get(id);
        if (dbUser == null) {
            addValidationMessage(EngineMessage.USER_MUST_EXIST_IN_DB);
            return false;
        }

        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__USER);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        // Not needed for admin operations.
        return Collections.singletonList(new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System,
                getActionType().getActionGroup()));
    }
}

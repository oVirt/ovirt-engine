package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AdElementParametersBase;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveUserCommand<T extends AdElementParametersBase> extends UserCommandBase<T> {

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
        for (permissions permission : DbFacade.getInstance()
                .getPermissionDao()
                .getAllDirectPermissionsForAdElement(getAdUserId())) {
            PermissionsOperationsParametes tempVar = new PermissionsOperationsParametes(permission);
            tempVar.setShouldBeLogged(false);
            Backend.getInstance().runInternalAction(VdcActionType.RemovePermission,
                    tempVar,
                    ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
        }
        DbFacade.getInstance().getDbUserDao().remove(getAdUserId());
        setSucceeded(true);
    }

    public static boolean CanRemoveUser(Guid user_guid, java.util.ArrayList<String> errors) {
        boolean returnValue = true;
        // check that the user exists in DB
        if (DbFacade.getInstance().getDbUserDao().get(user_guid) == null) {
            errors.add(VdcBllMessages.USER_MUST_EXIST_IN_DB.toString());
            returnValue = false;
        }
        return returnValue;
    }

    @Override
    protected boolean canDoAction() {
        boolean result = true;
        if (getParameters().getAdElementId().equals(PredefinedUsers.ADMIN_USER.getId())) {
            addCanDoActionMessage(VdcBllMessages.USER_CANNOT_REMOVE_ADMIN_USER);
            result = false;
        } else {
            if (getCurrentUser().getUserId().equals(getParameters().getAdElementId())) {
                addCanDoActionMessage(VdcBllMessages.USER_CANNOT_REMOVE_HIMSELF);
                result = false;
            } else {
                result = CanRemoveUser(getParameters().getAdElementId(), getReturnValue().getCanDoActionMessages());
            }
        }
        return result;
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

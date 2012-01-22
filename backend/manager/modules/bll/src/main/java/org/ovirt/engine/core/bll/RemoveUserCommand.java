package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AdElementParametersBase;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
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
                .getPermissionDAO()
                .getAllDirectPermissionsForAdElement(getAdUserId())) {
            PermissionsOperationsParametes tempVar = new PermissionsOperationsParametes(permission);
            tempVar.setShouldBeLogged(false);
            Backend.getInstance().runInternalAction(VdcActionType.RemovePermission,
                    tempVar,
                    ExecutionHandler.createDefaultContexForTasks(executionContext));
        }
        DbFacade.getInstance().getDbUserDAO().remove(getAdUserId());
        setSucceeded(true);
    }

    public static boolean CanRemoveUser(Guid user_guid, java.util.ArrayList<String> errors) {
        boolean returnValue = true;
        // check that the user exists in DB
        if (DbFacade.getInstance().getDbUserDAO().get(user_guid) == null) {
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
    public Map<Guid, VdcObjectType> getPermissionCheckSubjects() {
        return Collections.singletonMap(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID, VdcObjectType.System);
    }
}

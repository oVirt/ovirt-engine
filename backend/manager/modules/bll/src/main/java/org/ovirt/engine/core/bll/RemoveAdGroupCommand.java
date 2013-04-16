package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AdElementParametersBase;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class RemoveAdGroupCommand<T extends AdElementParametersBase> extends AdGroupsHandlingCommandBase<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected RemoveAdGroupCommand(Guid commandId) {
        super(commandId);
    }

    public RemoveAdGroupCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        for (permissions permission : getPermissionDAO().getAllDirectPermissionsForAdElement(getAdGroup().getid())) {
            PermissionsOperationsParametes param = new PermissionsOperationsParametes(permission);
            param.setSessionId(getParameters().getSessionId());
            getBackend().runInternalAction(VdcActionType.RemovePermission,
                    param,
                    ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
        }
        getAdGroupDAO().remove(getAdGroup().getid());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_AD_GROUP : AuditLogType.USER_REMOVE_AD_GROUP_FAILED;
    }

    @Override
    protected boolean canDoAction() {
        Guid groupId = getParameters().getAdElementId();
        List<String> reasons = getReturnValue().getCanDoActionMessages();
        boolean returnValue = true;
        if (isLastSuperUserGroup(groupId)) {
            returnValue = false;
            reasons.add(VdcBllMessages.ERROR_CANNOT_REMOVE_LAST_SUPER_USER_ROLE.toString());
        }
        if (groupId.equals(MultiLevelAdministrationHandler.EVERYONE_OBJECT_ID)) {
            returnValue = false;
            reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_REMOVE_BUILTIN_GROUP_EVERYONE.name());
        }

        return returnValue;
    }

    protected boolean isLastSuperUserGroup(Guid groupId) {
        return MultiLevelAdministrationHandler.isLastSuperUserGroup(groupId);
    }
}

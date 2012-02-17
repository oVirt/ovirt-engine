package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AdElementParametersBase;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;

@CustomLogFields({ @CustomLogField("Role") })
// internal class SetAdGroupRoleCommand : AdGroupsHandlingCommandBase
// {
// internal SetAdGroupRoleCommand(SetAdGroupRoleParameters parameters) :
// base(parameters)
// {
// }
// SetAdGroupRoleParameters SetAdGroupRoleParameters
// {
// get
// {
// return Parameters as SetAdGroupRoleParameters;
// }
// }
// public override AuditLogType AuditLogTypeValue
// {
// get
// {
// return
// Succeeded
// ?
// SetAdGroupRoleParameters.IsRestored
// ? AuditLogType.USER_RESTORED_ROLE_TO_AD_GROUP
// :
// AuditLogType.USER_SET_ROLE_TO_AD_GROUP
// :
// AuditLogType.USER_FAILED_SET_ROLE_TO_AD_GROUP;
// }
// }
// public string Role
// {
// get
// {
// return SetAdGroupRoleParameters.AdGroup.vdc_role.toString();
// }
// }
// protected override void ExecuteCommand()
// {
// if (AdGroup != null)
// {
// UserCommandBase.ProcessAdElementDefaultRole
// (SetAdGroupRoleParameters.AdGroup.id,
// SetAdGroupRoleParameters.AdGroup.vdc_role,
// SetAdGroupRoleParameters.AdGroup.name);
// DbFacade.Instance.updateAdGroup(SetAdGroupRoleParameters.AdGroup);
// }
// else
// {
// AddAdGroup(SetAdGroupRoleParameters.AdGroup);
// }
// Succeeded = true;
// }
// }
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
        for (permissions permission : DbFacade.getInstance()
                .getPermissionDAO()
                .getAllDirectPermissionsForAdElement(getAdGroup().getid())) {
            PermissionsOperationsParametes param = new PermissionsOperationsParametes(permission);
            param.setSessionId(getParameters().getSessionId());
            Backend.getInstance().runInternalAction(VdcActionType.RemovePermission,
                    param,
                    ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
        }
        DbFacade.getInstance().getAdGroupDAO().remove(getAdGroup().getid());
        setSucceeded(true);
    }

    public static boolean CanRemoveAdGroup(Guid groupId, java.util.ArrayList<String> reasons) {
        boolean returnValue = true;
        if (MultiLevelAdministrationHandler.isLastSuperUserGroup(groupId)) {
            returnValue = false;
            reasons.add(VdcBllMessages.ERROR_CANNOT_REMOVE_LAST_SUPER_USER_ROLE.toString());
        }
        if (groupId.equals(MultiLevelAdministrationHandler.EVERYONE_OBJECT_ID)) {
            returnValue = false;
            reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_REMOVE_BUILTIN_GROUP_EVERYONE.name());
        }
        return returnValue;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_AD_GROUP : AuditLogType.USER_REMOVE_AD_GROUP_FAILED;
    }

    @Override
    protected boolean canDoAction() {
        return CanRemoveAdGroup(getParameters().getAdElementId(), getReturnValue().getCanDoActionMessages());
    }
}

package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RolesParameterBase;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveRoleCommand<T extends RolesParameterBase> extends RolesCommandBase<T> {
    public RemoveRoleCommand(T parameters) {
        super(parameters);

    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;
        roles roles = DbFacade.getInstance().getRoleDAO().get(getParameters().getRoleId());
        if (roles == null) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_REMOVE_ROLE_INVALID_ROLE_ID);
        } else {
            if (CheckIfRoleIsReadOnly(getReturnValue().getCanDoActionMessages())) {
                returnValue = false;
                addCanDoActionMessage(VdcBllMessages.VAR__TYPE__ROLE);
                addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
            } else {
                if (DbFacade.getInstance().getPermissionDAO().getAllForRole(getParameters().getRoleId()).size() != 0) {
                    returnValue = false;
                    addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_REMOVE_ROLE_ATTACHED_TO_PERMISSION);

                }
            }
        }
        return returnValue;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_ROLE : AuditLogType.USER_REMOVE_ROLE_FAILED;
    }

    @Override
    protected void executeCommand() {
        // cache role for logging
        DbFacade.getInstance().getRoleDAO().remove(getRole().getId());
        setSucceeded(true);
    }
}

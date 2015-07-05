package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RolesParameterBase;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class RemoveRoleCommand<T extends RolesParameterBase> extends RolesCommandBase<T> {

    public RemoveRoleCommand(T parameters) {
        super(parameters);

    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;
        if (getRole() == null) {
            returnValue = false;
            addCanDoActionMessage(EngineMessage.ERROR_CANNOT_REMOVE_ROLE_INVALID_ROLE_ID);
        } else {
            if (checkIfRoleIsReadOnly(getReturnValue().getCanDoActionMessages())) {
                returnValue = false;
                addCanDoActionMessage(EngineMessage.VAR__TYPE__ROLE);
                addCanDoActionMessage(EngineMessage.VAR__ACTION__REMOVE);
            } else {
                if (getPermissionDao().getAllForRole(getParameters().getRoleId()).size() != 0) {
                    returnValue = false;
                    addCanDoActionMessage(EngineMessage.ERROR_CANNOT_REMOVE_ROLE_ATTACHED_TO_PERMISSION);

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
        getRoleDao().remove(getRole().getId());
        setSucceeded(true);
    }
}

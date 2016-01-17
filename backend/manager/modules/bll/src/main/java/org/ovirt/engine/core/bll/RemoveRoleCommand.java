package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RolesParameterBase;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class RemoveRoleCommand<T extends RolesParameterBase> extends RolesCommandBase<T> {

    public RemoveRoleCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validate() {
        boolean returnValue = true;
        if (getRole() == null) {
            returnValue = false;
            addValidationMessage(EngineMessage.ERROR_CANNOT_REMOVE_ROLE_INVALID_ROLE_ID);
        } else {
            if (checkIfRoleIsReadOnly(getReturnValue().getValidationMessages())) {
                returnValue = false;
                addValidationMessage(EngineMessage.VAR__TYPE__ROLE);
                addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
            } else {
                if (getPermissionDao().getAllForRole(getParameters().getRoleId()).size() != 0) {
                    returnValue = false;
                    addValidationMessage(EngineMessage.ERROR_CANNOT_REMOVE_ROLE_ATTACHED_TO_PERMISSION);

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

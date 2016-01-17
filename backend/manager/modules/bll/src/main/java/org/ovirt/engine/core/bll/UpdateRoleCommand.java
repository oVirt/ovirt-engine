package org.ovirt.engine.core.bll;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RolesOperationsParameters;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class UpdateRoleCommand<T extends RolesOperationsParameters> extends RolesOperationCommandBase<T> {

    public UpdateRoleCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_ROLE : AuditLogType.USER_UPDATE_ROLE_FAILED;
    }

    @Override
    protected boolean validate() {
        boolean returnValue = true;
        Role oldRole = getRoleDao().get(getRole().getId());
        if (oldRole == null) {
            addValidationMessage(EngineMessage.ERROR_CANNOT_UPDATE_ROLE_ID);
            returnValue = false;
        } else {
            if (checkIfRoleIsReadOnly(getReturnValue().getValidationMessages())) {
                returnValue = false;
                addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
            } else if (!StringUtils.equals(getRole().getName(), oldRole.getName())
                    && getRoleDao().getByName(getRole().getName()) != null) {
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
                returnValue = false;
            } // changing role type isn't allowed
            else if (getRole().getType() != oldRole.getType()) {
                addValidationMessage(EngineMessage.ERROR_CANNOT_UPDATE_ROLE_TYPE);
                returnValue = false;
            }
        }
        if (!returnValue) {
            addValidationMessage(EngineMessage.VAR__TYPE__ROLE);
            addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);

        }
        return returnValue;
    }

    @Override
    protected void executeCommand() {
        getRoleDao().update(getRole());
        setSucceeded(true);
    }
}

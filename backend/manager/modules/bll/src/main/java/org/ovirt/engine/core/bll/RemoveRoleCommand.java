package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RolesParameterBase;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.RoleDao;

public class RemoveRoleCommand<T extends RolesParameterBase> extends RolesCommandBase<T> {

    @Inject
    private PermissionDao permissionDao;
    @Inject
    private RoleDao roleDao;

    public RemoveRoleCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validate() {
        if (getRole() == null) {
            return failValidation(EngineMessage.ERROR_CANNOT_REMOVE_ROLE_INVALID_ROLE_ID);
        }
        if (checkIfRoleIsReadOnly(getReturnValue().getValidationMessages())) {
            return false;
        }
        if (!permissionDao.getAllForRole(getParameters().getRoleId()).isEmpty()) {
            return failValidation(EngineMessage.ERROR_CANNOT_REMOVE_ROLE_ATTACHED_TO_PERMISSION);
        }
        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__ROLE);
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_ROLE : AuditLogType.USER_REMOVE_ROLE_FAILED;
    }

    @Override
    protected void executeCommand() {
        // cache role for logging
        roleDao.remove(getRole().getId());
        setSucceeded(true);
    }
}

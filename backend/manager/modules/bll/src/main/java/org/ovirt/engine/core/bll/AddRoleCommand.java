package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RolesOperationsParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.compat.Guid;

public class AddRoleCommand<T extends RolesOperationsParameters> extends RolesOperationCommandBase<T> {

    public AddRoleCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;
        if (getRoleDao().getByName(getRoleName()) != null) {
            addCanDoActionMessage(EngineMessage.VAR__ACTION__ADD);
            addCanDoActionMessage(EngineMessage.VAR__TYPE__ROLE);
            addCanDoActionMessage(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
            returnValue = false;

        }
        return returnValue;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_ROLE : AuditLogType.USER_ADD_ROLE_FAILED;
    }

    @Override
    protected void executeCommand() {
        getRole().setId(Guid.newGuid());
        getRole().setAllowsViewingChildren(false);
        // Set the application mode as 255 - AllModes by default
        getRole().setAppMode(ApplicationMode.AllModes);
        getRoleDao().save(getRole());
        getReturnValue().setActionReturnValue(getRole().getId());
        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System,
                getActionType().getActionGroup()));
    }
}

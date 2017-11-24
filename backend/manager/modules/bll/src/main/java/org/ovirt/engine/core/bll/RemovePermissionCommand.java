package org.ovirt.engine.core.bll;

import java.util.Objects;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.DetachUserFromVmFromPoolParameters;
import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DbUserDao;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmStaticDao;

public class RemovePermissionCommand<T extends PermissionsOperationsParameters> extends PermissionsCommandBase<T> {

    @Inject
    private PermissionDao permissionDao;
    @Inject
    private DbUserDao dbUserDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private MultiLevelAdministrationHandler multiLevelAdministrationHandler;
    @Inject
    private VmStaticDao vmStaticDao;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected RemovePermissionCommand(Guid commandId) {
        super(commandId);
    }

    public RemovePermissionCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__PERMISSION);
    }

    @Override
    protected boolean validate() {
        Permission p = permissionDao.get(getParameters().getPermission().getId());
        if (multiLevelAdministrationHandler.isLastSuperUserPermission(p.getRoleId())) {
            return failValidation(EngineMessage.ERROR_CANNOT_REMOVE_LAST_SUPER_USER_ROLE);
        }
        if (p.getRoleType().equals(RoleType.ADMIN) && !isSystemSuperUser()) {
            return failValidation(EngineMessage.PERMISSION_REMOVE_FAILED_ONLY_SYSTEM_SUPER_USER_CAN_REMOVE_ADMIN_ROLES);
        }
        if (Objects.equals(p.getObjectId(), MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID) &&
            Objects.equals(p.getAdElementId(), MultiLevelAdministrationHandler.EVERYONE_OBJECT_ID)
        ) {
            return failValidation(EngineMessage.SYSTEM_PERMISSIONS_CANT_BE_REMOVED_FROM_EVERYONE);
        }
        if(!Objects.equals(p.getAdElementId(), getParameters().getTargetId())
            && dbUserDao.get(getParameters().getTargetId()) != null) {
            return failValidation(EngineMessage.INHERITED_PERMISSION_CANT_BE_REMOVED);
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        Permission perms = getParameters().getPermission();
        Guid userId = perms.getAdElementId();

        // if removing engine user permission from vm,
        // check if vm is from pool and detach it
        if (perms.getObjectType().equals(VdcObjectType.VM)
                && perms.getRoleId().equals(PredefinedRoles.ENGINE_USER.getId())) {
            VM vm = vmDao.get(perms.getObjectId());
            if (vm != null && vm.getVmPoolId() != null) {
                runInternalActionWithTasksContext(ActionType.DetachUserFromVmFromPool,
                        new DetachUserFromVmFromPoolParameters(vm.getVmPoolId(), userId, vm.getId(), true));
            }
        }

        vmStaticDao.incrementDbGeneration(perms.getObjectId());
        permissionDao.remove(perms.getId());
        dbUserDao.updateLastAdminCheckStatus(userId);
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_PERMISSION : AuditLogType.USER_REMOVE_PERMISSION_FAILED;
    }
}

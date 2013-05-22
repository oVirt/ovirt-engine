package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class AddPermissionCommand<T extends PermissionsOperationsParametes> extends PermissionsCommandBase<T> {

    public AddPermissionCommand(T parameters) {
        super(parameters);
    }

    public AddPermissionCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected boolean canDoAction() {
        permissions perm = getParameters().getPermission();
        if (perm == null) {
            addCanDoActionMessage(VdcBllMessages.PERMISSION_ADD_FAILED_PERMISSION_NOT_SENT);
            return false;
        }

        Role role = getRoleDao().get(perm.getrole_id());
        Guid adElementId = perm.getad_element_id();

        if (role == null) {
            addCanDoActionMessage(VdcBllMessages.PERMISSION_ADD_FAILED_INVALID_ROLE_ID);
            return false;
        }

        if (perm.getObjectType() == null
                || getVdcObjectName() == null) {
            addCanDoActionMessage(VdcBllMessages.PERMISSION_ADD_FAILED_INVALID_OBJECT_ID);
            return false;
        }
        // check if no ad_element_id in permission or id doesn't equal to sent
        // user or group
        if ((adElementId == null)
                || (getParameters().getVdcUser() != null && !getParameters().getVdcUser()
                        .getUserId()
                        .equals(adElementId))
                || (getParameters().getAdGroup() != null && !getParameters().getAdGroup().getid().equals(adElementId))) {
            addCanDoActionMessage(VdcBllMessages.PERMISSION_ADD_FAILED_USER_ID_MISMATCH);
            return false;
        }
        // if user and group not sent check user/group is in the db in order to
        // give permission
        if (getParameters().getVdcUser() == null
                && getParameters().getAdGroup() == null
                && getDbUserDAO().get(adElementId) == null
                && getAdGroupDAO().get(adElementId) == null) {
            getReturnValue().getCanDoActionMessages().add(VdcBllMessages.USER_MUST_EXIST_IN_DB.toString());
            return false;
        }

        // only system super user can give permissions with admin roles
        if (!isSystemSuperUser() && role.getType() == RoleType.ADMIN) {
            addCanDoActionMessage(VdcBllMessages.PERMISSION_ADD_FAILED_ONLY_SYSTEM_SUPER_USER_CAN_GIVE_ADMIN_ROLES);
            return false;
        }

        // don't allow adding permissions to vms from pool externally
        if (!isInternalExecution() && perm.getObjectType() == VdcObjectType.VM) {
            VM vm = getVmDAO().get(perm.getObjectId());
            if (vm != null && vm.getVmPoolId() != null) {
                addCanDoActionMessage(VdcBllMessages.PERMISSION_ADD_FAILED_VM_IN_POOL);
                return false;
            }
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        final permissions paramPermission = getParameters().getPermission();
        permissions permission =
                getPermissionDAO().getForRoleAndAdElementAndObject(paramPermission.getrole_id(),
                        paramPermission.getad_element_id(),
                        paramPermission.getObjectId());

        if (permission == null) {
            // try to add user to db if vdcUser sent
            if (getParameters().getVdcUser() != null && _dbUser == null) {
                _dbUser = UserCommandBase.initUser(getParameters().getVdcUser(), getParameters().getSessionId());
            }
            // try to add group to db if adGroup sent
            else if (getParameters().getAdGroup() != null) {
                _adGroup = AdGroupsHandlingCommandBase.initAdGroup(getParameters().getAdGroup());
            }

            paramPermission.setId(Guid.NewGuid());

            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                @Override
                public Void runInTransaction() {
                    getPermissionDAO().save(paramPermission);
                    getCompensationContext().snapshotNewEntity(paramPermission);
                    getCompensationContext().stateChanged();
                    return null;
                }
            });
            permission = paramPermission;
        }

        getReturnValue().setActionReturnValue(permission.getId());

        if (_dbUser != null) {
            updateAdminStatus(permission);
        }
        setSucceeded(true);
    }

    private void updateAdminStatus(permissions perm) {
        // if the role of the permission is of type admin update the user
        // lastAdminCheckStatus to true
        Role role = getRoleDao().get(perm.getrole_id());
        if (role.getType() == RoleType.ADMIN) {
            MultiLevelAdministrationHandler.setIsAdminGUIFlag(perm.getad_element_id(), true);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_PERMISSION : AuditLogType.USER_ADD_PERMISSION_FAILED;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        permissions permission = getParameters().getPermission();
        return Collections.singletonList(new PermissionSubject(permission.getObjectId(),
                permission.getObjectType(),
                getActionType().getActionGroup()));
    }
}

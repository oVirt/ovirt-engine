package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class AddPermissionCommand<T extends PermissionsOperationsParametes> extends PermissionsCommandBase<T> {
    public AddPermissionCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        permissions perm = getParameters().getPermission();
        if (perm == null) {
            addCanDoActionMessage(VdcBllMessages.PERMISSION_ADD_FAILED_PERMISSION_NOT_SENT);
            return false;
        }

        roles role = DbFacade.getInstance().getRoleDAO().get(perm.getrole_id());
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
                || (getParameters().getVdcUser() != null && !getParameters().getVdcUser().getUserId()
                        .equals(adElementId))
                        || (getParameters().getAdGroup() != null && !getParameters().getAdGroup().getid().equals(adElementId))) {
            addCanDoActionMessage(VdcBllMessages.PERMISSION_ADD_FAILED_USER_ID_MISMATCH);
            return false;
        }
        // if user and group not sent check user/group is in the db in order to
        // give permission
        if (adElementId != null
                && getParameters().getVdcUser() == null
                && getParameters().getAdGroup() == null
                && (DbFacade.getInstance().getDbUserDAO().get(adElementId) == null && DbFacade
                        .getInstance().getAdGroupDAO().get(adElementId) == null)) {
            getReturnValue().getCanDoActionMessages().add(
                                                          VdcBllMessages.USER_MUST_EXIST_IN_DB.toString());
            return false;
        }

        // we check that we don't insert duplicate permissions
        if (DbFacade
                .getInstance()
                .getPermissionDAO()
                .getForRoleAndAdElementAndObject(perm.getrole_id(), adElementId,
                                                 perm.getObjectId()) != null) {
            addCanDoActionMessage(VdcBllMessages.ERROR_PERMISSION_ALREADY_EXIST);
            return false;
        }

        // only system super user can give permissions with admin roles
        if (!isSystemSuperUser() && role.getType() == RoleType.ADMIN) {
            addCanDoActionMessage(VdcBllMessages.PERMISSION_ADD_FAILED_ONLY_SYSTEM_SUPER_USER_CAN_GIVE_ADMIN_ROLES);
            return false;
        }

        // don't allow adding permissions to vms from pool externally
        if (!isInternalExecution() && perm.getObjectType() == VdcObjectType.VM) {
            VM vm = DbFacade.getInstance().getVmDAO().getById(perm.getObjectId());
            if (vm != null && vm.getVmPoolId() != null) {
                addCanDoActionMessage(VdcBllMessages.PERMISSION_ADD_FAILED_VM_IN_POOL);
                return false;
            }
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        final permissions perm = getParameters().getPermission();

        // try to add user to db if vdcUser sent
        if (getParameters().getVdcUser() != null && _dbUser == null) {
            _dbUser = UserCommandBase.initUser(getParameters().getVdcUser(), getParameters().getSessionId());
            if (_dbUser == null) {
                return;
            }
        }
        // try to add group to db if adGroup sent
        else if (getParameters().getAdGroup() != null) {
            _adGroup = AdGroupsHandlingCommandBase.initAdGroup(getParameters().getAdGroup());
        }

        perm.setId(Guid.NewGuid());

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                DbFacade.getInstance().getPermissionDAO().save(perm);
                getCompensationContext().snapshotNewEntity(perm);
                getCompensationContext().stateChanged();
                return null;
            }
        });
        getReturnValue().setActionReturnValue(perm.getId());

        if (_dbUser != null) {
            updateAdminStatus(perm);
        }
        setSucceeded(true);
    }

    private void updateAdminStatus(permissions perm) {
        // if the role of the permission is of type admin update the user
        // lastAdminCheckStatus to true
        roles role = DbFacade.getInstance().getRoleDAO().get(perm.getrole_id());
        if (role.getType() == RoleType.ADMIN) {
            MultiLevelAdministrationHandler.setIsAdminGUIFlag(perm.getad_element_id(), true);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_PERMISSION : AuditLogType.USER_ADD_PERMISSION_FAILED;
    }

    @Override
    public Map<Guid, VdcObjectType> getPermissionCheckSubjects() {
        permissions permission = getParameters().getPermission();
        return Collections.singletonMap(permission.getObjectId(), permission.getObjectType());
    }
}

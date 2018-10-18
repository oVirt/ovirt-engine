package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddGroupParameters;
import org.ovirt.engine.core.common.action.AddUserParameters;
import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DbGroupDao;
import org.ovirt.engine.core.dao.DbUserDao;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.RoleDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@ValidateSupportsTransaction
public class AddPermissionCommand<T extends PermissionsOperationsParameters> extends PermissionsCommandBase<T> {

    @Inject
    private PermissionDao permissionDao;
    @Inject
    private RoleDao roleDao;
    @Inject
    private DbUserDao dbUserDao;
    @Inject
    private DbGroupDao dbGroupDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private MultiLevelAdministrationHandler multiLevelAdministrationHandler;

    public AddPermissionCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }


    public AddPermissionCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected boolean validate() {
        Permission perm = getParameters().getPermission();
        if (perm == null) {
            addValidationMessage(EngineMessage.PERMISSION_ADD_FAILED_PERMISSION_NOT_SENT);
            return false;
        }

        // Try to find the requested role, first by id and then by name:
        Role role = null;
        Guid roleId = perm.getRoleId();
        String roleName = perm.getRoleName();
        if (!Guid.isNullOrEmpty(roleId)) {
            role = roleDao.get(roleId);
            if (role != null) {
                roleName = role.getName();
                perm.setRoleName(roleName);
            }
        } else if (roleName != null) {
            role = roleDao.getByName(roleName);
            if (role != null) {
                roleId = role.getId();
                perm.setRoleId(roleId);
            }
        }
        if (role == null) {
            addValidationMessage(EngineMessage.PERMISSION_ADD_FAILED_INVALID_ROLE_ID);
            return false;
        }

        Guid adElementId = perm.getAdElementId();

        if (perm.getObjectType() == null
                || getVdcObjectName() == null) {
            addValidationMessage(EngineMessage.PERMISSION_ADD_FAILED_INVALID_OBJECT_ID);
            return false;
        }

        // if user and group not sent check user/group is in the db in order to
        // give permission
        if (getParameters().getUser() == null
                && getParameters().getGroup() == null
                && dbUserDao.get(adElementId) == null
                && dbGroupDao.get(adElementId) == null) {
            return failValidation(EngineMessage.USER_MUST_EXIST_IN_DB);
        }

        // only system super user can give permissions with admin roles
        if (!isSystemSuperUser() && role.getType() == RoleType.ADMIN) {
            return failValidation(EngineMessage.PERMISSION_ADD_FAILED_ONLY_SYSTEM_SUPER_USER_CAN_GIVE_ADMIN_ROLES);
        }

        // don't allow adding permissions to vms from pool externally
        if (!isInternalExecution() && perm.getObjectType() == VdcObjectType.VM) {
            VM vm = vmDao.get(perm.getObjectId());
            if (vm != null && vm.getVmPoolId() != null) {
                return failValidation(EngineMessage.PERMISSION_ADD_FAILED_VM_IN_POOL);
            }
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        boolean externalSsoEnabled = EngineLocalConfig.getInstance().getBoolean("ENGINE_SSO_ENABLE_EXTERNAL_SSO");
        // Get the parameters:
        T parameters = getParameters();

        // The user or group given in the parameters may haven't been added to the database yet, if this is the case
        // then they need to be added to the database now, before the permission:
        DbUser user = parameters.getUser();
        if (user != null) {
            Guid id = user.getId();
            String directory = user.getDomain();
            String externalId = user.getExternalId();
            DbUser existing = externalSsoEnabled ?
                    dbUserDao.getByUsernameAndDomain(user.getLoginName(), directory) :
                    dbUserDao.getByIdOrExternalId(id, directory, externalId);
            if (existing != null) {
                user = existing;
            } else {
                user = addUser(user);
                if (user == null) {
                    setSucceeded(false);
                    return;
                }
            }
        }
        DbGroup group = parameters.getGroup();
        if (group != null) {
            Guid id = group.getId();
            String directory = group.getDomain();
            String externalId = group.getExternalId();
            DbGroup existing = externalSsoEnabled ?
                    dbGroupDao.getByNameAndDomain(group.getName(), directory) :
                    dbGroupDao.getByIdOrExternalId(id, directory, externalId);
            if (existing != null) {
                group = existing;
            } else {
                group = addGroup(group);
                if (group == null) {
                    setSucceeded(false);
                    return;
                }
            }
        }

        // The identifier of the principal of the permission can come from the parameters directly or from the
        // user/group objects:
        Guid principalId;
        if (user != null) {
            principalId = user.getId();
        } else if (group != null) {
            principalId = group.getId();
        } else {
            principalId = parameters.getPermission().getAdElementId();
        }

        final Permission paramPermission = parameters.getPermission();

        Permission permission =
                permissionDao.getForRoleAndAdElementAndObject(paramPermission.getRoleId(), principalId,
                        paramPermission.getObjectId());

        if (permission == null) {
            paramPermission.setAdElementId(principalId);

            TransactionSupport.executeInNewTransaction(() -> {
                permissionDao.save(paramPermission);
                getCompensationContext().snapshotNewEntity(paramPermission);
                getCompensationContext().stateChanged();
                return null;
            });
            permission = paramPermission;
        }

        getReturnValue().setActionReturnValue(permission.getId());

        if (user != null) {
            updateAdminStatus(permission);
        }
        vmStaticDao.incrementDbGeneration(paramPermission.getObjectId());
        setSucceeded(true);
    }

    private void updateAdminStatus(Permission perm) {
        // if the role of the permission is of type admin update the user
        // lastAdminCheckStatus to true
        Role role = roleDao.get(perm.getRoleId());
        if (role.getType() == RoleType.ADMIN) {
            multiLevelAdministrationHandler.setIsAdminGUIFlag(perm.getAdElementId(), true);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_PERMISSION : AuditLogType.USER_ADD_PERMISSION_FAILED;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        Permission permission = getParameters().getPermission();
        List<PermissionSubject> permissionsSubject = new ArrayList<>();
        permissionsSubject.add(new PermissionSubject(permission.getObjectId(),
                permission.getObjectType(),
                getActionType().getActionGroup()));
        initUserAndGroupData();
        // if the user does not exist in the database we need to
        // check if the logged in user has permissions to add another
        // user from the directory service
        if ((getParameters().getUser() != null && dbUser == null)
                || (getParameters().getGroup() != null && dbGroup == null)) {
           permissionsSubject.add(new PermissionSubject(permission.getObjectId(),
                    permission.getObjectType(), ActionGroup.ADD_USERS_AND_GROUPS_FROM_DIRECTORY));
        }
        return permissionsSubject;
    }

    private DbUser addUser(DbUser dbUser) {
        // Try to add the user with the external id:
        if (dbUser.getDomain() != null && dbUser.getExternalId() != null) {
            AddUserParameters parameters = new AddUserParameters(dbUser);
            ActionReturnValue result = runInternalAction(ActionType.AddUser, parameters, cloneContextAndDetachFromParent());
            if (result.getSucceeded()) {
                Guid id = result.getActionReturnValue();
                if (id != null) {
                    return dbUserDao.get(id);
                }
                return null;
            }
        }
        // There is no such user in the directory:
        return null;
    }

    private DbGroup addGroup(DbGroup groupToAdd) {
        // Try to add the user with the external id:
        if (groupToAdd.getDomain() != null && groupToAdd.getExternalId() != null) {
            AddGroupParameters parameters = new AddGroupParameters(groupToAdd);
            ActionReturnValue
                    result = runInternalAction(ActionType.AddGroup, parameters, cloneContextAndDetachFromParent());
            if (result.getSucceeded()) {
                Guid id = result.getActionReturnValue();
                if (id != null) {
                    return dbGroupDao.get(id);
                }
                return null;
            }
        }
        // There is no such group in the directory:
        return null;
    }

}

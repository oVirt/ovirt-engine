package org.ovirt.engine.core.bll.profiles;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.PredefinedRoles;
import org.ovirt.engine.core.bll.ValidateSupportsTransaction;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CpuProfileParameters;
import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.profiles.CpuProfileDao;
import org.ovirt.engine.core.dao.profiles.ProfilesDao;
import org.ovirt.engine.core.di.Injector;

@ValidateSupportsTransaction
public class AddCpuProfileCommand extends AddProfileCommandBase<CpuProfileParameters, CpuProfile, CpuProfileValidator> {

    @Inject
    private CpuProfileDao cpuProfileDao;

    public AddCpuProfileCommand(CpuProfileParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected CpuProfileValidator getProfileValidator() {
        return Injector.injectMembers(new CpuProfileValidator(getProfile()));
    }

    @Override
    protected ProfilesDao<CpuProfile> getProfileDao() {
        return cpuProfileDao;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getProfile() != null ? getParameters().getProfile()
                .getClusterId()
                : null,
                VdcObjectType.Cluster,
                getActionType().getActionGroup()));
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__TYPE__CPU_PROFILE);
    }

    @Override
    protected void addPermissions() {
        PermissionsOperationsParameters permissionsOperationsParameters = createPermissionParameters(MultiLevelAdministrationHandler.EVERYONE_OBJECT_ID,
                                                                                                     PredefinedRoles.CPU_PROFILE_OPERATOR.getId());
        backend.runInternalAction(ActionType.AddPermission,
                permissionsOperationsParameters,
                cloneContext().withoutExecutionContext().withoutLock());

        permissionsOperationsParameters = createPermissionParameters(getUserId(), PredefinedRoles.CPU_PROFILE_CREATOR.getId());
        backend.runInternalAction(ActionType.AddPermission,
                permissionsOperationsParameters,
                cloneContext().withoutExecutionContext().withoutLock());

    }

    private PermissionsOperationsParameters createPermissionParameters(Guid userId, Guid roleId) {
        Permission permission = new Permission(userId, roleId, getProfileId(), VdcObjectType.CpuProfile);
        permission.setObjectName(getParameters().getProfile().getName());
        if (MultiLevelAdministrationHandler.EVERYONE_OBJECT_ID.equals(userId)) {
            permission.setAuthz("N/A");
        }

        PermissionsOperationsParameters permissionsOperationsParameters = new PermissionsOperationsParameters(permission);
        permissionsOperationsParameters.setParametersCurrentUser(getCurrentUser());
        permissionsOperationsParameters.setSessionId(getContext().getEngineContext().getSessionId());

        return permissionsOperationsParameters;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADDED_CPU_PROFILE : AuditLogType.USER_FAILED_TO_ADD_CPU_PROFILE;
    }
}

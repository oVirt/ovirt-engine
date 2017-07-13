package org.ovirt.engine.core.bll.profiles;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.PredefinedRoles;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.DiskProfileParameters;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.profiles.DiskProfileDao;
import org.ovirt.engine.core.dao.profiles.ProfilesDao;
import org.ovirt.engine.core.di.Injector;

public class AddDiskProfileCommand extends AddProfileCommandBase<DiskProfileParameters, DiskProfile, DiskProfileValidator> {

    @Inject
    private DiskProfileDao diskProfileDao;

    @Inject
    private MultiLevelAdministrationHandler multiLevelAdministrationHandler;

    public AddDiskProfileCommand(DiskProfileParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected DiskProfileValidator getProfileValidator() {
        return Injector.injectMembers(new DiskProfileValidator(getProfile()));
    }

    @Override
    protected ProfilesDao<DiskProfile> getProfileDao() {
        return diskProfileDao;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getProfile() != null ? getParameters().getProfile()
                .getStorageDomainId()
                : null,
                VdcObjectType.Storage, getActionType().getActionGroup()));
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__TYPE__DISK_PROFILE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADDED_DISK_PROFILE : AuditLogType.USER_FAILED_TO_ADD_DISK_PROFILE;
    }

    @Override
    protected void addPermissions() {
        multiLevelAdministrationHandler.addPermission(new Permission(MultiLevelAdministrationHandler.EVERYONE_OBJECT_ID,
                PredefinedRoles.DISK_PROFILE_USER.getId(),
                getProfileId(),
                VdcObjectType.DiskProfile));
    }
}

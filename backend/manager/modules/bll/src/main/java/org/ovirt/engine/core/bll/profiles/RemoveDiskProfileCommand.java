package org.ovirt.engine.core.bll.profiles;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.DiskProfileParameters;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.profiles.ProfilesDao;

public class RemoveDiskProfileCommand extends RemoveProfileCommandBase<DiskProfileParameters, DiskProfile, DiskProfileValidator> {

    public RemoveDiskProfileCommand(DiskProfileParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected DiskProfileValidator getProfileValidator() {
        return new DiskProfileValidator(getProfile());
    }

    @Override
    protected ProfilesDao<DiskProfile> getProfileDao() {
        return getDiskProfileDao();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getProfileId(),
                VdcObjectType.DiskProfile, getActionType().getActionGroup()));
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__TYPE__DISK_PROFILE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVED_DISK_PROFILE
                : AuditLogType.USER_FAILED_TO_REMOVE_DISK_PROFILE;
    }

}

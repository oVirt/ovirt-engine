package org.ovirt.engine.core.bll.profiles;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.DiskProfileParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class UpdateDiskProfileCommand extends DiskProfileCommandBase {

    public UpdateDiskProfileCommand(DiskProfileParameters parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        DiskProfileValidator validator = new DiskProfileValidator(getParameters().getProfile());
        return validate(validator.diskProfileIsSet())
                && validate(validator.diskProfileExists())
                && validate(validator.diskProfileNameNotUsed())
                && validate(validator.storageDomainNotChanged())
                && validate(validator.qosExistsOrNull());
    }

    @Override
    protected void executeCommand() {
        getDiskProfileDao().update(getParameters().getProfile());
        getReturnValue().setActionReturnValue(getParameters().getProfile().getId());
        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getProfileId(),
                VdcObjectType.DiskProfile, getActionType().getActionGroup()));
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATED_DISK_PROFILE
                : AuditLogType.USER_FAILED_TO_UPDATE_DISK_PROFILE;
    }
}

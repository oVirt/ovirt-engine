package org.ovirt.engine.core.bll.profiles;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.DiskProfileParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class RemoveDiskProfileCommand extends DiskProfileCommandBase {

    public RemoveDiskProfileCommand(DiskProfileParameters parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        DiskProfileValidator validator = new DiskProfileValidator(getProfile());
        return validate(validator.diskProfileIsSet())
                && validate(validator.diskProfileExists());
    }

    @Override
    protected void executeCommand() {
        getDbFacade().getDiskProfileDao().remove(getParameters().getProfileId());
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
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVED_DISK_PROFILE
                : AuditLogType.USER_FAILED_TO_REMOVE_DISK_PROFILE;
    }
}

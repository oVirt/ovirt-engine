package org.ovirt.engine.core.bll.profiles;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.DiskProfileParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;

public class AddDiskProfileCommand extends DiskProfileCommandBase {

    public AddDiskProfileCommand(DiskProfileParameters parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        DiskProfileValidator validator = new DiskProfileValidator(getParameters().getProfile());
        return validate(validator.diskProfileIsSet())
                && validate(validator.storageDomainExists())
                && validate(validator.qosExistsOrNull())
                && validate(validator.diskProfileNameNotUsed());
    }

    @Override
    protected void executeCommand() {
        getParameters().getProfile().setId(Guid.newGuid());
        getDiskProfileDao().save(getParameters().getProfile());
        getReturnValue().setActionReturnValue(getParameters().getProfile().getId());
        setSucceeded(true);
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
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADDED_DISK_PROFILE : AuditLogType.USER_FAILED_TO_ADD_DISK_PROFILE;
    }
}

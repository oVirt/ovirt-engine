package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.UserProfileParameters;
import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;


public class AddUserProfileCommand<T extends UserProfileParameters> extends UserProfilesOperationCommandBase<T> {
    public AddUserProfileCommand(T parameters) {
        this(parameters, null);
    }

    public AddUserProfileCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        if (userProfileDao.getByUserId(getUserId()) != null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_PROFILE_ALREADY_EXISTS);
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_PROFILE : AuditLogType.USER_ADD_PROFILE_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__USER_PROFILE);
    }

    @Override
    protected void executeCommand() {
        UserProfile profile = getParameters().getUserProfile();
        profile.setId(Guid.newGuid());
        profile.setUserId(getUserId()); /* must be overridden */
        userProfileDao.save(profile);
        setSucceeded(true);
    }
}

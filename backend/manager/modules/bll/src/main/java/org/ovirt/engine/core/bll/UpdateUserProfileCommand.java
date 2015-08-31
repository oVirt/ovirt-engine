package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.UserProfileParameters;
import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class UpdateUserProfileCommand<T extends UserProfileParameters> extends UserProfilesOperationCommandBase<T> {

    public UpdateUserProfileCommand(T parameters) {
        this(parameters, null);
    }

    public UpdateUserProfileCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        if (getUserProfile() == null) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_PROFILE_NOT_EXIST);
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_PROFILE : AuditLogType.USER_UPDATE_PROFILE_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(EngineMessage.VAR__ACTION__UPDATE);
        addCanDoActionMessage(EngineMessage.VAR__TYPE__USER_PROFILE);
    }

    @Override
    protected void executeCommand() {
        UserProfile profile = getUserProfile();
        /* we want to update only the SSH key right now, so discard anything else */
        profile.setSshPublicKeyId(getParameters().getUserProfile().getSshPublicKeyId());
        profile.setSshPublicKey(getParameters().getUserProfile().getSshPublicKey());
        userProfileDao.update(profile);
        setSucceeded(true);
    }
}

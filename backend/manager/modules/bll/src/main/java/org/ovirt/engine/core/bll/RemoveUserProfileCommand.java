package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.UserProfileParameters;
import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class RemoveUserProfileCommand<T extends UserProfileParameters> extends UserProfilesOperationCommandBase<T> {

    public RemoveUserProfileCommand(T parameters){
        this(parameters, null);
    }

    public RemoveUserProfileCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean canDoAction() {
        if (getUserProfile() == null) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_PROFILE_NOT_EXIST);
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_PROFILE : AuditLogType.USER_REMOVE_PROFILE_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(EngineMessage.VAR__ACTION__REMOVE);
        addCanDoActionMessage(EngineMessage.VAR__TYPE__USER_PROFILE);
    }

    @Override
    protected void executeCommand() {
        UserProfile profile = getUserProfile();
        userProfileDao.remove(profile.getId());
        setSucceeded(true);
    }
}

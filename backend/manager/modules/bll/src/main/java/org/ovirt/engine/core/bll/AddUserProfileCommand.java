package org.ovirt.engine.core.bll;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.UserProfileParameters;
import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
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

        if (getUserProfile() != null) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_PROFILE_ALREADY_EXISTS);
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_PROFILE : AuditLogType.USER_ADD_PROFILE_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(EngineMessage.VAR__ACTION__ADD);
        addCanDoActionMessage(EngineMessage.VAR__TYPE__USER_PROFILE);
    }

    @Override
    protected void executeCommand() {
        UserProfile profile = getParameters().getUserProfile();
        profile.setId(Guid.newGuid());
        profile.setUserId(getUserId()); /* must be overridden */
        if (!StringUtils.isEmpty(profile.getSshPublicKey()) && Guid.isNullOrEmpty(profile.getSshPublicKeyId())) {
            profile.setSshPublicKeyId(Guid.newGuid());
        }
        userProfileDao.save(profile);
        setSucceeded(true);
    }
}

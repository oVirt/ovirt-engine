package org.ovirt.engine.core.bll;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.UserProfileParameters;
import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;


public class AddUserProfileCommand<T extends UserProfileParameters> extends UserProfilesOperationCommandBase<T> {

    public AddUserProfileCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        UserProfile existingProfile = getUserProfile();
        if (existingProfile != null && !isSSHPublicKeyUpdate(existingProfile)) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_PROFILE_ALREADY_EXISTS);
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        UserProfile existingProfile = getUserProfile();
        if (existingProfile != null) { /* same as executeCommand below */
            return getSucceeded() ? AuditLogType.USER_UPDATE_PROFILE : AuditLogType.USER_UPDATE_PROFILE_FAILED;
        } else {
            return getSucceeded() ? AuditLogType.USER_ADD_PROFILE : AuditLogType.USER_ADD_PROFILE_FAILED;
        }
    }

    @Override
    protected void setActionMessageParameters() {
        UserProfile existingProfile = getUserProfile();
        if (existingProfile != null && isSSHPublicKeyUpdate(existingProfile)) {
            addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
        } else {
            addValidationMessage(EngineMessage.VAR__ACTION__ADD);
        }
        addValidationMessage(EngineMessage.VAR__TYPE__USER_PROFILE);
    }

    @Override
    protected void executeCommand() {
        UserProfile existingProfile = getUserProfile();
        if (existingProfile != null) {
            /*
             * if validate() allowed us to get this far, then we must expect
             * isSSHPublicKeyUpdate(existingProfile) == true
             */
            executeCommandUpdateSSHPublicKey(existingProfile);
        } else {
            UserProfile profile = getParameters().getUserProfile();
            profile.setId(Guid.newGuid());
            profile.setUserId(getUserId()); /* must be overridden */
            if (!StringUtils.isEmpty(profile.getSshPublicKey())) {
                profile.setSshPublicKeyId(Guid.newGuid());
            }
            userProfileDao.save(profile);
            setSucceeded(true);
        }
    }

    private boolean isSSHPublicKeyUpdate(UserProfile existingProfile) {
        /* fake Add on key which was wiped previously (rhbz#1283499) */
        if (StringUtils.isEmpty(existingProfile.getSshPublicKey()) &&
                !StringUtils.isEmpty(getParameters().getUserProfile().getSshPublicKey())) {
            return true;
        }
        return false;
    }
}

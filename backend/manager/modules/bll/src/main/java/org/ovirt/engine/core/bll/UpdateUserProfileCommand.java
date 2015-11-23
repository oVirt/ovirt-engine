package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.UserProfileParameters;
import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class UpdateUserProfileCommand<T extends UserProfileParameters> extends UserProfilesOperationCommandBase<T> {

    public UpdateUserProfileCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        if (getUserProfile() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_PROFILE_NOT_EXIST);
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_PROFILE : AuditLogType.USER_UPDATE_PROFILE_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
        addValidationMessage(EngineMessage.VAR__TYPE__USER_PROFILE);
    }

    @Override
    protected void executeCommand() {
        UserProfile profile = getUserProfile();
        profile.setUserPortalVmLoginAutomatically(getParameters().getUserProfile().isUserPortalVmLoginAutomatically());
        executeCommandUpdateSSHPublicKey(profile);
    }
}

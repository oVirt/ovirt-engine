package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.UserProfileParameters;
import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.UserProfileDao;
import org.ovirt.engine.core.uutils.ssh.OpenSSHUtils;


public abstract class UserProfilesOperationCommandBase<T extends UserProfileParameters> extends CommandBase<T> {

    protected UserProfile cachedUserProfile;

    @Inject
    protected UserProfileDao userProfileDao;

    public UserProfilesOperationCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validate() {
        Guid userId = getParameters().getUserProfile().getUserId();

        if (Guid.isNullOrEmpty(userId)) {
            // null/Empty GUID == current user. Fix parameters for later use (e.g. subclasses).
            userId = getUserId();
            getParameters().getUserProfile().setUserId(userId);
        } else if (!userId.equals(getUserId())) {
            return failValidation(EngineMessage.USER_NOT_AUTHORIZED_TO_PERFORM_ACTION);
        }

        String sshPublicKey = getParameters().getUserProfile().getSshPublicKey();

        if (sshPublicKey == null || sshPublicKey.isEmpty()) {
            // the user wants to wipe out its own key, and we're fine with that.
            return true;
        }

        // else it is either a new or replacement key. In both cases, must be a valid key.
        if (!OpenSSHUtils.arePublicKeysValid(sshPublicKey)) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_INVALID_PUBLIC_SSH_KEY);
        }

        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getUserId(),
                                                               VdcObjectType.System,
                                                               getActionType().getActionGroup()));
    }

    protected UserProfile getUserProfile() {
        if (cachedUserProfile == null) {
            cachedUserProfile = userProfileDao.getByUserId(getUserId());
        }
        return cachedUserProfile;
    }

    protected void executeCommandUpdateSSHPublicKey(UserProfile existingProfile) {
        UserProfile newProfile = getParameters().getUserProfile();
        existingProfile.setSshPublicKey(newProfile.getSshPublicKey());
        /* backend must make sure that the ID changes each time the key content changes */
        existingProfile.setSshPublicKeyId(Guid.newGuid());
        userProfileDao.update(existingProfile);
        setSucceeded(true);
    }
}

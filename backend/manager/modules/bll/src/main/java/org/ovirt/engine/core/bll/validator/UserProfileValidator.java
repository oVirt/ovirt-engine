package org.ovirt.engine.core.bll.validator;

import java.util.Objects;
import java.util.function.Function;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.uutils.ssh.OpenSSHUtils;

public class UserProfileValidator {

    public ValidationResult propertyProvided(UserProfileProperty property) {
        return ValidationResult
                .failWith(EngineMessage.ACTION_TYPE_FAILED_USER_PROFILE_PROPERTY_NOT_EXISTS)
                .when(property == null);
    }

    public ValidationResult authorized(DbUser currentUser, Guid targetUserId) {
        return ValidationResult
                .failWith(EngineMessage.USER_NOT_AUTHORIZED_TO_PERFORM_ACTION)
                .when(currentUser == null
                        || currentUser.getId() == null
                        || !Objects.equals(currentUser.getId(), targetUserId) && !currentUser.isAdmin());
    }

    public ValidationResult validPublicSshKey(UserProfileProperty property) {
        return ValidationResult
                .failWith(EngineMessage.ACTION_TYPE_FAILED_INVALID_PUBLIC_SSH_KEY)
                .when(property.isSshPublicKey() &&
                        (property.getContent() == null || !OpenSSHUtils.arePublicKeysValid(property.getContent())));
    }

    public ValidationResult sameOwner(Guid currentId, Guid targetId) {
        return ValidationResult
                .failWith(EngineMessage.USER_NOT_AUTHORIZED_TO_PERFORM_ACTION)
                .when(!Objects.equals(currentId, targetId));
    }

    public ValidationResult firstPublicSshKey(UserProfile profile, UserProfileProperty newProp) {
        return ValidationResult
                .failWith(EngineMessage.ACTION_TYPE_FAILED_USER_PROFILE_MULTIPLE_SSH_KEYS)
                .when(!profile.getSshProperties().isEmpty() && newProp.isSshPublicKey());
    }

    public boolean validateAdd(UserProfile existingProfile,
            UserProfileProperty newProp,
            DbUser currentUser,
            Function<ValidationResult, Boolean> validate) {
        return validate.apply(authorized(currentUser, newProp.getUserId())) &&
                validate.apply(sameOwner(existingProfile.getUserId(), newProp.getUserId())) &&
                validate.apply(validPublicSshKey(newProp)) &&
                validate.apply(firstPublicSshKey(existingProfile, newProp));
    }

    public boolean validateUpdate(UserProfileProperty currentProp,
            DbUser currentUser,
            Function<ValidationResult, Boolean> validate,
            UserProfileProperty update) {
        return validate.apply(propertyProvided(currentProp)) &&
                validate.apply(sameOwner(currentProp.getUserId(), update.getUserId())) &&
                validate.apply(authorized(currentUser, update.getUserId())) &&
                validate.apply(validPublicSshKey(update));
    }

}

package org.ovirt.engine.core.bll.validator;

import java.util.Objects;
import java.util.function.Function;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.uutils.ssh.OpenSSHUtils;

public class UserProfileValidator {

    private static final String VAR_PROP_ID = "propId";
    private static final String VAR_PROP_NAME = "propName";
    private static final String VAR_OTHER_PROP_NAME = "otherPropName";
    private static final String VAR_PROP_TYPE = "propType";
    private static final String VAR_OTHER_PROP_TYPE = "otherPropType";

    public ValidationResult firstPropertyWithGivenName(String newPropertyName, UserProfile existingProfile) {
        return existingProfile.getProperties().stream()
                .filter(prop -> prop.getName().equals(newPropertyName))
                .findFirst()
                .map(prop ->
                        ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_USER_PROFILE_PROPERTY_ALREADY_EXISTS,
                                ReplacementUtils.createSetVariableString(VAR_PROP_ID, prop.getPropertyId()),
                                ReplacementUtils.createSetVariableString(VAR_PROP_NAME, prop.getName()),
                                ReplacementUtils.createSetVariableString(VAR_PROP_TYPE, prop.getType()))
                                // always fail when duplicate exists
                                .when(true))
                .orElse(ValidationResult.VALID);
    }

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

    public ValidationResult sameType(UserProfileProperty.PropertyType existing, UserProfileProperty.PropertyType update, Guid id) {
        return ValidationResult
                .failWith(EngineMessage.ACTION_TYPE_FAILED_USER_PROFILE_PROPERTY_ALREADY_EXIST_WITH_DIFFERENT_TYPE,
                        ReplacementUtils.createSetVariableString(VAR_PROP_ID, id),
                        ReplacementUtils.createSetVariableString(VAR_PROP_TYPE, existing),
                        ReplacementUtils.createSetVariableString(VAR_OTHER_PROP_TYPE, update))
                .when(!Objects.equals(existing, update));
    }

    public ValidationResult sameName(String existing, String update, Guid id) {
        return ValidationResult
                .failWith(EngineMessage.ACTION_TYPE_FAILED_USER_PROFILE_PROPERTY_ALREADY_EXIST_WITH_DIFFERENT_NAME,
                        ReplacementUtils.createSetVariableString(VAR_PROP_ID, id),
                        ReplacementUtils.createSetVariableString(VAR_PROP_NAME, existing),
                        ReplacementUtils.createSetVariableString(VAR_OTHER_PROP_NAME, update))
                .when(!Objects.equals(existing, update));
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
                // /sshpublickeys endpoint is not aware of names so first check by SSH type
                // attempt to create second SSH key should fail with precise message
                validate.apply(firstPublicSshKey(existingProfile, newProp)) &&
                validate.apply(firstPropertyWithGivenName(newProp.getName(), existingProfile));
    }

    public boolean validateUpdate(UserProfileProperty currentProp,
            DbUser currentUser,
            Function<ValidationResult, Boolean> validate,
            UserProfileProperty update) {
        return validate.apply(propertyProvided(currentProp)) &&
                validate.apply(sameOwner(currentProp.getUserId(), update.getUserId())) &&
                validate.apply(authorized(currentUser, update.getUserId())) &&
                validate.apply(validPublicSshKey(update)) &&
                validate.apply(sameName(currentProp.getName(), update.getName(), currentProp.getPropertyId())) &&
                validate.apply(sameType(currentProp.getType(), update.getType(), currentProp.getPropertyId()));
    }
}

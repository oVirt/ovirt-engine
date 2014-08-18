package org.ovirt.engine.core.bll.profiles;

import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.profiles.ProfileBase;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.profiles.ProfilesDao;
import org.ovirt.engine.core.utils.ReplacementUtils;

public abstract class ProfileValidator<T extends ProfileBase> {

    private final Guid profileId;
    private T profile;
    private T profileFromDb;

    public ProfileValidator(T profile) {
        this(profile != null ? profile.getId() : null);
        this.profile = profile;
    }

    public ProfileValidator(Guid profileId) {
        this.profileId = profileId;
    }

    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    public ValidationResult profileIsSet() {
        return getProfile() == null
                ? new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_PROFILE_MISSING)
                : ValidationResult.VALID;
    }

    public ValidationResult profileExists() {
        return getProfileFromDb() == null
                ? new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_PROFILE_NOT_EXISTS)
                : ValidationResult.VALID;
    }

    public ValidationResult qosExistsOrNull() {
        return getProfile().getQosId() == null
                || getDbFacade().getStorageQosDao().get(getProfile().getQosId()) != null
                ? ValidationResult.VALID
                : new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_QOS_NOT_FOUND);
    }

    public ValidationResult profileNameNotUsed() {
        for (T profile : getProfilesByParentEntity()) {
            if (profile.getName().equals(getProfile().getName())
                    && !profile.getId().equals(getProfile().getId())) {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_PROFILE_NAME_IN_USE);
            }
        }

        return ValidationResult.VALID;
    }

    protected ValidationResult profileNotUsed(List<? extends Nameable> entities, VdcBllMessages entitiesReplacement) {
        if (entities.isEmpty()) {
            return ValidationResult.VALID;
        }

        Collection<String> replacements = ReplacementUtils.replaceWithNameable("ENTITIES_USING_PROFILE", entities);
        replacements.add(entitiesReplacement.name());
        return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_PROFILE_IN_USE, replacements);
    }

    public T getProfile() {
        if (profile == null) {
            profile = getProfileDao().get(profileId);
        }
        return profile;
    }

    protected T getProfileFromDb() {
        if (profileFromDb == null) {
            profileFromDb = getProfileDao().get(profile.getId());
        }

        return profileFromDb;
    }

    public abstract ValidationResult parentEntityExists();

    public abstract ValidationResult parentEntityNotChanged();

    public abstract ValidationResult isParentEntityValid(Guid parentEntityId);

    public abstract ValidationResult isLastProfileInParentEntity();

    protected abstract List<T> getProfilesByParentEntity();

    protected abstract ProfilesDao<T> getProfileDao();
}

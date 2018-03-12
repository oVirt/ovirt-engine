package org.ovirt.engine.core.bll.profiles;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.profiles.ProfileBase;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.profiles.ProfilesDao;
import org.ovirt.engine.core.dao.qos.StorageQosDao;
import org.ovirt.engine.core.utils.ReplacementUtils;

public abstract class ProfileValidator<T extends ProfileBase> {
    @Inject
    private StorageQosDao storageQosDao;

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

    public ValidationResult profileIsSet() {
        return getProfile() == null
                ? new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PROFILE_MISSING)
                : ValidationResult.VALID;
    }

    public ValidationResult profileExists() {
        return getProfileFromDb() == null
                ? new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PROFILE_NOT_EXISTS)
                : ValidationResult.VALID;
    }

    public ValidationResult qosExistsOrNull() {
        return getProfile().getQosId() == null
                || storageQosDao.get(getProfile().getQosId()) != null
                ? ValidationResult.VALID
                : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_QOS_NOT_FOUND);
    }

    public ValidationResult profileNameNotUsed() {
        for (T profile : getProfilesByParentEntity()) {
            if (profile.getName().equals(getProfile().getName())
                    && !profile.getId().equals(getProfile().getId())) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PROFILE_NAME_IN_USE);
            }
        }

        return ValidationResult.VALID;
    }

    public ValidationResult profileNotUsed() {
        ValidationResult vmsErrorMsg =
                profileNotUsed(getVmsUsingProfile(),
                        EngineMessage.VAR__ENTITIES__VMS);
        if (!vmsErrorMsg.isValid()) {
            return vmsErrorMsg;
        }
        return profileNotUsed(getTemplatesUsingProfile(),
                EngineMessage.VAR__ENTITIES__VM_TEMPLATES);
    }

    protected ValidationResult profileNotUsed(List<? extends Nameable> entities, EngineMessage entitiesReplacement) {
        if (entities.isEmpty()) {
            return ValidationResult.VALID;
        }

        Collection<String> replacements = ReplacementUtils.replaceWithNameable("ENTITIES_USING_PROFILE", entities);
        replacements.add(entitiesReplacement.name());
        return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PROFILE_IN_USE, replacements);
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

    public abstract ValidationResult isLastProfileInParentEntity();

    protected abstract List<T> getProfilesByParentEntity();

    protected abstract ProfilesDao<T> getProfileDao();

    protected abstract List<VmTemplate> getTemplatesUsingProfile();

    protected abstract List<VM> getVmsUsingProfile();
}

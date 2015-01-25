package org.ovirt.engine.core.bll.profiles;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.profiles.DiskProfileDao;


public class DiskProfileValidator extends ProfileValidator<DiskProfile> {
    private StorageDomain storageDomain;

    public DiskProfileValidator(DiskProfile profile) {
        super(profile);
    }

    public DiskProfileValidator(Guid profileId) {
        super(profileId);
    }

    @Override
    public ValidationResult parentEntityExists() {
        return new StorageDomainValidator(getStorageDomain()).isDomainExist();
    }

    @Override
    public ValidationResult parentEntityNotChanged() {
        if (ObjectUtils.equals(getProfile().getStorageDomainId(), getProfileFromDb().getStorageDomainId())) {
            return ValidationResult.VALID;
        }

        return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_CHANGE_PROFILE);
    }

    protected StorageDomain getStorageDomain() {
        if (storageDomain == null) {
            storageDomain = getDbFacade().getStorageDomainDao().get(getProfile().getStorageDomainId());
        }

        return storageDomain;
    }

    @Override
    public ValidationResult isParentEntityValid(Guid storageDomainId) {
        if (storageDomainId == null) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_DISK_PROFILE_STORAGE_DOMAIN_NOT_PROVIDED);
        }
        Guid id = getProfile().getId();
        if (id == null) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_DISK_PROFILE_EMPTY);
        }
        DiskProfile fetchedDiskProfile = getProfileDao().get(id);
        if (fetchedDiskProfile == null) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISK_PROFILE_NOT_FOUND);
        }
        if (!storageDomainId.equals(fetchedDiskProfile.getStorageDomainId())) {
            String diskProfileName = fetchedDiskProfile.getName();
            String targetStorageDomainName = getStorageDomain().getName();
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_DISK_PROFILE_NOT_MATCH_STORAGE_DOMAIN,
                    String.format("$diskProfile %s", diskProfileName),
                    String.format("$storageDomain %s", targetStorageDomainName));
        }
        return ValidationResult.VALID;
    }

    @Override
    public ValidationResult isLastProfileInParentEntity() {
        if (getProfileDao().getAllForStorageDomain(getProfile().getStorageDomainId()).size() == 1) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_CANNOT_REMOVE_LAST_DISK_PROFILE_IN_STORAGE_DOMAIN);
        }
        return ValidationResult.VALID;
    }

    @Override
    protected DiskProfileDao getProfileDao() {
        return getDbFacade().getDiskProfileDao();
    }

    @Override
    protected List<DiskProfile> getProfilesByParentEntity() {
        return getDbFacade().getDiskProfileDao().getAllForStorageDomain(getStorageDomain().getId());
    }
}

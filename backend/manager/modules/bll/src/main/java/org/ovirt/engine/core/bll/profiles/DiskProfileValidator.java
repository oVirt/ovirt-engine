package org.ovirt.engine.core.bll.profiles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.profiles.DiskProfileDao;
import org.ovirt.engine.core.utils.ReplacementUtils;


public class DiskProfileValidator extends ProfileValidator<DiskProfile> {
    private StorageDomain storageDomain;

    @Inject
    private StorageDomainDao storageDomainDao;

    @Inject
    private DiskProfileDao diskProfileDao;

    @Inject
    private VmTemplateDao vmTemplateDao;

    @Inject
    private VmDao vmDao;

    @Inject
    private DiskImageDao diskImageDao;

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
        if (Objects.equals(getProfile().getStorageDomainId(), getProfileFromDb().getStorageDomainId())) {
            return ValidationResult.VALID;
        }

        return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_CANNOT_CHANGE_PROFILE);
    }

    protected StorageDomain getStorageDomain() {
        if (storageDomain == null) {
            storageDomain = getStorageDomain(getProfile().getStorageDomainId());
        }
        return storageDomain;
    }

    protected StorageDomain getStorageDomain(Guid sdGuid) {
        if (sdGuid != null) {
            return storageDomainDao.get(sdGuid);
        }
        return null;
    }

    public ValidationResult isParentEntityValid(Guid storageDomainId) {
        if (storageDomainId == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_DISK_PROFILE_STORAGE_DOMAIN_NOT_PROVIDED);
        }
        Guid id = getProfile().getId();
        if (id == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_DISK_PROFILE_EMPTY);
        }
        DiskProfile fetchedDiskProfile = getProfileDao().get(id);
        if (fetchedDiskProfile == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_PROFILE_NOT_FOUND);
        }
        if (!storageDomainId.equals(fetchedDiskProfile.getStorageDomainId())) {
            String diskProfileName = fetchedDiskProfile.getName();
            StorageDomain targetStorageDomain = getStorageDomain(storageDomainId);
            String targetStorageDomainName = targetStorageDomain != null ? getStorageDomain(storageDomainId).getName() : "Unknown";
            return new ValidationResult(EngineMessage.ACTION_TYPE_DISK_PROFILE_NOT_MATCH_STORAGE_DOMAIN,
                    String.format("$diskProfile %s", diskProfileName),
                    String.format("$diskProfileId %s", fetchedDiskProfile.getId().toString()),
                    String.format("$storageDomain %s", targetStorageDomainName));
        }
        return ValidationResult.VALID;
    }

    @Override
    public ValidationResult isLastProfileInParentEntity() {
        if (getProfileDao().getAllForStorageDomain(getProfile().getStorageDomainId()).size() == 1) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_CANNOT_REMOVE_LAST_DISK_PROFILE_IN_STORAGE_DOMAIN);
        }
        return ValidationResult.VALID;
    }

    @Override
    public ValidationResult profileNotUsed() {
        ValidationResult validationResult = super.profileNotUsed();
        if (!validationResult.isValid()) {
            return validationResult;
        }
        return validateUnattachedDisks();
    }

    private ValidationResult validateUnattachedDisks() {
        List<DiskImage> entities = diskImageDao.getAllForDiskProfiles(Collections.singletonList(getProfile().getId()));
        if (entities.isEmpty()) {
            return ValidationResult.VALID;
        }

        List<Object> nameList = new ArrayList<>();
        for (DiskImage diskImage : entities) {
            nameList.add(diskImage.getDiskAlias());
        }
        Collection<String> replacements = ReplacementUtils.replaceWith("ENTITIES_USING_PROFILE", nameList);
        replacements.add(EngineMessage.VAR__ENTITIES__DISKS.name());
        return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PROFILE_IN_USE, replacements);
    }

    @Override
    protected DiskProfileDao getProfileDao() {
        return diskProfileDao;
    }

    @Override
    protected List<DiskProfile> getProfilesByParentEntity() {
        return diskProfileDao.getAllForStorageDomain(getStorageDomain().getId());
    }

    @Override
    protected List<VmTemplate> getTemplatesUsingProfile() {
        return vmTemplateDao.getAllForDiskProfile(getProfile().getId());
    }

    @Override
    protected List<VM> getVmsUsingProfile() {
        return vmDao.getAllForDiskProfiles(Collections.singletonList(getProfile().getId()));
    }
}

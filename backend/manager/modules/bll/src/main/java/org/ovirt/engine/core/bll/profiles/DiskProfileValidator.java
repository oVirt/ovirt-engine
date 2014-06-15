package org.ovirt.engine.core.bll.profiles;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class DiskProfileValidator {

    private final DiskProfile diskProfile;
    private DiskProfile diskProfileFromDb;
    private StorageDomain storageDomain;
    private List<DiskProfile> diskProfiles;

    public DiskProfileValidator(DiskProfile diskProfile) {
        this.diskProfile = diskProfile;
    }

    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    public ValidationResult diskProfileIsSet() {
        return diskProfile == null
                ? new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_PROFILE_NOT_EXISTS)
                : ValidationResult.VALID;
    }

    public ValidationResult diskProfileExists() {
        return getDiskProfileFromDb() == null
                ? new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_PROFILE_NOT_EXISTS)
                : ValidationResult.VALID;
    }

    public ValidationResult storageDomainExists() {
        return new StorageDomainValidator(getStorageDomain()).isDomainExist();
    }

    public ValidationResult qosExistsOrNull() {
        return diskProfile.getQosId() == null
                || getDbFacade().getStorageQosDao().get(diskProfile.getQosId()) != null
                ? ValidationResult.VALID
                : new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_QOS_NOT_FOUND);
    }

    public ValidationResult diskProfileNameNotUsed() {
        for (DiskProfile profile : getDiskProfiles()) {
            if (profile.getName().equals(diskProfile.getName()) && !profile.getId().equals(diskProfile.getId())) {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_PROFILE_NAME_IN_USE);
            }
        }

        return ValidationResult.VALID;
    }

    public ValidationResult storageDomainNotChanged() {
        if (ObjectUtils.equals(diskProfile.getStorageDomainId(), getDiskProfileFromDb().getStorageDomainId())) {
            return ValidationResult.VALID;
        }

        return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_CHANGE_PROFILE);
    }

    protected ValidationResult diskProfileNotUsed(List<? extends Nameable> entities, VdcBllMessages entitiesReplacement) {
        if (entities.isEmpty()) {
            return ValidationResult.VALID;
        }

        Collection<String> replacements = ReplacementUtils.replaceWithNameable("ENTITIES_USING_PROFILE", entities);
        replacements.add(entitiesReplacement.name());
        return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_PROFILE_IN_USE, replacements);
    }

    protected StorageDomain getStorageDomain() {
        if (storageDomain == null) {
            storageDomain = getDbFacade().getStorageDomainDao().get(diskProfile.getStorageDomainId());
        }

        return storageDomain;
    }

    protected List<DiskProfile> getDiskProfiles() {
        if (diskProfiles == null) {
            diskProfiles = getDbFacade().getDiskProfileDao().getAllForStorageDomain(diskProfile.getStorageDomainId());
        }

        return diskProfiles;
    }

    protected DiskProfile getDiskProfileFromDb() {
        if (diskProfileFromDb == null) {
            diskProfileFromDb = getDbFacade().getDiskProfileDao().get(diskProfile.getId());
        }

        return diskProfileFromDb;
    }
}

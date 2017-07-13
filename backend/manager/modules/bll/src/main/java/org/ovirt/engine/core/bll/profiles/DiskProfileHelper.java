package org.ovirt.engine.core.bll.profiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.profiles.DiskProfileDao;
import org.ovirt.engine.core.di.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DiskProfileHelper {

    private static final Logger log = LoggerFactory.getLogger(DiskProfileHelper.class);

    @Inject
    private DiskProfileDao diskProfileDao;

    @Inject
    private PermissionDao permissionDao;

    public DiskProfile createDiskProfile(Guid storageDomainId, String name) {
        DiskProfile profile = new DiskProfile();
        profile.setId(Guid.newGuid());
        profile.setName(name);
        profile.setStorageDomainId(storageDomainId);
        return profile;
    }

    public ValidationResult setAndValidateDiskProfiles(Map<DiskImage, Guid> map, DbUser user) {
        if (map == null) {
            return ValidationResult.VALID;
        }

        Map<Guid, List<DiskProfile>> storageDiskProfilesMap = new HashMap<>();
        // caching disk profile ids that was already checked.
        Set<Guid> permittedDiskProfilesIds = new HashSet<>();
        for (Entry<DiskImage, Guid> entry : map.entrySet()) {
            DiskImage diskImage = entry.getKey();
            Guid storageDomainId = entry.getValue();
            if (diskImage.getDiskStorageType() != DiskStorageType.IMAGE) {
                log.info("Disk profiles is not supported for storage type '{}' (Disk '{}')",
                        diskImage.getDiskStorageType(), diskImage.getDiskAlias());
                continue;
            }
            if (diskImage.getDiskProfileId() == null && storageDomainId != null) {
                List<DiskProfile> diskProfilesList = storageDiskProfilesMap.get(storageDomainId);
                if (diskProfilesList == null) {
                    diskProfilesList = diskProfileDao.getAllForStorageDomain(storageDomainId);
                    storageDiskProfilesMap.put(storageDomainId, diskProfilesList);
                }
                // Set Disk Profile according to permissions
                if (!updateDiskProfileForBackwardCompatibility(diskImage,
                        diskProfilesList,
                        permittedDiskProfilesIds,
                        user)) {
                    return new ValidationResult(EngineMessage.USER_NOT_AUTHORIZED_TO_ATTACH_DISK_PROFILE);
                }
            } else {
                DiskProfile diskProfile = updateDiskImageProfilesList(diskImage, storageDomainId);
                if (diskProfile == null) {
                    return new ValidationResult(EngineMessage.ACTION_TYPE_DISK_PROFILE_NOT_FOUND_FOR_STORAGE_DOMAIN,
                            String.format("$storageDomainId %s", storageDomainId));
                }
                ValidationResult result = isDiskProfileParentEntityValid(diskProfile, storageDomainId);
                if (result != ValidationResult.VALID) {
                    return result;
                }
                if (!isDiskProfilePermitted(diskProfile, permittedDiskProfilesIds, user)) {
                    return new ValidationResult(EngineMessage.USER_NOT_AUTHORIZED_TO_ATTACH_DISK_PROFILE);
                }
            }
        }

        return ValidationResult.VALID;
    }

    public ValidationResult isDiskProfileParentEntityValid(DiskProfile diskProfile, Guid storageDomainId) {
        return Injector.injectMembers(new DiskProfileValidator(diskProfile)).isParentEntityValid(storageDomainId);
    }

    /**
     * Updates the disk profiles list of the given disk image according to the storageDomainID.
     * The disk profiles list will be set with the first disk profile that matches the storage domain id.
     *
     * @param diskImage       disk image to be updated with the relevant disk profiles list
     * @param storageDomainId storage domain id to match a disk profile with
     * @return valid disk profile in case there is a match with the given storage domain ID. otherwise return an
     * invalid disk profile.
     */
    private DiskProfile updateDiskImageProfilesList(DiskImage diskImage, Guid storageDomainId) {
        DiskProfile diskProfile = null;
        if (storageDomainId != null) {
            List<Guid> diskProfileIds = diskImage.getDiskProfileIds();
            List<DiskProfile> diskProfilesListByStorageDomain =
                    diskProfileDao.getAllForStorageDomain(storageDomainId);
            Optional<DiskProfile> match = diskProfilesListByStorageDomain.stream()
                    .filter(profile -> diskProfileIds.contains(profile.getId()))
                    .findFirst();
            if (match.isPresent()) {
                diskProfile = match.get();
                diskImage.setDiskProfileIds(new ArrayList<Guid>(Arrays.asList(diskProfile.getId())));
            }
        }
        return diskProfile;
    }

    private boolean updateDiskProfileForBackwardCompatibility(DiskImage diskImage,
            List<DiskProfile> diskProfilesList,
            Set<Guid> permittedDiskProfilesIds,
            DbUser user) {
        for (DiskProfile diskProfile : diskProfilesList) {
            if (isDiskProfilePermitted(diskProfile, permittedDiskProfilesIds, user)) {
                permittedDiskProfilesIds.add(diskProfile.getId());
                diskImage.setDiskProfileId(diskProfile.getId());
                return true;
            }
        }
        return false;
    }

    private boolean isDiskProfilePermitted(DiskProfile diskProfile,
            Set<Guid> permittedDiskProfilesIds,
            DbUser user) {
        return user == null
                || permittedDiskProfilesIds.contains(diskProfile.getId())
                || permissionDao.getEntityPermissions(user.getId(),
                        ActionGroup.ATTACH_DISK_PROFILE,
                        diskProfile.getId(),
                        VdcObjectType.DiskProfile) != null;
    }
}

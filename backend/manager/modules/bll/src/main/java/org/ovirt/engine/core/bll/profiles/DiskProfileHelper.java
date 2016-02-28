package org.ovirt.engine.core.bll.profiles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.profiles.DiskProfileDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiskProfileHelper {

    private static final Logger log = LoggerFactory.getLogger(DiskProfileHelper.class);

    private DiskProfileHelper() {

    }

    public static DiskProfile createDiskProfile(Guid storageDomainId, String name) {
        DiskProfile profile = new DiskProfile();
        profile.setId(Guid.newGuid());
        profile.setName(name);
        profile.setStorageDomainId(storageDomainId);
        return profile;
    }

    public static ValidationResult setAndValidateDiskProfiles(Map<DiskImage, Guid> map, DbUser user) {
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
                    diskProfilesList = getDiskProfileDao().getAllForStorageDomain(storageDomainId);
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
                DiskProfile diskProfile = getDiskProfileDao().get(diskImage.getDiskProfileId());
                ValidationResult result =
                        new DiskProfileValidator(diskProfile).isParentEntityValid(storageDomainId);
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

    private static boolean updateDiskProfileForBackwardCompatibility(DiskImage diskImage,
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

    private static boolean isDiskProfilePermitted(DiskProfile diskProfile,
            Set<Guid> permittedDiskProfilesIds,
            DbUser user) {
        return user == null
                || permittedDiskProfilesIds.contains(diskProfile.getId())
                || getPermissionDao().getEntityPermissions(user.getId(),
                        ActionGroup.ATTACH_DISK_PROFILE,
                        diskProfile.getId(),
                        VdcObjectType.DiskProfile) != null;
    }

    private static DiskProfileDao getDiskProfileDao() {
        return DbFacade.getInstance().getDiskProfileDao();
    }

    private static PermissionDao getPermissionDao() {
        return DbFacade.getInstance().getPermissionDao();
    }
}

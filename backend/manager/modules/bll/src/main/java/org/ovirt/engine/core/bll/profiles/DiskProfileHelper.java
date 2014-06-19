package org.ovirt.engine.core.bll.profiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.profiles.DiskProfileDao;

public class DiskProfileHelper {

    private DiskProfileHelper() {

    }

    public static DiskProfile createDiskProfile(Guid storageDomainId, String name) {
        DiskProfile profile = new DiskProfile();
        profile.setId(Guid.newGuid());
        profile.setName(name);
        profile.setStorageDomainId(storageDomainId);
        return profile;
    }

    public static ValidationResult setAndValidateDiskProfiles(Map<DiskImage, Guid> map, Version version) {
        if (map == null || !FeatureSupported.storageQoS(version)) {
            return ValidationResult.VALID;
        }

        Map<Guid, List<DiskProfile>> storageDiskProfilesMap = new HashMap<>();
        for (Entry<DiskImage, Guid> entry : map.entrySet()) {
            DiskImage diskImage = entry.getKey();
            Guid storageDomainId = entry.getValue();
            if (diskImage.getDiskProfileId() == null && storageDomainId != null) { // set disk profile if there's only 1 for SD.
                List<DiskProfile> diskProfilesList = storageDiskProfilesMap.get(storageDomainId);
                if (diskProfilesList == null) {
                    diskProfilesList = getDiskProfileDao().getAllForStorageDomain(storageDomainId);
                    storageDiskProfilesMap.put(storageDomainId, diskProfilesList);
                }
                if (diskProfilesList.size() == 1) {
                    diskImage.setDiskProfileId(diskProfilesList.get(0).getId());
                } else {
                    return new ValidationResult(VdcBllMessages.ACTION_TYPE_DISK_PROFILE_EMPTY);
                }
            } else {
                ValidationResult result =
                        new DiskProfileValidator(getDiskProfileDao().get(diskImage.getDiskProfileId())).isStorageDomainValid(storageDomainId);
                if (result != ValidationResult.VALID) {
                    return result;
                }
            }
        }

        return ValidationResult.VALID;
    }

    private static DiskProfileDao getDiskProfileDao() {
        return DbFacade.getInstance().getDiskProfileDao();
    }
}

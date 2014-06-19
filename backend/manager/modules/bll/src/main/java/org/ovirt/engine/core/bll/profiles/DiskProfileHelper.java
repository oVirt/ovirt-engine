package org.ovirt.engine.core.bll.profiles;

import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.compat.Guid;

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
}

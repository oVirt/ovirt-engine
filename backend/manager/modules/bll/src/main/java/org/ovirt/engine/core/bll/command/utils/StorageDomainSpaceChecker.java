package org.ovirt.engine.core.bll.command.utils;

import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

public class StorageDomainSpaceChecker {

    public static boolean isWithinThresholds(final storage_domains domain) {
        StorageDomainDynamic dynamic = domain.getStorageDynamicData();
        return (dynamic != null && dynamic.getfreeDiskInGB() > getLowDiskSpaceThreshold());
    }

    public static boolean hasSpaceForRequest(final storage_domains domain, final long requestedSize) {
        return domain.getavailable_disk_size() != null
                && domain.getavailable_disk_size() - requestedSize >= getLowDiskSpaceThreshold();
    }

    private static Integer getLowDiskSpaceThreshold() {
        return Config.<Integer> GetValue(ConfigValues.FreeSpaceCriticalLowInGB);
    }
}

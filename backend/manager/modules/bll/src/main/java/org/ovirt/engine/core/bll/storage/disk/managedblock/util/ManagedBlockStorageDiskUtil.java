package org.ovirt.engine.core.bll.storage.disk.managedblock.util;

import javax.ejb.Singleton;
import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.dao.DiskImageDynamicDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;

@Singleton
public class ManagedBlockStorageDiskUtil {

    @Inject
    private ImageStorageDomainMapDao imageStorageDomainMapDao;

    @Inject
    private DiskImageDynamicDao diskImageDynamicDao;

    public void saveDisk(ManagedBlockStorageDisk disk) {
        imageStorageDomainMapDao.save(new ImageStorageDomainMap(disk.getImageId(),
                disk.getStorageIds().get(0),
                disk.getQuotaId(),
                disk.getDiskProfileId()));

        DiskImageDynamic diskDynamic = new DiskImageDynamic();
        diskDynamic.setId(disk.getImageId());
        diskDynamic.setActualSize(disk.getActualSizeInBytes());
        diskImageDynamicDao.save(diskDynamic);
    }
}

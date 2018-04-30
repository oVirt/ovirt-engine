package org.ovirt.engine.core.bll.storage.repoimage;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;

public class ImportExportRepoImageCommandTest extends BaseCommandTest {
    @Mock
    private StorageDomainDao storageDomainDao;

    protected Guid repoStorageDomainId = Guid.newGuid();
    protected Guid storagePoolId = Guid.newGuid();
    protected Guid storageDomainId = Guid.newGuid();
    protected StorageDomain diskStorageDomain;
    protected StoragePool storagePool;
    protected Guid diskImageId = Guid.newGuid();
    protected Guid diskImageGroupId = Guid.newGuid();
    protected DiskImage diskImage;

    @BeforeEach
    public void setUp() {
        StorageDomain imageStorageDomain = new StorageDomain();
        imageStorageDomain.setStorage(Guid.newGuid().toString());

        diskStorageDomain = new StorageDomain();
        diskStorageDomain.setId(storageDomainId);
        diskStorageDomain.setStoragePoolId(storagePoolId);
        diskStorageDomain.setStatus(StorageDomainStatus.Active);

        storagePool  = new StoragePool();
        storagePool.setId(storagePoolId);
        storagePool.setStatus(StoragePoolStatus.Up);

        diskImage = new DiskImage();
        diskImage.setId(diskImageId);
        diskImage.setStorageIds(new ArrayList<>(Collections.singletonList(storageDomainId)));
        diskImage.setStoragePoolId(storagePoolId);
        diskImage.setImageStatus(ImageStatus.OK);

        when(storageDomainDao.getAllForStorageDomain(storageDomainId))
                .thenReturn(Collections.singletonList(diskStorageDomain));
    }
}

package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Before;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.provider.storage.OpenStackImageProviderProxy;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;


public class ImportExportRepoImageCommandTest extends BaseCommandTest {

    @Mock
    private StorageDomainDao storageDomainDao;

    @Mock
    private StoragePoolDao storagePoolDao;

    @Mock
    protected DiskDao diskDao;

    @Mock
    private DiskImageDao diskImageDao;

    @Mock
    protected OpenStackImageProviderProxy providerProxy;

    protected Guid repoStorageDomainId = Guid.newGuid();

    protected Guid storagePoolId = Guid.newGuid();

    protected Guid storageDomainId = Guid.newGuid();

    protected StorageDomain diskStorageDomain;

    protected StoragePool storagePool;

    protected String repoImageId = Guid.newGuid().toString();

    protected Guid diskImageId = Guid.newGuid();

    protected Guid diskImageGroupId = Guid.newGuid();

    protected DiskImage diskImage;

    @Before
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

        when(storageDomainDao.get(repoStorageDomainId)).thenReturn(imageStorageDomain);
        when(storageDomainDao.getAllForStorageDomain(storageDomainId))
                .thenReturn(Collections.singletonList(diskStorageDomain));
        when(storagePoolDao.get(storagePoolId)).thenReturn(storagePool);
        when(diskDao.get(diskImageGroupId)).thenReturn(diskImage);
        when(diskImageDao.get(diskImageId)).thenReturn(diskImage);
        when(providerProxy.getImageAsDiskImage(repoImageId)).thenReturn(diskImage);
    }

}

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
    private DiskDao diskDao;

    @Mock
    private DiskImageDao diskImageDao;

    @Mock
    private OpenStackImageProviderProxy providerProxy;

    private Guid repoStorageDomainId = Guid.newGuid();

    private Guid storagePoolId = Guid.newGuid();

    private Guid storageDomainId = Guid.newGuid();

    private StorageDomain diskStorageDomain;

    private StoragePool storagePool;

    private String repoImageId = Guid.newGuid().toString();

    private Guid diskImageId = Guid.newGuid();

    private Guid diskImageGroupId = Guid.newGuid();

    private DiskImage diskImage;

    public Guid getRepoStorageDomainId() {
        return repoStorageDomainId;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public StorageDomain getDiskStorageDomain() {
        return diskStorageDomain;
    }

    public String getRepoImageId() {
        return repoImageId;
    }

    public StorageDomainDao getStorageDomainDao() {
        return storageDomainDao;
    }

    public StoragePoolDao getStoragePoolDao() {
        return storagePoolDao;
    }

    public OpenStackImageProviderProxy getProviderProxy() {
        return providerProxy;
    }

    public StoragePool getStoragePool() {
        return storagePool;
    }

    public Guid getDiskImageId() {
        return diskImageId;
    }

    public Guid getDiskImageGroupId() {
        return diskImageGroupId;
    }

    public DiskDao getDiskDao() {
        return diskDao;
    }

    public DiskImageDao getDiskImageDao() {
        return diskImageDao;
    }

    public DiskImage getDiskImage() {
        return diskImage;
    }

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

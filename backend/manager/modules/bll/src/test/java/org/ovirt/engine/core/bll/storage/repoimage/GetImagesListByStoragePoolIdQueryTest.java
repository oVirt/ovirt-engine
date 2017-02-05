package org.ovirt.engine.core.bll.storage.repoimage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.queries.GetImagesListByStoragePoolIdParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;

@RunWith(Parameterized.class)
public class GetImagesListByStoragePoolIdQueryTest
        extends AbstractUserQueryTest<GetImagesListByStoragePoolIdParameters, GetImagesListByStoragePoolIdQuery<? extends GetImagesListByStoragePoolIdParameters>> {
    @Mock
    private StorageDomainDao storageDomainDaoMock;

    @Mock
    private StoragePoolDao storagePoolDaoMock;

    @Parameterized.Parameter
    public ImageFileType expectedType;
    private Guid storageDomainId;

    @Parameterized.Parameters
    public static Object[] data() {
        return new ImageFileType[] { ImageFileType.ISO, ImageFileType.Floppy };
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        storageDomainId = Guid.newGuid();
    }

    @Override
    protected void setUpMockQueryParameters() {
        super.setUpMockQueryParameters();
        when(getQueryParameters().getStoragePoolId()).thenReturn(Guid.newGuid());
        when(getQueryParameters().getImageType()).thenReturn(expectedType);
    }

    @Override
    protected void initQuery(GetImagesListByStoragePoolIdQuery<? extends GetImagesListByStoragePoolIdParameters> query) {
        super.initQuery(query);
        RepoImage rfmd = new RepoImage();
        rfmd.setFileType(expectedType);
        doReturn(Collections.singletonList(rfmd)).when(query).getUserRequestForStorageDomainRepoFileList();
    }

    @Test
    public void testGetStorageDomainIdWithPermissions() {
        mockStoragePoolDao(new StoragePool());

        when(storageDomainDaoMock.getIsoStorageDomainIdForPool(getQueryParameters().getStoragePoolId(),
                StorageDomainStatus.Active)).thenReturn(storageDomainId);

        assertEquals("wrong storage domain id", storageDomainId, getQuery().getStorageDomainIdForQuery());
    }

    @Test
    public void testGetStorageDomainIdWithNoPermissions() {
        mockStoragePoolDao(null);

        assertNull("No storage domains should have been returned", getQuery().getStorageDomainIdForQuery());
    }

    /**
     * Mocks the storage pool Dao to return the given storage pool
     * @param sp The storage_pool the Dao should return
     */
    private void mockStoragePoolDao(StoragePool pool) {
        Guid storagePoolId = getQueryParameters().getStoragePoolId();
        if (pool != null) {
            pool.setId(storagePoolId);
        }

        when(storagePoolDaoMock.get(
                storagePoolId,
                getUser().getId(),
                getQueryParameters().isFiltered())).thenReturn(pool);
    }
}

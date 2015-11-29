package org.ovirt.engine.core.bll.storage.repoimage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
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

    private Class<? extends GetImagesListByStoragePoolIdQuery<GetImagesListByStoragePoolIdParameters>> queryClass;
    private ImageFileType expectedType;
    private Guid storageDomainId;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]
        { { GetImagesListByStoragePoolIdQuery.class, ImageFileType.ISO },
                { GetImagesListByStoragePoolIdQuery.class, ImageFileType.Floppy } });
    }

    public GetImagesListByStoragePoolIdQueryTest(Class<? extends GetImagesListByStoragePoolIdQuery<GetImagesListByStoragePoolIdParameters>> queryClass,
            ImageFileType expectedType) {
        this.queryClass = queryClass;
        this.expectedType = expectedType;
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
    protected void setUpSpyQuery() throws Exception {
        super.setUpSpyQuery();
        RepoImage rfmd = new RepoImage();
        rfmd.setFileType(expectedType);
        doReturn(Collections.singletonList(rfmd)).when(getQuery()).getUserRequestForStorageDomainRepoFileList();
    }

    @Override
    protected Class<? extends GetImagesListByStoragePoolIdQuery<? extends GetImagesListByStoragePoolIdParameters>> getQueryType() {
        return queryClass;
    }

    @Test
    public void testGetStorageDomainIdWithPermissions() {
        mockStoragePoolDao(new StoragePool());

        StorageDomainDao storageDomainDaoMock = mock(StorageDomainDao.class);
        when(storageDomainDaoMock.getIsoStorageDomainIdForPool(getQueryParameters().getStoragePoolId(),
                StorageDomainStatus.Active)).thenReturn(storageDomainId);
        when(getDbFacadeMockInstance().getStorageDomainDao()).thenReturn(storageDomainDaoMock);

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

        StoragePoolDao storagePoolDaoMock = mock(StoragePoolDao.class);
        when(storagePoolDaoMock.get(
                storagePoolId,
                getUser().getId(),
                getQueryParameters().isFiltered())).thenReturn(pool);
        when(getDbFacadeMockInstance().getStoragePoolDao()).thenReturn(storagePoolDaoMock);
    }
}

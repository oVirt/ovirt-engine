package org.ovirt.engine.core.bll.storage.repoimage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

public class GetImagesListByStoragePoolIdQueryTest
        extends AbstractUserQueryTest<GetImagesListByStoragePoolIdParameters, GetImagesListByStoragePoolIdQuery<? extends GetImagesListByStoragePoolIdParameters>> {
    @Mock
    private StorageDomainDao storageDomainDaoMock;

    @Mock
    private StoragePoolDao storagePoolDaoMock;

    private Guid storageDomainId;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        storageDomainId = Guid.newGuid();
    }

    @Override
    protected void setUpMockQueryParameters() {
        super.setUpMockQueryParameters();
        when(getQueryParameters().getStoragePoolId()).thenReturn(Guid.newGuid());
    }

    @Override
    protected void initQuery(GetImagesListByStoragePoolIdQuery<? extends GetImagesListByStoragePoolIdParameters> query) {
        super.initQuery(query);
        RepoImage rfmd = new RepoImage();
        doReturn(Collections.singletonList(rfmd)).when(query).getUserRequestForStorageDomainRepoFileList();
    }

    @ParameterizedTest
    @EnumSource(value = ImageFileType.class, names = {"ISO", "Floppy"})
    public void testGetStorageDomainIdWithPermissions(ImageFileType expectedType) {
        when(getQueryParameters().getImageType()).thenReturn(expectedType);
        mockStoragePoolDao(new StoragePool());

        when(storageDomainDaoMock.getIsoStorageDomainIdForPool(getQueryParameters().getStoragePoolId(),
                StorageDomainStatus.Active)).thenReturn(storageDomainId);

        assertEquals(storageDomainId, getQuery().getStorageDomainIdForQuery(), "wrong storage domain id");
    }

    @ParameterizedTest
    @EnumSource(value = ImageFileType.class, names = {"ISO", "Floppy"})
    public void testGetStorageDomainIdWithNoPermissions(ImageFileType expectedType) {
        when(getQueryParameters().getImageType()).thenReturn(expectedType);
        mockStoragePoolDao(null);

        assertNull(getQuery().getStorageDomainIdForQuery(), "No storage domains should have been returned");
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

package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.dao.DuplicateKeyException;

public class RepoFileMetaDataDaoTest extends BaseDaoTestCase<RepoFileMetaDataDao> {
    @Inject
    private StorageDomainDao storageDomainDao;

    /**
     * Ensures that saving a domain works as expected.
     */
    @Test
    public void testSave() {
        // Fetch the file from cache table
        List<RepoImage> listOfRepoFiles =
                dao.getRepoListForStorageDomain(FixturesTool.STORAGE_DOMAIN_NFS_ISO, ImageFileType.ISO);
        assertNotNull(listOfRepoFiles);
        assertTrue(listOfRepoFiles.isEmpty());

        RepoImage newRepoFileMap = getNewIsoRepoFile();
        dao.addRepoFileMap(newRepoFileMap);

        listOfRepoFiles = dao.getRepoListForStorageDomain(FixturesTool.STORAGE_DOMAIN_NFS_ISO, ImageFileType.ISO);
        assertFalse(listOfRepoFiles.isEmpty());
    }

    /**
     * Test remove of repo file from storage domain.
     */
    @Test
    public void testRemove() {
        // Should get one iso file
        List<RepoImage> listOfRepoFiles = dao.getRepoListForStorageDomain
                (FixturesTool.SHARED_ISO_STORAGE_DOMAIN_FOR_SP2_AND_SP3, ImageFileType.ISO);

        assertNotNull(listOfRepoFiles);
        assertFalse(listOfRepoFiles.isEmpty());

        // Remove the file from cache table
        dao.removeRepoDomainFileList(FixturesTool.SHARED_ISO_STORAGE_DOMAIN_FOR_SP2_AND_SP3, ImageFileType.ISO);
        listOfRepoFiles = getActiveIsoDomain();

        assertNotNull(listOfRepoFiles);
        assertTrue(listOfRepoFiles.isEmpty());
    }

    /**
     * Test foreign key when remove storage domain Iso.
     */
    @Test
    public void testRemoveByRemoveIsoDomain() {
        // Should get one iso file
        List<RepoImage> listOfRepoFiles = dao
                .getRepoListForStorageDomain(FixturesTool.SHARED_ISO_STORAGE_DOMAIN_FOR_SP2_AND_SP3,
                        ImageFileType.ISO);

        assertNotNull(listOfRepoFiles);
        assertFalse(listOfRepoFiles.isEmpty());

        // Test remove Iso
        storageDomainDao.remove(FixturesTool.SHARED_ISO_STORAGE_DOMAIN_FOR_SP2_AND_SP3);
        listOfRepoFiles = getActiveIsoDomain();

        assertNotNull(listOfRepoFiles);
        assertTrue(listOfRepoFiles.isEmpty());
    }

    /**
     * Test when insert row and fetching it later.
     */
    @Test
    public void testInsertRepoFileAndFetchItAgain() {
        RepoImage newRepoFileMap = getNewIsoRepoFile();
        dao.addRepoFileMap(newRepoFileMap);

        List<RepoImage> listOfRepoFiles =
                dao.getRepoListForStorageDomain(FixturesTool.STORAGE_DOMAIN_NFS_ISO, ImageFileType.ISO);

        assertNotNull(listOfRepoFiles);
        assertFalse(listOfRepoFiles.isEmpty());
        assertEquals(listOfRepoFiles.get(0).getRepoImageId(), newRepoFileMap.getRepoImageId());
        assertEquals(listOfRepoFiles.get(0).getLastRefreshed(), newRepoFileMap.getLastRefreshed());
        assertEquals(listOfRepoFiles.get(0).getSize(), newRepoFileMap.getSize());
        assertEquals(listOfRepoFiles.get(0).getRepoDomainId(), newRepoFileMap.getRepoDomainId());
    }

    /**
     * Test update of Iso file. The test demonstrate the refresh procedure. It first deletes the Iso file from the
     * repo_file_meta_data table, and then insert the new files fetched again from VDSM.
     */
    @Test
    public void testUpdateRepoFileByRemoveAndInsert() {
        RepoImage newRepoFileMap = getNewIsoRepoFile();
        dao.addRepoFileMap(newRepoFileMap);

        // Fetch the file from cache table
        List<RepoImage> listOfRepoFiles = getActiveIsoDomain();

        // Get first file and update its String
        assertNotNull(listOfRepoFiles);
        assertFalse(listOfRepoFiles.isEmpty());
        RepoImage repoFile = listOfRepoFiles.get(0);
        assertNotNull(repoFile);
        String oldRepoImageId = repoFile.getRepoImageId();
        newRepoFileMap.setRepoImageId("updatedFileName"
                + newRepoFileMap.getRepoImageId());

        // Remove the file from cache table
        dao.removeRepoDomainFileList(FixturesTool.STORAGE_DOMAIN_NFS_ISO, ImageFileType.ISO);

        // Add the new updated file into the cache table.
        dao.addRepoFileMap(newRepoFileMap);

        // Fetch the updated File.
        listOfRepoFiles = getActiveIsoDomain();

        assertNotNull(listOfRepoFiles);
        assertFalse(listOfRepoFiles.isEmpty());
        RepoImage newRepoFile = listOfRepoFiles.get(0);
        assertNotNull(repoFile);

        // Check if not same file name as in the old file.
        assertNotSame(oldRepoImageId, newRepoFile.getRepoImageId());
    }

    /**
     * Test that the list returns is not null.
     */
    @Test
    public void testFetchExistingRepoFileListById() {
        List<RepoImage> listOfRepoFiles = getActiveIsoDomain();
        assertNotNull(listOfRepoFiles);
    }

    /**
     * Test primary key validity.
     */
    @Test
    public void testPrimaryKeyValidation() {
        RepoImage newRepoFileMap = getNewIsoRepoFile();
        dao.addRepoFileMap(newRepoFileMap);

        // Should enter here since its a violation of primary key
        assertThrows(DuplicateKeyException.class, () -> dao.addRepoFileMap(newRepoFileMap));
    }

    /**
     * Test that the list returns is not null, but is empty.
     */
    @Test
    public void testFetchNotExistingRepoFileListById() {
        Guid falseGuid = new Guid("11111111-1111-1111-1111-111111111111");
        List<RepoImage> listOfRepoFiles = dao.getRepoListForStorageDomain(falseGuid, ImageFileType.ISO);

        assertNotNull(listOfRepoFiles);
        assertTrue(listOfRepoFiles.isEmpty());
    }

    private static RepoImage getNewIsoRepoFile() {
        RepoImage newRepoFileMap = new RepoImage();
        newRepoFileMap.setFileType(ImageFileType.ISO);
        newRepoFileMap.setRepoImageId("isoDomain.iso");
        newRepoFileMap.setLastRefreshed(System.currentTimeMillis());
        newRepoFileMap.setSize(null);
        newRepoFileMap.setDateCreated(null);
        newRepoFileMap.setRepoDomainId(FixturesTool.STORAGE_DOMAIN_NFS_ISO);
        return newRepoFileMap;
    }

    private List<RepoImage> getActiveIsoDomain() {
        return dao.getRepoListForStorageDomain
                (FixturesTool.SHARED_ISO_STORAGE_DOMAIN_FOR_SP2_AND_SP3, ImageFileType.ISO);
    }

}

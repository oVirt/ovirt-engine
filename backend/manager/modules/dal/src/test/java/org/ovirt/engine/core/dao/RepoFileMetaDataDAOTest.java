package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.ImageType;
import org.ovirt.engine.core.common.businessentities.RepoFileMetaData;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.compat.Guid;

public class RepoFileMetaDataDAOTest extends BaseDAOTestCase {

    private RepoFileMetaDataDAO repoFileMetaDataDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        repoFileMetaDataDao = dbFacade.getRepoFileMetaDataDao();
    }

    /**
     * Ensures that saving a domain works as expected.
     */
    @Test
    public void testSave() {
        // Fetch the file from cache table
        List<RepoFileMetaData> listOfRepoFiles = repoFileMetaDataDao
                .getRepoListForStorageDomain(FixturesTool.STORAGE_DOAMIN_NFS_ISO,
                        ImageType.ISO);
        assertNotNull(listOfRepoFiles);
        assertSame(listOfRepoFiles.isEmpty(), true);

        RepoFileMetaData newRepoFileMap = getNewIsoRepoFile();
        repoFileMetaDataDao.addRepoFileMap(newRepoFileMap);

        listOfRepoFiles = repoFileMetaDataDao
                .getRepoListForStorageDomain(FixturesTool.STORAGE_DOAMIN_NFS_ISO,
                        ImageType.ISO);
        assertSame(listOfRepoFiles.isEmpty(), false);
    }

    /**
     * Test remove of repo file from storage domain.
     */
    @Test
    public void testRemove() {
        // Should get one iso file
        List<RepoFileMetaData> listOfRepoFiles = repoFileMetaDataDao
                .getRepoListForStorageDomain(FixturesTool.SHARED_ISO_STORAGE_DOAMIN_FOR_SP2_AND_SP3,
                        ImageType.ISO);

        assertNotNull(listOfRepoFiles);
        assertNotSame(true, listOfRepoFiles.isEmpty());

        // Remove the file from cache table
        repoFileMetaDataDao.removeRepoDomainFileList(FixturesTool.SHARED_ISO_STORAGE_DOAMIN_FOR_SP2_AND_SP3,
                ImageType.ISO);
        listOfRepoFiles = getActiveIsoDomain();

        assertNotNull(listOfRepoFiles);
        assertSame(true, listOfRepoFiles.isEmpty());
    }

    /**
     * Test foreign key when remove storage domain Iso.
     */
    @Test
    public void testRemoveByRemoveIsoDomain() {
        // Should get one iso file
        List<RepoFileMetaData> listOfRepoFiles = repoFileMetaDataDao
                .getRepoListForStorageDomain(FixturesTool.SHARED_ISO_STORAGE_DOAMIN_FOR_SP2_AND_SP3,
                        ImageType.ISO);

        assertNotNull(listOfRepoFiles);
        assertNotSame(true, listOfRepoFiles.isEmpty());

        // Test remove Iso
        StorageDomainDAO storageDomainDao = dbFacade.getStorageDomainDao();
        storageDomainDao.remove(FixturesTool.SHARED_ISO_STORAGE_DOAMIN_FOR_SP2_AND_SP3);
        listOfRepoFiles = getActiveIsoDomain();

        assertNotNull(listOfRepoFiles);
        assertSame(true, listOfRepoFiles.isEmpty());
    }

    /**
     * Test fetch of all storage domains for all the repository files,
     * The fetch should fetch 4 rows, the first one is an empty storage domain,
     * The empty storage domain, should not have files, but should be fetched, since we want to refresh it.
     * The other three are from the same storage domain with three different types.
     */
    @Test
    public void testFetchAllIsoDomainInSystemNoDuplicate() {
        // Should get one iso file
        List<RepoFileMetaData> listOfAllIsoFiles = repoFileMetaDataDao
                .getAllRepoFilesForAllStoragePools(StorageDomainType.ISO,
                        StoragePoolStatus.Up,
                        StorageDomainStatus.Active,
                        VDSStatus.Up);

        // Should get only 4 files, 3 file types from one shared storage domain.
        // plus one empty file of the storage pool with no Iso at all.
        assertSame(listOfAllIsoFiles.size(), 4);
    }

    /**
     * Test fetch of all storage domains for all the repository files,
     * The fetch should fetch 4 rows, the first one is an empty storage domain,
     * The empty storage domain, should not have files, but should be fetched, since we want to refresh it.
     * The other three are from the same storage domain with three different types.
     * In this test, we test the file types, to check if all were fetched.
     */
    @Test
    public void testFileTypeWhenFetchAllIsoDomainInSystem() {
        // Should get one iso file
        List<RepoFileMetaData> listOfAllIsoFiles = repoFileMetaDataDao
                .getAllRepoFilesForAllStoragePools(StorageDomainType.ISO,
                        StoragePoolStatus.Up,
                        StorageDomainStatus.Active,
                        VDSStatus.Up);

        List<ImageType> SharedStorageDomainFileType = new ArrayList<ImageType>();
        List<ImageType> EmptyStorageDomainFileType = new ArrayList<ImageType>();
        for (RepoFileMetaData fileMD : listOfAllIsoFiles) {
            Guid repoDomainId = fileMD.getRepoDomainId();
            if (repoDomainId.equals(FixturesTool.SHARED_ISO_STORAGE_DOAMIN_FOR_SP2_AND_SP3)) {
                // Should have three types of files.
                SharedStorageDomainFileType.add(fileMD.getFileType());
            } else if (repoDomainId.equals(FixturesTool.STORAGE_DOAMIN_NFS_ISO)) {
                // Should have only one type (UNKNOWN)
                EmptyStorageDomainFileType.add(fileMD.getFileType());
            }
        }

        // Start the check
        // the shared storage domain, should have three types of files.
        assertEquals(SharedStorageDomainFileType.size(), 3);
        assertEquals(SharedStorageDomainFileType.contains(ImageType.Unknown), true);
        assertEquals(SharedStorageDomainFileType.contains(ImageType.ISO), true);
        assertEquals(SharedStorageDomainFileType.contains(ImageType.Floppy), true);

        // The empty storage domain, should not have files, but should be fetched, since we want to refresh it.
        assertEquals(EmptyStorageDomainFileType.size(), 1);
        assertEquals(EmptyStorageDomainFileType.contains(ImageType.Unknown), true);
    }

    /**
     * Test fetch of all storage pools and check if fetched the oldest file,
     * when fetching all the repository files.
     */
    @Test
    public void testFetchAllIsoDomainOldestFile() {
        List<RepoFileMetaData> listOfIsoFiles = repoFileMetaDataDao
                .getAllRepoFilesForAllStoragePools(StorageDomainType.ISO,
                        StoragePoolStatus.Up,
                        StorageDomainStatus.Active,
                        VDSStatus.Up);

        List<RepoFileMetaData> listOfFloppyFiles =
                repoFileMetaDataDao
                        .getRepoListForStorageDomain(FixturesTool.SHARED_ISO_STORAGE_DOAMIN_FOR_SP2_AND_SP3,
                                ImageType.Floppy);

        long minLastRefreshed = new Long("9999999999999").longValue();
        for (RepoFileMetaData fileMD : listOfFloppyFiles) {
            long fileLastRefreshed = fileMD.getLastRefreshed();
            if (fileLastRefreshed < minLastRefreshed) {
                minLastRefreshed = fileLastRefreshed;
            }
        }

        // Check if fetched the oldest file when fetching all repository files.
        boolean isValid = true;
        for (RepoFileMetaData fileMetaData : listOfIsoFiles) {
            if (fileMetaData.getFileType() == ImageType.Floppy) {
                if (fileMetaData.getLastRefreshed() > minLastRefreshed) {
                    isValid = false;
                }
            }
        }
        assertEquals(isValid, true);
    }

    /**
     * Test when insert row and fetching it later.
     */
    @Test
    public void testInsertRepoFileAndFetchItAgain() {
        RepoFileMetaData newRepoFileMap = getNewIsoRepoFile();
        repoFileMetaDataDao.addRepoFileMap(newRepoFileMap);

        List<RepoFileMetaData> listOfRepoFiles = repoFileMetaDataDao
                .getRepoListForStorageDomain(FixturesTool.STORAGE_DOAMIN_NFS_ISO,
                        ImageType.ISO);

        assertNotNull(listOfRepoFiles);
        assertSame(true, !listOfRepoFiles.isEmpty());
        assertSame(
                true,
                listOfRepoFiles.get(0).getRepoFileName()
                        .equals(newRepoFileMap.getRepoFileName()));
        assertSame(true,
                listOfRepoFiles.get(0).getLastRefreshed() == newRepoFileMap
                        .getLastRefreshed());
        assertSame(true,
                listOfRepoFiles.get(0).getSize() == newRepoFileMap.getSize());
        assertSame(
                true,
                listOfRepoFiles.get(0).getRepoDomainId()
                        .equals(newRepoFileMap.getRepoDomainId()));
    }

    /**
     * Test update of Iso file. The test demonstrate the refresh procedure. It first deletes the Iso file from the
     * repo_file_meta_data table, and then insert the new files fetched again from VDSM.
     */
    @Test
    public void testUpdateRepoFileByRemoveAndInsert() {
        RepoFileMetaData newRepoFileMap = getNewIsoRepoFile();
        repoFileMetaDataDao.addRepoFileMap(newRepoFileMap);

        // Fetch the file from cache table
        List<RepoFileMetaData> listOfRepoFiles = getActiveIsoDomain();

        // Get first file and update its String
        assertNotNull(listOfRepoFiles);
        assertNotSame(true, listOfRepoFiles.isEmpty());
        RepoFileMetaData repoFile = listOfRepoFiles.get(0);
        assertNotNull(repoFile);
        String oldRepoFileName = repoFile.getRepoFileName();
        newRepoFileMap.setRepoFileName("updatedFileName"
                + newRepoFileMap.getRepoFileName());

        // Remove the file from cache table
        repoFileMetaDataDao.removeRepoDomainFileList(FixturesTool.STORAGE_DOAMIN_NFS_ISO, ImageType.ISO);

        // Add the new updated file into the cache table.
        repoFileMetaDataDao.addRepoFileMap(newRepoFileMap);

        // Fetch the updated File.
        listOfRepoFiles = getActiveIsoDomain();

        assertNotNull(listOfRepoFiles);
        assertNotSame(true, listOfRepoFiles.isEmpty());
        RepoFileMetaData newRepoFile = listOfRepoFiles.get(0);
        assertNotNull(repoFile);

        // Check if not same file name as in the old file.
        assertNotSame(oldRepoFileName, newRepoFile.getRepoFileName());
    }

    /**
     * Test that the list returns is not null.
     */
    @Test
    public void testFetchExistingRepoFileListById() {
        List<RepoFileMetaData> listOfRepoFiles = getActiveIsoDomain();
        assertNotNull(listOfRepoFiles);
    }

    /**
     * Test primary key validity.
     */
    @Test
    public void testPrimaryKeyValidation() {
        RepoFileMetaData newRepoFileMap = getNewIsoRepoFile();
        getNewIsoRepoFile();
        repoFileMetaDataDao.addRepoFileMap(newRepoFileMap);
        try {
            repoFileMetaDataDao.addRepoFileMap(newRepoFileMap);
        } catch (Exception e) {
            // Should enter here since its a violation of primary key
            assertTrue(true);
        }
    }

    /**
     * Test that the list returns is not null, but is empty.
     */
    @Test
    public void testFetchNotExistingRepoFileListById() {
        Guid falseGuid = new Guid("11111111-1111-1111-1111-111111111111");
        List<RepoFileMetaData> listOfRepoFiles = repoFileMetaDataDao
                .getRepoListForStorageDomain(falseGuid,
                        ImageType.ISO);

        assertNotNull(listOfRepoFiles);
        assertSame(true, listOfRepoFiles.isEmpty());
    }

    private static RepoFileMetaData getNewIsoRepoFile() {
        RepoFileMetaData newRepoFileMap = new RepoFileMetaData();
        newRepoFileMap.setFileType(ImageType.ISO);
        newRepoFileMap.setRepoFileName("isoDomain.iso");
        newRepoFileMap.setLastRefreshed(System.currentTimeMillis());
        newRepoFileMap.setSize(0);
        newRepoFileMap.setDateCreated(null);
        newRepoFileMap.setRepoDomainId(FixturesTool.STORAGE_DOAMIN_NFS_ISO);
        return newRepoFileMap;
    }

    private List<RepoFileMetaData> getActiveIsoDomain() {
        return repoFileMetaDataDao
                .getRepoListForStorageDomain(FixturesTool.SHARED_ISO_STORAGE_DOAMIN_FOR_SP2_AND_SP3,
                        ImageType.ISO);
    }

}

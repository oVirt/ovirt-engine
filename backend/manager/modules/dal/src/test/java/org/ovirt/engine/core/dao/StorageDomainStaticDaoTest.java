package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainStaticDaoTest extends BaseDaoTestCase {
    private static final Guid EXISTING_POOL_ID = new Guid("6d849ebf-755f-4552-ad09-9a090cda105d");

    private StorageDomainStaticDao dao;
    private StorageDomainDynamicDao dynamicDao;
    private DiskImageDao diskImageDao;
    private ImageDao imageDao;
    private StorageDomainStatic existingDomain;
    private StorageDomainStatic newStaticDomain;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getStorageDomainStaticDao();
        dynamicDao = dbFacade.getStorageDomainDynamicDao();
        diskImageDao = dbFacade.getDiskImageDao();
        imageDao = dbFacade.getImageDao();
        existingDomain = dao.get(new Guid("72e3a666-89e1-4005-a7ca-f7548004a9ab"));

        newStaticDomain = new StorageDomainStatic();
        newStaticDomain.setStorageName("NewStorageDomain");
        newStaticDomain.setStorage("fDMzhE-wx3s-zo3q-Qcxd-T0li-yoYU-QvVePl");
        newStaticDomain.setStorageFormat(StorageFormatType.V1);
        newStaticDomain.setWipeAfterDelete(true);
        newStaticDomain.setWarningLowSpaceIndicator(3);
        newStaticDomain.setCriticalSpaceActionBlocker(9);
    }

    /**
     * Ensures that null is returned when the id is invalid.
     */
    @Test
    public void testGetWithInvalidId() {
        StorageDomainStatic result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that retrieving the static domain works as expected.
     */
    @Test
    public void testGet() {
        StorageDomainStatic result = dao.get(existingDomain.getId());

        assertNotNull(result);
        assertEquals(existingDomain.getId(), result.getId());
    }

    /**
     * Ensures that get all is not implemented.
     */
    @Test
    public void testGetAll() {
        List<StorageDomainStatic> result = dao.getAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that null is returned when the name is invalid.
     */
    @Test
    public void testGetByNameWithInvalidName() {
        StorageDomainStatic result = dao.getByName("farkle");

        assertNull(result);
    }

    /**
     * Ensures the right instance is returned.
     */
    @Test
    public void testGetByName() {
        StorageDomainStatic result = dao.getByName(existingDomain
                .getStorageName());

        assertNotNull(result);
        assertEquals(existingDomain.getId(), result.getId());
    }

    /**
     * Ensures an empty collection is returned.
     */
    @Test
    public void testGetAllForStoragePoolWithInvalidPool() {
        List<StorageDomainStatic> result = dao
                .getAllForStoragePool(Guid.newGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures the right collection of domains are returned.
     */
    @Test
    public void testGetAllForStoragePool() {
        List<StorageDomainStatic> result = dao.getAllForStoragePool(EXISTING_POOL_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetAllIdsForNonExistingStoragePoolId() throws Exception {
        List<Guid> result = dao.getAllIds(Guid.newGuid(), StorageDomainStatus.Active);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllIdsForNonExistingStatus() throws Exception {
        List<Guid> result = dao.getAllIds(EXISTING_POOL_ID, StorageDomainStatus.Unknown);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllIds() throws Exception {
        List<Guid> result = dao.getAllIds(EXISTING_POOL_ID, StorageDomainStatus.Active);

        assertNotNull(result);
        assertFalse(result.isEmpty());

        for (Guid id : result) {
            assertTrue(!Guid.Empty.equals(id));
        }
    }

    /**
     * Ensures that saving a domain works as expected.
     */
    @Test
    public void testSave() {
        dao.save(newStaticDomain);

        StorageDomainStatic result = dao.get(newStaticDomain.getId());

        assertNotNull(result);
    }

    /**
     * Ensures that updating the static and dynamic portions works as expected.
     */
    @Test
    public void testUpdate() {
        existingDomain.setStorageName("UpdatedName");
        existingDomain.setWipeAfterDelete(true);
        existingDomain.setWarningLowSpaceIndicator(4);
        existingDomain.setCriticalSpaceActionBlocker(8);
        dao.update(existingDomain);

        StorageDomainStatic after = dao.get(existingDomain.getId());

        assertEquals(after, existingDomain);
    }

    /**
     * Ensures that removing a storage domain works as expected.
     */
    @Test
    public void testRemove() {
        dynamicDao.remove(existingDomain.getId());
        List<DiskImage> imagesToRemove = diskImageDao.getAllSnapshotsForStorageDomain(existingDomain.getId());
        Set<Guid> itGuids = new HashSet<>();
        for (DiskImage image : imagesToRemove) {
            itGuids.add(image.getImageTemplateId());
        }
        // First remove images that are not image templates
        for (DiskImage image : imagesToRemove) {
            if (!itGuids.contains(image.getImageId())) {
                imageDao.remove(image.getImageId());
            }
        }
        // Remove images of templates - the blank image guid (empty guid was also inserted) so it is first removed from
        // the set
        // as it has no representation as image on the storage domain
        itGuids.remove(Guid.Empty);
        for (Guid guid : itGuids) {
            imageDao.remove(guid);
        }
        dao.remove(existingDomain.getId());

        StorageDomainStatic domainResult = dao.get(existingDomain.getId());

        assertNull(domainResult);
    }

}

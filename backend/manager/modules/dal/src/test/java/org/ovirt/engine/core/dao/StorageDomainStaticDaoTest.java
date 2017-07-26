package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainStaticDaoTest extends BaseDaoTestCase {
    private StorageDomainStaticDao dao;
    private StorageDomainStatic existingDomain;
    private StorageDomainStatic newStaticDomain;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getStorageDomainStaticDao();
        existingDomain = dao.get(new Guid("bee623f3-9174-4ffd-aa30-4fb0dc0aa2f6"));

        newStaticDomain = new StorageDomainStatic();
        newStaticDomain.setStorageName("NewStorageDomain");
        newStaticDomain.setStorage("fDMzhE-wx3s-zo3q-Qcxd-T0li-yoYU-QvVePl");
        newStaticDomain.setStorageFormat(StorageFormatType.V1);
        newStaticDomain.setWipeAfterDelete(true);
        newStaticDomain.setDiscardAfterDelete(false);
        newStaticDomain.setWarningLowSpaceIndicator(3);
        newStaticDomain.setCriticalSpaceActionBlocker(9);
        newStaticDomain.setFirstMetadataDevice(FixturesTool.LUN_ID2);
        newStaticDomain.setVgMetadataDevice(FixturesTool.LUN_ID2);
        newStaticDomain.setBackup(false);
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
        List<StorageDomainStatic> result = dao.getAllForStoragePool(FixturesTool.DATA_CENTER);

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
        List<Guid> result = dao.getAllIds(FixturesTool.DATA_CENTER, StorageDomainStatus.Unknown);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllIds() throws Exception {
        List<Guid> result = dao.getAllIds(FixturesTool.DATA_CENTER, StorageDomainStatus.Active);

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
        existingDomain.setFirstMetadataDevice(FixturesTool.LUN_ID1);
        existingDomain.setVgMetadataDevice(FixturesTool.LUN_ID1);
        existingDomain.setBackup(true);
        dao.update(existingDomain);

        StorageDomainStatic after = dao.get(existingDomain.getId());

        assertEquals(after, existingDomain);
    }

    /**
     * Ensures that removing a storage domain works as expected.
     */
    @Test
    public void testRemove() {
        dao.remove(existingDomain.getId());

        StorageDomainStatic domainResult = dao.get(existingDomain.getId());

        assertNull(domainResult);
    }

}

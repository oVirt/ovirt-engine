package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainStaticDaoTest
        extends BaseGenericDaoTestCase<Guid, StorageDomainStatic, StorageDomainStaticDao> {

    @Override
    protected StorageDomainStatic generateNewEntity() {
        StorageDomainStatic newStaticDomain = new StorageDomainStatic();
        newStaticDomain.setStorageName("NewStorageDomain");
        newStaticDomain.setStorage("fDMzhE-wx3s-zo3q-Qcxd-T0li-yoYU-QvVePl");
        newStaticDomain.setStorageFormat(StorageFormatType.V1);
        newStaticDomain.setWipeAfterDelete(true);
        newStaticDomain.setDiscardAfterDelete(false);
        newStaticDomain.setWarningLowSpaceIndicator(3);
        newStaticDomain.setCriticalSpaceActionBlocker(9);
        newStaticDomain.setWarningLowConfirmedSpaceIndicator(6);
        newStaticDomain.setFirstMetadataDevice(FixturesTool.LUN_ID2);
        newStaticDomain.setVgMetadataDevice(FixturesTool.LUN_ID2);
        newStaticDomain.setBackup(false);
        return newStaticDomain;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setStorageName("UpdatedName");
        existingEntity.setWipeAfterDelete(true);
        existingEntity.setWarningLowSpaceIndicator(4);
        existingEntity.setCriticalSpaceActionBlocker(8);
        existingEntity.setWarningLowConfirmedSpaceIndicator(5);
        existingEntity.setFirstMetadataDevice(FixturesTool.LUN_ID1);
        existingEntity.setVgMetadataDevice(FixturesTool.LUN_ID1);
        existingEntity.setBackup(true);
    }

    @Override
    protected Guid getExistingEntityId() {
        return new Guid("bee623f3-9174-4ffd-aa30-4fb0dc0aa2f6");
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return 12;
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
        StorageDomainStatic result = dao.getByName(existingEntity.getStorageName());

        assertEquals(existingEntity, result);
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
    public void testGetAllIdsForNonExistingStoragePoolId() {
        List<Guid> result = dao.getAllIds(Guid.newGuid(), StorageDomainStatus.Active);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllIdsForNonExistingStatus() {
        List<Guid> result = dao.getAllIds(FixturesTool.DATA_CENTER, StorageDomainStatus.Unknown);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllIds() {
        List<Guid> result = dao.getAllIds(FixturesTool.DATA_CENTER, StorageDomainStatus.Active);

        assertNotNull(result);
        assertFalse(result.isEmpty());

        for (Guid id : result) {
            assertTrue(!Guid.Empty.equals(id));
        }
    }
}

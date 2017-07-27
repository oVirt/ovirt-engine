package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMapId;

public class StorageServerConnectionLunMapDaoTest extends BaseGenericDaoTestCase
        <LUNStorageServerConnectionMapId, LUNStorageServerConnectionMap, StorageServerConnectionLunMapDao> {

    @Override
    protected LUNStorageServerConnectionMap generateNewEntity() {
        return new LUNStorageServerConnectionMap(FixturesTool.LUN_ID_FOR_DISK, FixturesTool.STORAGE_CONNECTION_ID);
    }

    @Override
    protected void updateExistingEntity() {
        // Unused
    }

    @Override
    protected LUNStorageServerConnectionMapId getExistingEntityId() {
        return new LUNStorageServerConnectionMapId(FixturesTool.LUN_ID1, FixturesTool.STORAGE_CONNECTION_ID);
    }

    @Override
    protected StorageServerConnectionLunMapDao prepareDao() {
        return dbFacade.getStorageServerConnectionLunMapDao();
    }

    @Override
    protected LUNStorageServerConnectionMapId generateNonExistingId() {
        return new LUNStorageServerConnectionMapId("not", "exists");
    }

    @Override
    protected int getEntitiesTotalCount() {
        return 6;
    }

    @Ignore
    @Override
    public void testUpdate() {
        // Not supported
    }

    @Ignore
    @Override
    public void testRemove() {
        // Note supported
    }

    @Test
    public void testGetAllByLunId() {
        List<LUNStorageServerConnectionMap> result = dao.getAll(existingEntity.getId().lunId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (LUNStorageServerConnectionMap mapping : result) {
            assertEquals(existingEntity.getId().lunId, mapping.getId().lunId);
        }
    }
}

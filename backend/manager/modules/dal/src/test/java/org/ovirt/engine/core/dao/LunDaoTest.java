package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;

public class LunDaoTest extends BaseGenericDaoTestCase<String, LUNs, LunDao> {
    @Override
    protected String getExistingEntityId() {
        return FixturesTool.LUN_ID1;
    }

    @Override
    protected LUNs generateNewEntity() {
        LUNs newLUN = new LUNs();
        newLUN.setLUNId("oicu812");
        newLUN.setVolumeGroupId("");
        return newLUN;
    }

    @Override
    protected String generateNonExistingId() {
        return "farkle";
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setSerial("killer");
    }

    @Override
    protected int getEntitiesTotalCount() {
        return 11;
    }

    /**
     * Ensures an empty collection is returned.
     */
    @Test
    public void testGetAllForStorageServerConnectionWithNoLuns() {
        List<LUNs> result = dao.getAllForStorageServerConnection("farkle");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that LUNs are returned for the connection.
     */
    @Test
    public void testGetAllForStorageServerConnection() {
        List<LUNs> result = dao.getAllForStorageServerConnection(FixturesTool.STORAGE_CONNECTION_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForVolumeGroupWithNoLuns() {
        List<LUNs> result = dao.getAllForVolumeGroup("farkle");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures the right set of LUNs are returned.
     */
    @Test
    public void testGetAllForVolumeGroup() {
        List<LUNs> result = dao.getAllForVolumeGroup(existingEntity.getVolumeGroupId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (LUNs lun : result) {
            assertEquals(existingEntity.getVolumeGroupId(), lun.getVolumeGroupId());
        }
    }

    @Test
    public void testRemoveAll() {
        dao.removeAll(Arrays.asList(FixturesTool.ORPHAN_LUN_ID1, FixturesTool.ORPHAN_LUN_ID2));
        assertEquals(getEntitiesTotalCount() - 2, dao.getAll().size());
    }
}

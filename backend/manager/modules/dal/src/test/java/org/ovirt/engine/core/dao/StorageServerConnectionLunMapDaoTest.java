package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMapId;

public class StorageServerConnectionLunMapDaoTest extends BaseDaoTestCase {
    private static final String FREE_LUN_ID = "1IET_00180002";
    private static final int NUM_LUN_MAPS = 3;

    private StorageServerConnectionLunMapDao dao;
    private LUNStorageServerConnectionMap existingLUNStorageMap;
    private LUNStorageServerConnectionMap newLUNStorageMap;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getStorageServerConnectionLunMapDao();
        StorageServerConnectionDao storageServerConnectionDao = dbFacade.getStorageServerConnectionDao();

        StorageServerConnections existingConnection = storageServerConnectionDao.get("0cc146e8-e5ed-482c-8814-270bc48c297e");

        existingLUNStorageMap =
                dao.get(new LUNStorageServerConnectionMapId("1IET_00180001", existingConnection.getId()));
        newLUNStorageMap = new LUNStorageServerConnectionMap(FREE_LUN_ID, existingConnection.getId());
    }

    @Test
    public void testGet() {
        LUNStorageServerConnectionMap result = dao.get(existingLUNStorageMap.getId());

        assertNotNull(result);
        assertEquals(existingLUNStorageMap, result);
    }

    @Test
    public void testSave() {
        dao.save(newLUNStorageMap);

        LUNStorageServerConnectionMap result = dao.get(newLUNStorageMap.getId());

        assertNotNull(result);
        assertEquals(newLUNStorageMap, result);
    }

    @Test
    public void testGetAllByLunId() {
        List<LUNStorageServerConnectionMap> result =
                dao.getAll(existingLUNStorageMap.getId().lunId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (LUNStorageServerConnectionMap mapping : result) {
            assertEquals(existingLUNStorageMap.getId().lunId, mapping.getId().lunId);
        }
    }

    @Test
    public void testGetAll() {
        List<LUNStorageServerConnectionMap> result = dao.getAll();
        assertEquals(NUM_LUN_MAPS, result.size());
    }
}

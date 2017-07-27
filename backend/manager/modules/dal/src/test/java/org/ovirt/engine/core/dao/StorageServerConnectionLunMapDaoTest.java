package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMapId;

public class StorageServerConnectionLunMapDaoTest extends BaseDaoTestCase {
    private static final int NUM_LUN_MAPS = 6;

    private StorageServerConnectionLunMapDao dao;
    private LUNStorageServerConnectionMap existingLUNStorageMap;
    private LUNStorageServerConnectionMap newLUNStorageMap;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getStorageServerConnectionLunMapDao();

        existingLUNStorageMap =
                dao.get(new LUNStorageServerConnectionMapId(FixturesTool.LUN_ID1, FixturesTool.STORAGE_CONNECTION_ID));
        newLUNStorageMap = new LUNStorageServerConnectionMap(FixturesTool.LUN_ID_FOR_DISK, FixturesTool.STORAGE_CONNECTION_ID);
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

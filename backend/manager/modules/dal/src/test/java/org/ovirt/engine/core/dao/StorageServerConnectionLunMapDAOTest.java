package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUN_storage_server_connection_map;
import org.ovirt.engine.core.common.businessentities.storage.LUN_storage_server_connection_map_id;

public class StorageServerConnectionLunMapDAOTest extends BaseDAOTestCase {
    private static final String FREE_LUN_ID = "1IET_00180002";
    private static final String EXISTING_DOMAIN_STORAGE_NAME = "fDMzhE-wx3s-zo3q-Qcxd-T0li-yoYU-QvVePk";

    private StorageServerConnectionLunMapDAO dao;
    private StorageServerConnectionDAO storageServerConnectionDao;
    private StorageServerConnections newServerConnection;
    private StorageServerConnections existingConnection;
    private LUN_storage_server_connection_map existingLUNStorageMap;
    private LUN_storage_server_connection_map newLUNStorageMap;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getStorageServerConnectionLunMapDao();
        storageServerConnectionDao = dbFacade.getStorageServerConnectionDao();

        existingConnection = storageServerConnectionDao.get("0cc146e8-e5ed-482c-8814-270bc48c297e");

        newServerConnection = new StorageServerConnections();
        newServerConnection.setid("0cc146e8-e5ed-482c-8814-270bc48c2980");
        newServerConnection.setconnection(EXISTING_DOMAIN_STORAGE_NAME);

        existingLUNStorageMap =
                dao.get(new LUN_storage_server_connection_map_id("1IET_00180001", existingConnection.getid()));
        newLUNStorageMap = new LUN_storage_server_connection_map(FREE_LUN_ID, existingConnection.getid());
    }

    @Test
    public void testGet() {
        LUN_storage_server_connection_map result = dao.get(existingLUNStorageMap.getId());

        assertNotNull(result);
        assertEquals(existingLUNStorageMap, result);
    }

    @Test
    public void testSave() {
        dao.save(newLUNStorageMap);

        LUN_storage_server_connection_map result = dao.get(newLUNStorageMap.getId());

        assertNotNull(result);
        assertEquals(newLUNStorageMap, result);
    }

    @Test
    public void testGetAll() {
        List<LUN_storage_server_connection_map> result =
                dao.getAll(existingLUNStorageMap.getId().lunId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (LUN_storage_server_connection_map mapping : result) {
            assertEquals(existingLUNStorageMap.getId().lunId, mapping.getId().lunId);
        }
    }
}

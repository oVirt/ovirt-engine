package org.ovirt.engine.core.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.compat.Guid;

public class StoragePoolIsoMapDAOTest extends BaseDAOTestCase {
    private static final Guid EXISTING_ISO_ID = new Guid("72e3a666-89e1-4005-a7ca-f7548004a9ab");
    private static final Guid FREE_ISO_ID = new Guid("72e3a666-89e1-4005-a7ca-f7548004a9ac");
    private StoragePoolDAO storagePoolIsoMapDAO;
    private StoragePoolIsoMapDAO dao;
    private storage_pool existingPool;
    private StoragePoolIsoMap existingStoragePoolIsoMap;
    private StoragePoolIsoMap newStoragePoolIsoMap;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getStoragePoolIsoMapDao();
        storagePoolIsoMapDAO = dbFacade.getStoragePoolDao();

        existingPool = storagePoolIsoMapDAO
                .get(new Guid("6d849ebf-755f-4552-ad09-9a090cda105d"));
        existingStoragePoolIsoMap = dao.get(new StoragePoolIsoMapId(EXISTING_ISO_ID, existingPool.getId()));
        newStoragePoolIsoMap =
                new StoragePoolIsoMap(FREE_ISO_ID, existingPool.getId(), StorageDomainStatus.Unattached);
    }

    @Test
    public void testGetStoragePoolIsoMap() {
        StoragePoolIsoMap result =
                dao.get(new StoragePoolIsoMapId(existingStoragePoolIsoMap.getstorage_id(),
                        new Guid(existingStoragePoolIsoMap.getstorage_pool_id().toString())));

        assertNotNull(result);
        assertEquals(existingStoragePoolIsoMap, result);
    }

    @Test
    public void testAddStoragePoolIsoMap() {
        dao.save(newStoragePoolIsoMap);

        StoragePoolIsoMap result =
                dao.get(new StoragePoolIsoMapId(newStoragePoolIsoMap.getstorage_id(),
                        new Guid(newStoragePoolIsoMap.getstorage_pool_id().toString())));

        assertNotNull(result);
        assertEquals(newStoragePoolIsoMap, result);
    }

    @Test
    public void testUpdateStoragePoolIsoMap() {
        existingStoragePoolIsoMap.setstatus(StorageDomainStatus.Active);

        dao.update(existingStoragePoolIsoMap);

        StoragePoolIsoMap result =
                dao.get(new StoragePoolIsoMapId(existingStoragePoolIsoMap.getstorage_id(),
                        new Guid(existingStoragePoolIsoMap.getstorage_pool_id().toString())));

        assertNotNull(result);
        assertEquals(existingStoragePoolIsoMap, result);
    }

    @Test
    public void testRemoveStoragePoolIsoMap() {
        dao.remove(new StoragePoolIsoMapId(new Guid(existingStoragePoolIsoMap.getstorage_id().toString()),
                existingStoragePoolIsoMap.getstorage_pool_id()));

        StoragePoolIsoMap result =
                dao.get(new StoragePoolIsoMapId(existingStoragePoolIsoMap.getstorage_id(),
                        new Guid(existingStoragePoolIsoMap.getstorage_pool_id().toString())));

        assertNull(result);
    }

    @Test
    public void testGetAllStoragePoolIsoMapsForStoragePool() {
        List<StoragePoolIsoMap> result = dao.getAllForStoragePool(existingPool.getId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (StoragePoolIsoMap mapping : result) {
            assertEquals(existingPool.getId(), mapping.getstorage_pool_id());
        }
    }

    @Test
    public void testGetAllStoragePoolIsoMapsForIso() {
        List<StoragePoolIsoMap> result = dao.getAllForStorage(EXISTING_ISO_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (StoragePoolIsoMap mapping : result) {
            assertEquals(EXISTING_ISO_ID, mapping.getstorage_id());
        }
    }
}

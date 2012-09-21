package org.ovirt.engine.core.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.storage_pool_iso_map;
import org.ovirt.engine.core.compat.Guid;

public class StoragePoolIsoMapDAOTest extends BaseDAOTestCase {
    private static final Guid EXISTING_ISO_ID = new Guid("72e3a666-89e1-4005-a7ca-f7548004a9ab");
    private static final Guid FREE_ISO_ID = new Guid("72e3a666-89e1-4005-a7ca-f7548004a9ac");
    private StoragePoolDAO storagePoolIsoMapDAO;
    private StoragePoolIsoMapDAO dao;
    private storage_pool existingPool;
    private storage_pool_iso_map existingStoragePoolIsoMap;
    private storage_pool_iso_map newStoragePoolIsoMap;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getStoragePoolIsoMapDao());
        storagePoolIsoMapDAO = prepareDAO(dbFacade.getStoragePoolDao());

        existingPool = storagePoolIsoMapDAO
                .get(new Guid("6d849ebf-755f-4552-ad09-9a090cda105d"));
        existingStoragePoolIsoMap = dao.get(new StoragePoolIsoMapId(EXISTING_ISO_ID, existingPool.getId()));
        newStoragePoolIsoMap =
                new storage_pool_iso_map(FREE_ISO_ID, existingPool.getId(), StorageDomainStatus.Unattached);
    }

    @Test
    public void testGetStoragePoolIsoMap() {
        storage_pool_iso_map result =
                dao.get(new StoragePoolIsoMapId(existingStoragePoolIsoMap.getstorage_id(),
                        new Guid(existingStoragePoolIsoMap.getstorage_pool_id().toString())));

        assertNotNull(result);
        assertEquals(existingStoragePoolIsoMap, result);
    }

    @Test
    public void testAddStoragePoolIsoMap() {
        dao.save(newStoragePoolIsoMap);

        storage_pool_iso_map result =
                dao.get(new StoragePoolIsoMapId(newStoragePoolIsoMap.getstorage_id(),
                        new Guid(newStoragePoolIsoMap.getstorage_pool_id().toString())));

        assertNotNull(result);
        assertEquals(newStoragePoolIsoMap, result);
    }

    @Test
    public void testUpdateStoragePoolIsoMap() {
        existingStoragePoolIsoMap.setstatus(StorageDomainStatus.Active);

        dao.update(existingStoragePoolIsoMap);

        storage_pool_iso_map result =
                dao.get(new StoragePoolIsoMapId(existingStoragePoolIsoMap.getstorage_id(),
                        new Guid(existingStoragePoolIsoMap.getstorage_pool_id().toString())));

        assertNotNull(result);
        assertEquals(existingStoragePoolIsoMap, result);
    }

    @Test
    public void testRemoveStoragePoolIsoMap() {
        dao.remove(new StoragePoolIsoMapId(new Guid(existingStoragePoolIsoMap.getstorage_id().toString()),
                existingStoragePoolIsoMap.getstorage_pool_id()));

        storage_pool_iso_map result =
                dao.get(new StoragePoolIsoMapId(existingStoragePoolIsoMap.getstorage_id(),
                        new Guid(existingStoragePoolIsoMap.getstorage_pool_id().toString())));

        assertNull(result);
    }

    @Test
    public void testGetAllStoragePoolIsoMapsForStoragePool() {
        List<storage_pool_iso_map> result = dao.getAllForStoragePool(existingPool.getId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (storage_pool_iso_map mapping : result) {
            assertEquals(existingPool.getId(), mapping.getstorage_pool_id());
        }
    }

    @Test
    public void testGetAllStoragePoolIsoMapsForIso() {
        List<storage_pool_iso_map> result = dao.getAllForStorage(EXISTING_ISO_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (storage_pool_iso_map mapping : result) {
            assertEquals(EXISTING_ISO_ID, mapping.getstorage_id());
        }
    }
}

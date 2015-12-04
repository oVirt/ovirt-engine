package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.compat.Guid;

public class StoragePoolIsoMapDaoTest extends BaseDaoTestCase {
    private static final Guid EXISTING_ISO_ID = new Guid("72e3a666-89e1-4005-a7ca-f7548004a9ab");
    private static final Guid FREE_ISO_ID = new Guid("72e3a666-89e1-4005-a7ca-f7548004a9ac");
    private StoragePoolDao storagePoolIsoMapDao;
    private StoragePoolIsoMapDao dao;
    private StoragePool existingPool;
    private StoragePoolIsoMap existingStoragePoolIsoMap;
    private StoragePoolIsoMap newStoragePoolIsoMap;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getStoragePoolIsoMapDao();
        storagePoolIsoMapDao = dbFacade.getStoragePoolDao();

        existingPool = storagePoolIsoMapDao
                .get(new Guid("6d849ebf-755f-4552-ad09-9a090cda105d"));
        existingStoragePoolIsoMap = dao.get(new StoragePoolIsoMapId(EXISTING_ISO_ID, existingPool.getId()));
        newStoragePoolIsoMap =
                new StoragePoolIsoMap(FREE_ISO_ID, existingPool.getId(), StorageDomainStatus.Unattached);
    }

    @Test
    public void testGetStoragePoolIsoMap() {
        StoragePoolIsoMap result =
                dao.get(new StoragePoolIsoMapId(existingStoragePoolIsoMap.getStorageId(),
                        existingStoragePoolIsoMap.getStoragePoolId()));

        assertNotNull(result);
        assertEquals(existingStoragePoolIsoMap, result);
    }

    @Test
    public void testAddStoragePoolIsoMap() {
        dao.save(newStoragePoolIsoMap);

        StoragePoolIsoMap result =
                dao.get(new StoragePoolIsoMapId(newStoragePoolIsoMap.getStorageId(),
                        newStoragePoolIsoMap.getStoragePoolId()));

        assertNotNull(result);
        assertEquals(newStoragePoolIsoMap, result);
    }

    @Test
    public void testUpdateStoragePoolIsoMap() {
        existingStoragePoolIsoMap.setStatus(StorageDomainStatus.Active);

        dao.update(existingStoragePoolIsoMap);

        StoragePoolIsoMap result =
                dao.get(new StoragePoolIsoMapId(existingStoragePoolIsoMap.getStorageId(),
                        existingStoragePoolIsoMap.getStoragePoolId()));

        assertNotNull(result);
        assertEquals(existingStoragePoolIsoMap, result);
    }

    @Test
    public void testRemoveStoragePoolIsoMap() {
        dao.remove(new StoragePoolIsoMapId(existingStoragePoolIsoMap.getStorageId(),
                existingStoragePoolIsoMap.getStoragePoolId()));

        StoragePoolIsoMap result =
                dao.get(new StoragePoolIsoMapId(existingStoragePoolIsoMap.getStorageId(),
                        existingStoragePoolIsoMap.getStoragePoolId()));

        assertNull(result);
    }

    @Test
    public void testGetAllStoragePoolIsoMapsForStoragePool() {
        List<StoragePoolIsoMap> result = dao.getAllForStoragePool(existingPool.getId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (StoragePoolIsoMap mapping : result) {
            assertEquals(existingPool.getId(), mapping.getStoragePoolId());
        }
    }

    @Test
    public void testGetAllStoragePoolIsoMapsForIso() {
        List<StoragePoolIsoMap> result = dao.getAllForStorage(EXISTING_ISO_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (StoragePoolIsoMap mapping : result) {
            assertEquals(EXISTING_ISO_ID, mapping.getStorageId());
        }
    }
}

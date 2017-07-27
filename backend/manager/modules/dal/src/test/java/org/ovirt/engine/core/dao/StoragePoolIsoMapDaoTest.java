package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;

public class StoragePoolIsoMapDaoTest extends BaseDaoTestCase {
    private StoragePoolIsoMapDao dao;
    private StoragePoolIsoMap existingStoragePoolIsoMap;
    private StoragePoolIsoMap newStoragePoolIsoMap;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getStoragePoolIsoMapDao();

        existingStoragePoolIsoMap = dao.get(new StoragePoolIsoMapId(FixturesTool.STORAGE_DOMAIN_SCALE_SD5, FixturesTool.DATA_CENTER));
        newStoragePoolIsoMap =
                new StoragePoolIsoMap(FixturesTool.STORAGE_DOMAIN_SCALE_SD6, FixturesTool.DATA_CENTER, StorageDomainStatus.Unattached);
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
        List<StoragePoolIsoMap> result = dao.getAllForStoragePool(FixturesTool.DATA_CENTER);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (StoragePoolIsoMap mapping : result) {
            assertEquals(FixturesTool.DATA_CENTER, mapping.getStoragePoolId());
        }
    }

    @Test
    public void testGetAllStoragePoolIsoMapsForIso() {
        List<StoragePoolIsoMap> result = dao.getAllForStorage(FixturesTool.STORAGE_DOMAIN_SCALE_SD5);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (StoragePoolIsoMap mapping : result) {
            assertEquals(FixturesTool.STORAGE_DOMAIN_SCALE_SD5, mapping.getStorageId());
        }
    }
}

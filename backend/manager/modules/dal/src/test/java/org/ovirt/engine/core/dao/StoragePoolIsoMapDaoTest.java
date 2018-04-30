package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.compat.Guid;

public class StoragePoolIsoMapDaoTest extends
        BaseGenericDaoTestCase<StoragePoolIsoMapId, StoragePoolIsoMap, StoragePoolIsoMapDao> {

    @Override
    protected StoragePoolIsoMap generateNewEntity() {
        return new StoragePoolIsoMap
                (FixturesTool.STORAGE_DOMAIN_SCALE_SD6, FixturesTool.DATA_CENTER, StorageDomainStatus.Unattached);
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setStatus(StorageDomainStatus.Active);
    }

    @Override
    protected StoragePoolIsoMapId getExistingEntityId() {
        return new StoragePoolIsoMapId(FixturesTool.STORAGE_DOMAIN_SCALE_SD5, FixturesTool.DATA_CENTER);
    }

    @Override
    protected StoragePoolIsoMapId generateNonExistingId() {
        return new StoragePoolIsoMapId(Guid.newGuid(), Guid.newGuid());
    }

    @Override
    protected int getEntitiesTotalCount() {
        return 11;
    }

    @Disabled
    @Override
    public void testGetAll() {
        // Not supported
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

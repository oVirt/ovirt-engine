package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.VdsSpmIdMap;
import org.ovirt.engine.core.compat.Guid;

public class VdsSpmIdMapDaoTest extends BaseGenericDaoTestCase<Guid, VdsSpmIdMap, VdsSpmIdMapDao> {
    private static final Guid FREE_STORAGE_POOL_ID = new Guid("6d849ebf-755f-4552-ad09-9a090cda105e");

    @Override
    protected VdsSpmIdMap generateNewEntity() {
        return new VdsSpmIdMap(FREE_STORAGE_POOL_ID, FixturesTool.VDS_RHEL6_NFS_SPM, 1);
    }

    @Override
    protected void updateExistingEntity() {
        // Not supported
    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.HOST_ID;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return 1;
    }

    @Disabled
    @Override
    public void testUpdate() {
        // Not supported
    }

    @Test
    public void testDeleteByPoolVdsSpmIdMap() {
        dao.removeByVdsAndStoragePool(existingEntity.getId(), existingEntity.getStoragePoolId());
        VdsSpmIdMap result = dao.get(existingEntity.getId());

        assertNull(result);
    }

    @Test
    public void testGetAll() {
        List<VdsSpmIdMap> result = dao.getAll(FixturesTool.DATA_CENTER);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VdsSpmIdMap mapping : result) {
            assertEquals(FixturesTool.DATA_CENTER, mapping.getStoragePoolId());
        }
    }

    @Test
    public void testGetVdsSpmIdMapForStoragePoolAndVdsId() {
        VdsSpmIdMap result = dao.get(existingEntity.getStoragePoolId(), existingEntity.getVdsSpmId());

        assertNotNull(result);
        assertEquals(existingEntity, result);
    }
}

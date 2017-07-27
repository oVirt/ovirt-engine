package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VdsSpmIdMap;
import org.ovirt.engine.core.compat.Guid;

public class VdsSpmIdMapDaoTest extends BaseDaoTestCase {
    private static final Guid FREE_STORAGE_POOL_ID = new Guid("6d849ebf-755f-4552-ad09-9a090cda105e");
    private VdsSpmIdMapDao dao;
    private VdsSpmIdMap existingVdsSpmIdMap;
    private VdsSpmIdMap newVdsSpmIdMap;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getVdsSpmIdMapDao();
        existingVdsSpmIdMap = dao.get(FixturesTool.HOST_ID);
        newVdsSpmIdMap = new VdsSpmIdMap(FREE_STORAGE_POOL_ID, FixturesTool.VDS_RHEL6_NFS_SPM, 1);
    }

    @Test
    public void testGet() {
        VdsSpmIdMap result = dao.get(existingVdsSpmIdMap.getId());

        assertNotNull(result);
        assertEquals(existingVdsSpmIdMap, result);
    }

    @Test
    public void testSave() {
        dao.save(newVdsSpmIdMap);

        VdsSpmIdMap result = dao.get(newVdsSpmIdMap.getId());

        assertNotNull(result);
        assertEquals(newVdsSpmIdMap, result);
    }

    @Test
    public void testRemove() {
        dao.remove(existingVdsSpmIdMap.getId());

        VdsSpmIdMap result = dao.get(existingVdsSpmIdMap.getId());

        assertNull(result);
    }

    @Test
    public void testDeleteByPoolVdsSpmIdMap() {
        dao.removeByVdsAndStoragePool(existingVdsSpmIdMap.getId(), existingVdsSpmIdMap.getStoragePoolId());
        VdsSpmIdMap result = dao.get(existingVdsSpmIdMap.getId());

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
        VdsSpmIdMap result =
                dao.get(existingVdsSpmIdMap.getStoragePoolId(),
                        existingVdsSpmIdMap.getVdsSpmId());

        assertNotNull(result);
        assertEquals(existingVdsSpmIdMap, result);
    }
}

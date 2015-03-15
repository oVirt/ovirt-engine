package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.storage.DiskLunMap;
import org.ovirt.engine.core.common.businessentities.storage.DiskLunMapId;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.RandomUtils;

/**
 * Unit tests to validate {@link DiskLunMapDao}.
 */
public class DiskLunMapDaoTest extends BaseGenericDaoTestCase<DiskLunMapId, DiskLunMap, DiskLunMapDao> {

    private static final DiskLunMapId EXISTING_DISK_LUN_MAP_ID =
            new DiskLunMapId(FixturesTool.LUN_DISK_ID, FixturesTool.LUN_ID_FOR_DISK);
    protected static final int TOTAL_DISK_LUN_MAPS = 3;

    @Override
    protected DiskLunMapId generateNonExistingId() {
        return new DiskLunMapId(Guid.newGuid(), RandomUtils.instance().nextString(10));
    }

    @Override
    protected int getEneitiesTotalCount() {
        return TOTAL_DISK_LUN_MAPS;
    }

    @Override
    protected DiskLunMap generateNewEntity() {
        return new DiskLunMap(new Guid("1b26a52b-b60f-44cb-9f46-3ef333b04a34"), "1IET_00180001");
    }

    @Override
    protected void updateExistingEntity() {
        // Nothing to do here, move along..
    }

    @Override
    protected DiskLunMapDao prepareDao() {
        return dbFacade.getDiskLunMapDao();
    }

    @Override
    protected DiskLunMapId getExistingEntityId() {
        return EXISTING_DISK_LUN_MAP_ID;
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testUpdate() {
        super.testUpdate();
    }

    @Test
    public void testGetDiskIdByLunId() {
        assertEquals(dao.getDiskIdByLunId(EXISTING_DISK_LUN_MAP_ID.getLunId()),
                new DiskLunMap(EXISTING_DISK_LUN_MAP_ID.getDiskId(), EXISTING_DISK_LUN_MAP_ID.getLunId()));
    }
}

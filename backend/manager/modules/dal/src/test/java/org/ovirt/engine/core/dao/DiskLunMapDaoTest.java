package org.ovirt.engine.core.dao;

import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
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
    protected static final int TOTAL_DISK_LUN_MAPS = 4;

    @Override
    protected DiskLunMapId generateNonExistingId() {
        return new DiskLunMapId(Guid.newGuid(), RandomUtils.instance().nextString(10));
    }

    @Override
    protected int getEntitiesTotalCount() {
        return TOTAL_DISK_LUN_MAPS;
    }

    @Override
    protected DiskLunMap generateNewEntity() {
        return new DiskLunMap(new Guid("1b26a52b-b60f-44cb-9f46-3ef333b04a34"), FixturesTool.LUN_ID1);
    }

    @Override
    protected void updateExistingEntity() {
        // Nothing to do here, move along..
    }

    @Override
    protected DiskLunMapId getExistingEntityId() {
        return EXISTING_DISK_LUN_MAP_ID;
    }

    @Override
    @Test
    public void testUpdate() {
        assertThrows(UnsupportedOperationException.class, super::testUpdate);
    }

    @Test
    public void testGetDiskIdByLunId() {
        assertEquals(dao.getDiskIdByLunId(EXISTING_DISK_LUN_MAP_ID.getLunId()),
                new DiskLunMap(EXISTING_DISK_LUN_MAP_ID.getDiskId(), EXISTING_DISK_LUN_MAP_ID.getLunId()));
    }

    @Test
    public void testGetDiskLunMapByDiskId() {
        assertEquals(dao.getDiskLunMapByDiskId(EXISTING_DISK_LUN_MAP_ID.getDiskId()),
                new DiskLunMap(EXISTING_DISK_LUN_MAP_ID.getDiskId(), EXISTING_DISK_LUN_MAP_ID.getLunId()));
    }

    @Test
    public void getDiskLunMapForVmsInPool() {
        List<DiskLunMap> diskLunMapsForVmsInPool = dao.getDiskLunMapsForVmsInPool(FixturesTool.DATA_CENTER);
        assertTrue(isEqualCollection(
                Arrays.asList(FixturesTool.LUN_ID_FOR_DISK, FixturesTool.LUN_ID_FOR_DISK2),
                diskLunMapsForVmsInPool.stream().map(DiskLunMap::getLunId).collect(Collectors.toList())));
    }
}

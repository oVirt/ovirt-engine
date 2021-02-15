package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class DiskImageDynamicDaoTest extends BaseGenericDaoTestCase<Guid, DiskImageDynamic, DiskImageDynamicDao> {
    private static final int TOTAL_DYNAMIC_DISK_IMAGES = 5;

    @Override
    protected DiskImageDynamic generateNewEntity() {
        return createDiskImageDynamic(Guid.newGuid());
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setActualSize(existingEntity.getActualSize() * 10);
        existingEntity.setReadLatency(0.000000001d);
        existingEntity.setWriteLatency(0.000000002d);
        existingEntity.setFlushLatency(0.999999999d);
    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.IMAGE_ID;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return TOTAL_DYNAMIC_DISK_IMAGES;
    }

    public DiskImageDynamic createDiskImageDynamic(Guid id) {
        DiskImageDynamic dynamic = new DiskImageDynamic();
        dynamic.setReadRate(5);
        dynamic.setReadOps(6L);
        dynamic.setWriteRate(7);
        dynamic.setWriteOps(8L);
        dynamic.setReadLatency(0d);
        dynamic.setFlushLatency(0.0202020d);
        dynamic.setWriteLatency(null);
        dynamic.setId(id);
        return dynamic;
    }

    @Test
    public void testUpdateAll() {
        DiskImageDynamic existingEntity2 = dao.get(new Guid("42058975-3d5e-484a-80c1-01c31207f579"));
        existingEntity.setActualSize(100);
        existingEntity2.setReadRate(120);
        existingEntity.setReadLatency(100d);
        existingEntity2.setReadLatency(0.00001d);

        dao.updateAll(Arrays.asList(existingEntity, existingEntity2));

        assertEquals(existingEntity, dao.get(existingEntity.getId()));
        assertEquals(existingEntity2, dao.get(existingEntity2.getId()));
    }

    @Test
    public void updateAllDiskImageDynamicWithDiskIdByVmId() {
        Guid imageId = FixturesTool.IMAGE_ID_2;
        Guid imageGroupId = FixturesTool.IMAGE_GROUP_ID_2;

        DiskImageDynamic existingEntity2 = dao.get(imageId);
        assertNotEquals(120, (int) existingEntity2.getReadRate());

        existingEntity2.setId(imageGroupId);
        Integer readRate = 120;
        existingEntity2.setReadRate(readRate);

        // test that the record is updated when the active disk is attached to the vm
        dao.updateAllDiskImageDynamicWithDiskIdByVmId(Collections.singleton(new Pair<>(FixturesTool.VM_RHEL5_POOL_57,
                existingEntity2)));

        existingEntity2.setId(imageId);
        assertEquals(existingEntity2, dao.get(imageId));

        existingEntity2.setReadRate(150);
        dao.updateAllDiskImageDynamicWithDiskIdByVmId(Collections.singleton(new Pair<>(FixturesTool.VM_RHEL5_POOL_57,
                existingEntity2)));
        assertEquals(readRate, dao.get(imageId).getReadRate());
    }

    @Test
    public void sortDiskImageDynamicForUpdate() {
        Guid firstGuid = Guid.Empty;
        Guid secondGuid = Guid.createGuidFromString("11111111-1111-1111-1111-111111111111");
        Guid thirdGuid = Guid.createGuidFromString("22222222-2222-2222-2222-222222222222");
        List<Pair<Guid, DiskImageDynamic>> diskImageDynamicForVm = new LinkedList<>();
        diskImageDynamicForVm.add(new Pair<>(Guid.Empty, createDiskImageDynamic(thirdGuid)));
        diskImageDynamicForVm.add(new Pair<>(Guid.Empty, createDiskImageDynamic(secondGuid)));
        diskImageDynamicForVm.add(new Pair<>(Guid.Empty, createDiskImageDynamic(firstGuid)));
        List<Pair<Guid, DiskImageDynamic>> sortedList =
                DiskImageDynamicDaoImpl.sortDiskImageDynamicForUpdate(diskImageDynamicForVm);
        Collections.reverse(diskImageDynamicForVm);
        assertEquals(diskImageDynamicForVm, sortedList);
    }
}

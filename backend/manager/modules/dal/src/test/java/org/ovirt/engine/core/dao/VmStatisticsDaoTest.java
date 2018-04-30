package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.compat.Guid;

public class VmStatisticsDaoTest extends BaseGenericDaoTestCase<Guid, VmStatistics, VmStatisticsDao > {
    @Override
    protected VmStatistics generateNewEntity() {
        VmStatistics newVmStatistics = new VmStatistics();
        newVmStatistics.setId(FixturesTool.VM_RHEL5_POOL_50_ID);
        newVmStatistics.setCpuSys(22.0D);
        newVmStatistics.setCpuUser(35.0D);
        newVmStatistics.setUsageCpuPercent(44);
        newVmStatistics.setUsageMemPercent(67);
        newVmStatistics.setDisksUsage("disk_usage");
        newVmStatistics.setGuestMemoryBuffered(32L);
        newVmStatistics.setGuestMemoryCached(34L);
        return newVmStatistics;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setUsageMemPercent(17);
        existingEntity.setDisksUsage("java.util.map { [ ] }");
    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.VM_RHEL5_POOL_57;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        // Not used
        return 0;
    }

    @Test
    public void testGetAll() {
        assertThrows(UnsupportedOperationException.class, () -> dao.getAll());
    }

    @Test
    public void testUpdateAll() {
        VmStatistics existingVm = dao.get(FixturesTool.VM_RHEL5_POOL_57);
        VmStatistics existingVm2 = dao.get(FixturesTool.VM_RHEL5_POOL_51);
        existingVm.setCpuSys(50.0);
        existingVm2.setCpuUser(50.0);

        dao.updateAll(Arrays.asList(existingVm, existingVm2));

        assertEquals(existingVm, dao.get(existingVm.getId()));
        assertEquals(existingVm2, dao.get(existingVm2.getId()));
    }
}

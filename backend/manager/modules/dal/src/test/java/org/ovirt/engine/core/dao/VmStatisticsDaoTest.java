package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.dao.DataIntegrityViolationException;

public class VmStatisticsDaoTest extends BaseDaoTestCase {
    private static final Guid NEW_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f5001");

    private VmStatisticsDao dao;
    private VmStatistics newVmStatistics;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getVmStatisticsDao();
        newVmStatistics = new VmStatistics();
    }

    @Test
    public void testGet() {
        VmStatistics result = dao.get(FixturesTool.VM_RHEL5_POOL_57);

        assertNotNull(result);
        assertEquals(FixturesTool.VM_RHEL5_POOL_57, result.getId());
    }

    @Test
    public void testGetNonExistingId() {
        VmStatistics result = dao.get(Guid.newGuid());
        assertNull(result);
    }

    @Test
    public void testSave() {
        newVmStatistics.setId(NEW_VM_ID);
        newVmStatistics.setCpuSys(22D);
        newVmStatistics.setCpuUser(35D);
        newVmStatistics.setUsageCpuPercent(44);
        newVmStatistics.setUsageMemPercent(67);
        newVmStatistics.setDisksUsage("disk_usage");
        newVmStatistics.setGuestMemoryBuffered(32L);
        newVmStatistics.setGuestMemoryCached(34L);
        dao.save(newVmStatistics);

        VmStatistics stats = dao.get(NEW_VM_ID);

        assertNotNull(stats);
        assertEquals(newVmStatistics, stats);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testSaveStaticDoesNotExist() {
        Guid newGuid = Guid.newGuid();
        newVmStatistics.setId(newGuid);
        dao.save(newVmStatistics);

        VmStatistics stats = dao.get(newGuid);
        assertNull(stats);
    }

    @Test
    public void testUpdateStatistics() {
        VmStatistics before = dao.get(FixturesTool.VM_RHEL5_POOL_57);

        before.setUsageMemPercent(17);
        before.setDisksUsage("java.util.map { [ ] }");
        dao.update(before);

        VmStatistics after = dao.get(FixturesTool.VM_RHEL5_POOL_57);
        assertEquals(before, after);
    }

    @Test
    public void testRemoveStatistics() {
        VmStatistics before = dao.get(FixturesTool.VM_RHEL5_POOL_57);
        // make sure we're using a real example
        assertNotNull(before);
        dao.remove(FixturesTool.VM_RHEL5_POOL_57);
        VmStatistics after = dao.get(FixturesTool.VM_RHEL5_POOL_57);
        assertNull(after);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetAll() {
        dao.getAll();
    }

    @Test
    public void testUpdateAll() throws Exception {
        VmStatistics existingVm = dao.get(FixturesTool.VM_RHEL5_POOL_57);
        VmStatistics existingVm2 = dao.get(FixturesTool.VM_RHEL5_POOL_51);
        existingVm.setCpuSys(50.0);
        existingVm2.setCpuUser(50.0);

        dao.updateAll(Arrays.asList(existingVm, existingVm2));

        assertEquals(existingVm, dao.get(existingVm.getId()));
        assertEquals(existingVm2, dao.get(existingVm2.getId()));
    }
}

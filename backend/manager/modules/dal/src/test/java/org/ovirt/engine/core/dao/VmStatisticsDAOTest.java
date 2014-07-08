/**
 *
 */
package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.dao.DataIntegrityViolationException;

/**
 *
 */
public class VmStatisticsDAOTest extends BaseDAOTestCase {
    private static final Guid EXISTING_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355");

    private VmStaticDAO vmStaticDao;
    private VmStatisticsDAO dao;
    private VmStatic newVmStatic;
    private VmStatistics newVmStatistics;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        vmStaticDao = dbFacade.getVmStaticDao();
        dao = dbFacade.getVmStatisticsDao();
        newVmStatic = vmStaticDao.get(new Guid("77296e00-0cad-4e5a-9299-008a7b6f5001"));
        newVmStatistics = new VmStatistics();
    }

    @Test
    public void testGet() {
        VmStatistics result = dao.get(EXISTING_VM_ID);

        assertNotNull(result);
        assertEquals(EXISTING_VM_ID, result.getId());
    }

    @Test
    public void testGetNonExistingId() {
        VmStatistics result = dao.get(Guid.newGuid());
        assertNull(result);
    }

    @Test
    public void testSave() {
        newVmStatistics.setId(newVmStatic.getId());
        newVmStatistics.setMigrationProgressPercent(0);
        newVmStatistics.setMemoryUsageHistory(Arrays.asList(2, 3, 4));
        newVmStatistics.setCpuUsageHistory(Arrays.asList(5, 6, 7));
        newVmStatistics.setNetworkUsageHistory(Collections.EMPTY_LIST);
        dao.save(newVmStatistics);

        VmStatistics stats = dao.get(newVmStatic.getId());

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
        VmStatistics before = dao.get(EXISTING_VM_ID);

        before.setusage_mem_percent(17);
        before.setDisksUsage("java.util.map { [ ] }");
        dao.update(before);

        VmStatistics after = dao.get(EXISTING_VM_ID);
        assertEquals(before, after);
    }

    @Test
    public void testRemoveStatistics() {
        VmStatistics before = dao.get(EXISTING_VM_ID);
        // make sure we're using a real example
        assertNotNull(before);
        dao.remove(EXISTING_VM_ID);
        VmStatistics after = dao.get(EXISTING_VM_ID);
        assertNull(after);
    }

    @Test(expected = NotImplementedException.class)
    public void testGetAll() {
        dao.getAll();
    }

    @Test
    public void testUpdateAll() throws Exception {
        VmStatistics existingVm = dao.get(EXISTING_VM_ID);
        VmStatistics existingVm2 = dao.get(new Guid("77296e00-0cad-4e5a-9299-008a7b6f4356"));
        existingVm.setcpu_sys(50.0);
        existingVm2.setcpu_user(50.0);

        dao.updateAll(Arrays.asList(new VmStatistics[] { existingVm, existingVm2 }));

        assertEquals(existingVm, dao.get(existingVm.getId()));
        assertEquals(existingVm2, dao.get(existingVm2.getId()));
    }
}

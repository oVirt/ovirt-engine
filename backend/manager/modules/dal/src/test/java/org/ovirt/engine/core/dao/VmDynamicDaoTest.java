package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.GuestAgentStatus;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.compat.Guid;

public class VmDynamicDaoTest extends BaseDaoTestCase {
    private static final Guid VDS_STATIC_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6");
    private static final Guid VDS_STATIC_ID2 = new Guid("23f6d691-5dfb-472b-86dc-9e1d2d3c18f3");
    private static final int DYNAMIC_RUNNING_COUNT = 3;
    private VmDynamicDao dao;
    private VmDynamic existingVm;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getVmDynamicDao();
        existingVm = dao.get(new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355"));
        existingVm.setStatus(VMStatus.Up);
    }

    /**
     * Gets all dynamic details for VMs running on a specific VDS.
     */
    @Test
    public void testGetAllForRunningForVds() {
        List<VmDynamic> result = dao.getAllRunningForVds(VDS_STATIC_ID);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(DYNAMIC_RUNNING_COUNT, result.size());
        for (VmDynamic vmdynamic : result) {
            assertEquals(VDS_STATIC_ID, vmdynamic.getRunOnVds());
        }
    }

    @Test
    public void testIsAnyVmRunOnVds() {
        assertTrue(dao.isAnyVmRunOnVds(VDS_STATIC_ID));
        assertFalse(dao.isAnyVmRunOnVds(VDS_STATIC_ID2));
    }

    /**
     * Ensures updating the dynamic status aspect of the VM works.
     */
    @Test
    public void testUpdateStatus() {
        VmDynamic before = dao.get(existingVm.getId());
        before.setStatus(VMStatus.Down);
        dao.updateStatus(before.getId(), before.getStatus());
        VmDynamic after = dao.get(existingVm.getId());
        assertEquals(before, after);
    }

    /**
     * Ensures that null is returned when the id is invalid.
     */
    @Test
    public void testGetWithInvalidId() {
        VmDynamic result = dao.get(Guid.newGuid());
        assertNull(result);
    }

    @Test
    public void testGet() {
        VmDynamic result = dao.get(existingVm.getId());
        assertNotNull(result);
        assertEquals(existingVm.getId(), result.getId());
    }

    @Test
    public void testSave() {
        dao.remove(existingVm.getId());
        dao.save(existingVm);
        VmDynamic vmdynamic = dao.get(existingVm.getId());

        assertNotNull(vmdynamic);
        assertEquals(vmdynamic, existingVm);
    }

    /**
     * Ensures deleting the dynamic portion of a VM works.
     */
    @Test
    public void testRemoveDynamic() {
        VmDynamic before = dao.get(existingVm.getId());

        // make sure we're using a real example
        assertNotNull(before);
        dao.remove(existingVm.getId());
        VmDynamic after = dao.get(existingVm.getId());
        assertNull(after);
    }

    /**
     * Ensures updating the dynamic aspect of the VM works.
     */
    @Test
    public void testUpdate() {
        VmDynamic before = dao.get(existingVm.getId());

        before.setVmHost("farkle.redhat.com");
        before.setRunOnce(!before.isRunOnce());
        dao.update(before);

        VmDynamic after = dao.get(existingVm.getId());

        assertEquals(before, after);
    }

    @Test
    public void testUpdateAll() throws Exception {
        VmDynamic existingVm2 = dao.get(new Guid("77296e00-0cad-4e5a-9299-008a7b6f4356"));
        existingVm.setStatus(VMStatus.Down);
        existingVm2.setVmIp("111");
        existingVm2.setVmFQDN("localhost.localdomain");

        dao.updateAll(Arrays.asList(new VmDynamic[] { existingVm, existingVm2 }));

        assertEquals(existingVm, dao.get(existingVm.getId()));
        assertEquals(existingVm2, dao.get(existingVm2.getId()));
    }

    /**
     * Make sure that saving a new console user id and console user name to a virtual machine
     * without a previous console user succeeds and returns <code>true</code>.
     */
    @Test
    public void testUpdateConsoleUserWithOptimisticLockingSuccess() throws Exception {
        VmDynamic vmWithoutConsoleUser = dao.get(new Guid("77296e00-0cad-4e5a-9299-008a7b6f4356"));
        vmWithoutConsoleUser.setConsoleUserId(new Guid("9bf7c640-b620-456f-a550-0348f366544b"));

        boolean result = dao.updateConsoleUserWithOptimisticLocking(vmWithoutConsoleUser);

        assertTrue(result);
    }

    /**
     * Make sure that saving a new console user id and console user name to a virtual machine
     * that already as a previous console user fails and returns <code>false</code>.
     */
    @Test
    public void testUpdateConsoleUserWithOptimisticLockingFailure() throws Exception {
        VmDynamic vmWithoutConsoleUser = dao.get(new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355"));
        vmWithoutConsoleUser.setConsoleUserId(new Guid("9bf7c640-b620-456f-a550-0348f366544b"));

        boolean result = dao.updateConsoleUserWithOptimisticLocking(vmWithoutConsoleUser);

        assertFalse(result);
    }

    @Test
    public void testClearMigratingToVds() throws Exception {
        Guid vmId = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4356");
        VmDynamic vmDynamic = dao.get(vmId);
        assertNotNull("migrating_to_vds field should not be null before we clear it",
                vmDynamic.getMigratingToVds());

        dao.clearMigratingToVds(vmId);

        vmDynamic = dao.get(vmId);
        assertNull("migrating_to_vds field should be null after we clear it",
                vmDynamic.getMigratingToVds());
    }

    @Test
    public void testGuestAgentStatus() throws Exception {
        Guid vmId = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4356");
        VmDynamic vmDynamic = dao.get(vmId);
        vmDynamic.setGuestAgentStatus(GuestAgentStatus.UpdateNeeded);
        dao.update(vmDynamic);
        vmDynamic = dao.get(vmId);
        assertTrue(vmDynamic.getGuestAgentStatus().getValue() == GuestAgentStatus.UpdateNeeded.getValue());
    }

    @Test
    public void testUpdateToUnknown() {
        VmDynamic existingVm2 = dao.get(new Guid("77296e00-0cad-4e5a-9299-008a7b6f4356"));
        VmDynamic existingVm3 = dao.get(new Guid("77296e00-0cad-4e5a-9299-008a7b6f4354"));
        dao.updateVmsToUnknown(Arrays.asList(existingVm.getId(), existingVm2.getId()));
        assertEquals(VMStatus.Unknown, dao.get(existingVm.getId()).getStatus());
        assertEquals(VMStatus.Unknown, dao.get(existingVm2.getId()).getStatus());
        assertNotEquals(VMStatus.Unknown, dao.get(existingVm3.getId()).getStatus());
    }
}

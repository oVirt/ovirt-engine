package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.compat.Guid;

public class VmDynamicDAOTest extends BaseDAOTestCase {
    private static final Guid VDS_STATIC_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6");
    private static final int DYNAMIC_RUNNING_COUNT = 3;
    private VmDynamicDAO dao;
    private VmDynamic existingVm;
    private Guid existingStaticGuidVm =  new Guid("77296e00-0cad-4e5a-9299-008a7b6f4357");
    private VmDynamic newVmDynamic;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getVmDynamicDAO();
        existingVm = dao.get(new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355"));
        existingVm.setstatus(VMStatus.Up);

        newVmDynamic = new VmDynamic();
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
            assertEquals(VDS_STATIC_ID, vmdynamic.getrun_on_vds());
        }
    }

    /**
     * Ensures updating the dynamic status aspect of the VM works.
     */
    @Test
    public void testUpdateStatus() {
        VmDynamic before = dao.get(existingVm.getId());
        before.setstatus(VMStatus.Down);
        dao.updateStatus(before.getId(), before.getstatus());
        VmDynamic after = dao.get(existingVm.getId());
        assertEquals(before, after);
    }

    /**
     * Ensures that null is returned when the id is invalid.
     */
    @Test
    public void testGetWithInvalidId() {
        VmDynamic result = dao.get(Guid.NewGuid());
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
        newVmDynamic.setId(existingStaticGuidVm);
        dao.save(newVmDynamic);
        VmDynamic vmdynamic = dao.get(newVmDynamic.getId());

        assertNotNull(vmdynamic);
        assertEquals(vmdynamic, newVmDynamic);
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

        before.setvm_host("farkle.redhat.com");
        dao.update(before);

        VmDynamic after = dao.get(existingVm.getId());

        assertEquals(before, after);
    }

    @Test
    public void testUpdateAll() throws Exception {
        VmDynamic existingVm2 = dao.get(Guid.createGuidFromString("77296e00-0cad-4e5a-9299-008a7b6f4356"));
        existingVm.setstatus(VMStatus.Down);
        existingVm2.setvm_ip("111");

        dao.updateAll(Arrays.asList(new VmDynamic[] { existingVm, existingVm2 }));

        assertEquals(existingVm, dao.get(existingVm.getId()));
        assertEquals(existingVm2, dao.get(existingVm2.getId()));
    }
}

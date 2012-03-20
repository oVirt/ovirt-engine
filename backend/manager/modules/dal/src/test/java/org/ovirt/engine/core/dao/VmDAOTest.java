package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;

public class VmDAOTest extends BaseDAOTestCase {
    private static final Guid VDS_STATIC_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6");
    private static final Guid VDS_GROUP_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
    private static final Guid USER_ID = new Guid("9bf7c640-b620-456f-a550-0348f366544b");
    private static final Guid STORAGE_DOMAIN_ID = new Guid("72e3a666-89e1-4005-a7ca-f7548004a9ab");

    private static final int VM_COUNT = 3;
    private VmDAO dao;
    private VM existingVm;
    private VmStatic newVmStatic;
    private VM newVm;
    private VmTemplate vmtemplate;
    private VmTemplate existingTemplate;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getVmDAO();
        existingVm = dao.get(new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355"));
        existingVm.setstatus(VMStatus.Up);
        vmtemplate = dbFacade.getVmTemplateDAO().get(
                new Guid("1b85420c-b84c-4f29-997e-0eb674b40b79"));
        existingTemplate = dbFacade.getVmTemplateDAO().get(
                new Guid("1b85420c-b84c-4f29-997e-0eb674b40b79"));

        newVm = new VM();
        newVm.setId(Guid.NewGuid());
        newVm.setvds_group_id(VDS_GROUP_ID);
        newVm.setvmt_guid(vmtemplate.getId());

        newVmStatic = new VmStatic();
        newVmStatic.setvm_name("New Virtual Machine");
        newVmStatic.setvds_group_id(VDS_GROUP_ID);
        newVmStatic.setvmt_guid(vmtemplate.getId());
    }

    /**
     * Ensures that get requires a valid id.
     */
    @Test
    public void testGetWithInvalidId() {
        VM result = dao.get(Guid.NewGuid());

        assertNull(result);
    }

    /**
     * Ensures that get works as expected.
     */
    @Test
    public void testGet() {
        VM result = dao.get(existingVm.getId());

        assertNotNull(result);
        assertEquals(result, existingVm);
    }

    /**
     * Ensures the correct VM is returned.
     */
    @Test
    public void testGetForHibernationImage() {
        VM result = dao.getForHibernationImage(FixturesTool.IMAGE_ID);

        assertNotNull(result);
        assertEquals(FixturesTool.IMAGE_ID.toString(), result.gethibernation_vol_handle());
    }

    /**
     * Gets the VM associated with the specified image.
     */
    @Test
    public void testGetForImage() {
        Map<Boolean, List<VM>> result = dao.getForImage(FixturesTool.IMAGE_ID);

        assertNotNull(result);
        assertEquals("wrong number of VMs with plugged image", 2, result.get(true).size());
        assertEquals("wrong number of VMs with unplugged image", 1, result.get(false).size());
    }

    /**
     * Ensures that null is returned.
     */
    @Test
    public void testGetForImageGroupWithInvalidGroup() {
        VM result = dao.getForImageGroup(Guid.NewGuid());

        assertNull(result);
    }

    @Test
    public void testGetForImageGroup() {
        VM result = dao.getForImageGroup(FixturesTool.IMAGE_GROUP_ID.getValue());

        assertNotNull(result);
    }

    /**
     * Ensures that getting all VMs works as expected.
     */
    @Test
    public void testGetAll() {
        List<VM> result = dao.getAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(VM_COUNT, result.size());
    }

    /**
     * Gets all VMs for the named ad group.
     */
    @Test
    public void testGetAllForAdGroupByName() {
        List<VM> result = dao.getAllForAdGroupByName("philosophers");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that getting all VMs for a storage domain works as expected.
     */
    @Test
    public void testGetAllVmsRelatedToQuotaIdWithNoVmsRelated() {
        List<VM> result = dao.getAllVmsRelatedToQuotaId(FixturesTool.QUOTA_SPECIFIC);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllVmsRelatedToQuotaId() {
        List<VM> result = dao.getAllVmsRelatedToQuotaId(FixturesTool.QUOTA_GENERAL);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that getting all VMs for a storage domain works as expected.
     */
    @Test
    public void testGetAllForStorageDomain() {
        List<VM> result = dao.getAllForStorageDomain(STORAGE_DOMAIN_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that getting all VMs for a specific VDS client.
     */
    @Test
    public void testGetAllForDedicatedPowerClientByVds() {
        List<VM> result = dao.getAllForDedicatedPowerClientByVds(VDS_STATIC_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VM vm : result) {
            assertEquals(VDS_GROUP_ID, vm.getvds_group_id());
        }
    }

    /**
     * Ensures that retrieving all VMs for a specified user works as expected.
     */
    @Test
    public void testGetAllForUser() {
        List<VM> result = dao.getAllForUser(USER_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that it retrieves all such VMs works as expected.
     */
    @Test
    public void testGetAllForUsersWithGroupsAndUserRoles() {
        List<VM> result = dao.getAllForUserWithGroupsAndUserRoles(USER_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that it retrieves all VMs running on the specified VDS.
     */
    @Test
    public void testGetAllRunningForVds() {
        Map<Guid, VM> result = dao.getAllRunningByVds(VDS_STATIC_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that all running VMs associated with a storage domain.
     */
    @Test
    public void testGetAllRunningForStorageDomain() {
        List<VM> result = dao.getAllRunningForStorageDomain(STORAGE_DOMAIN_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures the VMs related to the specified template are returned.
     */
    @Test
    public void testGetAllWithTemplate() {
        List<VM> result = dao
                .getAllWithTemplate(existingTemplate.getId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VM vm : result) {
            assertEquals(existingTemplate.getId(), vm.getvmt_guid());
        }
    }

    /**
     * Ensures removing a vm works as expected.
     */
    @Test
    public void testRemove() {
        VM before = dao.get(existingVm.getId());

        // ensure we're actually doing a real test
        assertNotNull(before);

        dao.remove(existingVm.getId());

        VM after = dao.get(existingVm.getId());

        assertNull(after);
    }

}

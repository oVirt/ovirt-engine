package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
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

    private static final int VM_COUNT = 5;
    private VmDAO dao;
    private VM existingVm;
    private VmStatic newVmStatic;
    private VM newVm;
    private VmTemplate vmtemplate;
    private VmTemplate existingTemplate;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getVmDao();
        existingVm = dao.get(new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355"));
        existingVm.setStatus(VMStatus.Up);
        vmtemplate = dbFacade.getVmTemplateDao().get(
                new Guid("1b85420c-b84c-4f29-997e-0eb674b40b79"));
        existingTemplate = dbFacade.getVmTemplateDao().get(
                new Guid("1b85420c-b84c-4f29-997e-0eb674b40b79"));

        newVm = new VM();
        newVm.setId(Guid.NewGuid());
        newVm.setVdsGroupId(VDS_GROUP_ID);
        newVm.setVmtGuid(vmtemplate.getId());

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

        assertGetResult(result);
    }

    /**
     * Ensures that get works as expected when a filtered for permissions of a privileged user.
     */
    @Test
    public void testGetFilteredWithPermissions() {
        VM result = dao.get(existingVm.getId(), PRIVILEGED_USER_ID, true);

        assertGetResult(result);
    }

    /**
     * Ensures that get works as expected when a filtered for permissions of an unprivileged user.
     */
    @Test
    public void testGetFilteredWithPermissionsNoPermissions() {
        VM result = dao.get(existingVm.getId(), UNPRIVILEGED_USER_ID, true);

        assertNull(result);
    }

    /**
     * Ensures that get works as expected when a filtered for permissions of an unprivileged user, and filtering disabled.
     */
    @Test
    public void testGetFilteredWithPermissionsNoPermissionsAndNoFilter() {
        VM result = dao.get(existingVm.getId(), UNPRIVILEGED_USER_ID, false);

        assertGetResult(result);
    }

    /**
     * Asserts the given VM is the expected one
     */
    private void assertGetResult(VM result) {
        assertNotNull(result);
        assertEquals("Vm db generation wasn't loaded as expected", 1, result.getDbGeneration());
        assertEquals(result, existingVm);
    }

    /**
     * Ensures the correct VM is returned.
     */
    @Test
    public void testGetForHibernationImage() {
        VM result = dao.getForHibernationImage(FixturesTool.IMAGE_ID);

        assertNotNull(result);
        assertEquals(FixturesTool.IMAGE_ID.toString(), result.getHibernationVolHandle());
    }

    /**
     * Gets the VM associated with the specified image.
     */
    @Test
    public void testGetForDisk() {
        Map<Boolean, List<VM>> result = dao.getForDisk(FixturesTool.IMAGE_GROUP_ID);

        assertNotNull(result);
        assertEquals("wrong number of VMs with unplugged image", 1, result.get(false).size());
    }

    /**
     * Gets list of VMs which associated with the specified disk id.
     */
    @Test
    public void testGetVmsListForDisk() {
        List<VM> result = dao.getVmsListForDisk(FixturesTool.IMAGE_GROUP_ID);

        assertNotNull(result);
        assertEquals("wrong number of VMs", 1, result.size());
    }

    /**
     * Ensures that getting all VMs works as expected.
     */
    @Test
    public void testGetAll() {
        List<VM> result = dao.getAll();

        VmDAOTest.assertCorrectGetAllResult(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetVmsByIds() {
        List<VM> result = dao.getVmsByIds(Arrays.asList(FixturesTool.VM_RHEL5_POOL_60, FixturesTool.VM_RHEL5_POOL_59));
        assertEquals("loaded templates list isn't in the expected size", 2, result.size());
        Collection<Guid> recieved = CollectionUtils.collect(result, new Transformer() {
            @Override
            public Object transform(Object input) {
                return ((VM)input).getId();
            }
        });
        assertTrue("the received list didn't contain an expected VM", recieved.contains(FixturesTool.VM_RHEL5_POOL_60));
        assertTrue("the received list didn't contain an expected VM", recieved.contains(FixturesTool.VM_RHEL5_POOL_59));
    }

    /**
     * Ensures that retrieving VMs works as expected for a privileged user with filtering enabled.
     */
    @Test
    public void testGetAllWithPermissionsPrivilegedUser() {
        List<VM> result = dao.getAll(PRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains(existingVm));
    }

    /**
     * Ensures that retrieving VMs works as expected with filtering disabled for an unprivileged user.
     */
    @Test
    public void testGetAllWithPermissionsDisabledUnprivilegedUser() {
        List<VM> result = dao.getAll(UNPRIVILEGED_USER_ID, false);

        VmDAOTest.assertCorrectGetAllResult(result);
    }

    /**
     * Ensures that no VM is retrieved for an unprivileged user with filtering enabled.
     */
    @Test
    public void testGetWithPermissionsUnprivilegedUser() {
        List<VM> result = dao.getAll(UNPRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertTrue(result.isEmpty());
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
        for (VM vm : result) {
            assertEquals("Wrong quota id", FixturesTool.QUOTA_GENERAL, vm.getQuotaId());
            assertEquals("Wrong quota name", "Quota General", vm.getQuotaName());
            assertFalse("Quota shouldn't be default", vm.isQuotaDefault());
            assertEquals("Wrong quota enforcement type",
                    QuotaEnforcementTypeEnum.DISABLED,
                    vm.getQuotaEnforcementType());
        }
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
            assertEquals(VDS_GROUP_ID, vm.getVdsGroupId());
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
    public void testGetAllActiveForStorageDomain() {
        List<VM> result = dao.getAllActiveForStorageDomain(STORAGE_DOMAIN_ID);

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
            assertEquals(existingTemplate.getId(), vm.getVmtGuid());
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

    /**
     * Ensures that only the correct vm is fetched.
     */
    @Test
    public void testGetAllForNetwork() {
        List<VM> result = dao.getAllForNetwork(FixturesTool.NETWORK_ENGINE_2);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(existingVm, result.get(0));
    }

    /**
     * Ensures that no vms are fetched since the network is not assigned to any cluster
     */
    @Test
    public void testGetAllForNetworkEmpty() {
        List<VM> result = dao.getAllForNetwork(FixturesTool.NETWORK_NO_CLUSTERS_ATTACHED);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private static void assertCorrectGetAllResult(List<VM> result) {
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(VM_COUNT, result.size());
        for (VM vm : result) {
            assertEquals("Vm db generation wasn't loaded as expected", 1, vm.getDbGeneration());
        }
    }
}

package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;

public class VmDaoTest extends BaseDaoTestCase {
    private static final int VM_COUNT = 8;
    private VmDao dao;
    private VM existingVm;
    private VmStatic newVmStatic;
    private VM newVm;
    private VmTemplate vmtemplate;
    private VmTemplate existingTemplate;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getVmDao();
        existingVm = dao.get(FixturesTool.VM_RHEL5_POOL_57);
        existingVm.setStatus(VMStatus.Up);
        vmtemplate = dbFacade.getVmTemplateDao().get(FixturesTool.VM_TEMPLATE_RHEL5);
        existingTemplate = dbFacade.getVmTemplateDao().get(FixturesTool.VM_TEMPLATE_RHEL5);

        newVm = new VM();
        newVm.setId(Guid.newGuid());
        newVm.setClusterId(FixturesTool.CLUSTER_RHEL6_ISCSI);
        newVm.setVmtGuid(vmtemplate.getId());

        newVmStatic = new VmStatic();
        newVmStatic.setName("New Virtual Machine");
        newVmStatic.setClusterId(FixturesTool.CLUSTER_RHEL6_ISCSI);
        newVmStatic.setVmtGuid(vmtemplate.getId());
    }

    /**
     * Ensures that get requires a valid id.
     */
    @Test
    public void testGetWithInvalidId() {
        VM result = dao.get(Guid.newGuid());

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
     * Ensures that get by name works as expected when a filtered for permissions
     * of a privileged user.
     */
    @Test
    public void testGetByNameFilteredWithPermissions() {
        VM result = dao.getByNameForDataCenter(null, existingVm.getName(), PRIVILEGED_USER_ID, true);

        assertGetResult(result);
    }

    /**
     * Ensures that get by name works as expected when a filtered for permissions
     * of an unprivileged user.
     */
    @Test
    public void testGetByNameFilteredWithPermissionsNoPermissions() {
        VM result = dao.getByNameForDataCenter(null, existingVm.getName(), UNPRIVILEGED_USER_ID, true);

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
     * Gets the VM associated with the specified image.
     */
    @Test
    public void testGetForDisk() {
        Map<Boolean, List<VM>> result = dao.getForDisk(FixturesTool.IMAGE_GROUP_ID, true);

        assertNotNull(result);
        assertEquals("wrong number of VMs with unplugged image", 1, result.get(Boolean.TRUE).size());
    }

    /**
     * Gets list of VMs which associated with the specified disk id.
     */
    @Test
    public void testGetVmsListForDisk() {
        List<VM> result = dao.getVmsListForDisk(FixturesTool.IMAGE_GROUP_ID, false);

        assertNotNull(result);
        assertEquals("wrong number of VMs", 1, result.size());
    }

    /**
     * Ensures that getting all VMs works as expected.
     */
    @Test
    public void testGetAll() {
        List<VM> result = dao.getAll();

        VmDaoTest.assertCorrectGetAllResult(result);
    }

    /**
     * Ensures that getting all VMs for specific action group works as expected.
     */
    @Test
    public void testGetAllForUserAndActionGroup() {
        List<VM> result = dao.getAllForUserAndActionGroup(PRIVILEGED_USER_ID, ActionGroup.CONNECT_TO_SERIAL_CONSOLE);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(6, result.size());
    }

    /**
     * Ensures that getting all VMs for unprivileged specific action group works as expected.
     */
    @Test
    public void testGetAllForUnPrivilegedUserAndActionGroup() {
        List<VM> result = dao.getAllForUserAndActionGroup(UNPRIVILEGED_USER_ID, ActionGroup.CONNECT_TO_SERIAL_CONSOLE);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetVmsByIds() {
        List<VM> result = dao.getVmsByIds(Arrays.asList(FixturesTool.VM_RHEL5_POOL_60, FixturesTool.VM_RHEL5_POOL_59));
        assertEquals("loaded templates list isn't in the expected size", 2, result.size());
        Collection<Guid> recieved = result.stream().map(VM::getId).collect(Collectors.toList());
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
     * Ensures that the VMs based on an instance type ID are returned properly by GetVmsByInstanceTypeId SP
     */
    @Test
    public void testGetVmsByInstanceTypeId() {
        List<VM> result = dao.getVmsListByInstanceType(new Guid("99408929-82cf-4dc7-a532-9d998063fa95"));
        assertEquals(result.size(), 1);
        assertEquals(result.iterator().next().getId(), new Guid("77296e00-0cad-4e5a-9299-008a7b6f4354"));
    }

    /**
     * Ensures that retrieving VMs works as expected with filtering disabled for an unprivileged user.
     */
    @Test
    public void testGetAllWithPermissionsDisabledUnprivilegedUser() {
        List<VM> result = dao.getAll(UNPRIVILEGED_USER_ID, false);

        VmDaoTest.assertCorrectGetAllResult(result);
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
     * Ensures that getting all VMs for a storage domain works as expected for a domain without VMs.
     */
    @Test
    public void testGetAllForStorageDomainWithVms() {
        List<VM> result = dao.getAllForStorageDomain(FixturesTool.STORAGE_DOAMIN_SCALE_SD5);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(FixturesTool.VM_RHEL5_POOL_57, result.get(0).getId());
    }

    /**
     * Ensures that getting all VMs for a storage domain works as expected for a domain without VMs.
     */
    @Test
    public void testGetAllForStorageDomainWithoutVMs() {
        List<VM> result = dao.getAllForStorageDomain(FixturesTool.STORAGE_DOAMIN_SCALE_SD6);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that retrieving all VMs for a specified user works as expected.
     */
    @Test
    public void testGetAllForUser() {
        List<VM> result = dao.getAllForUser(FixturesTool.USER_EXISTING_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that it retrieves all such VMs works as expected.
     */
    @Test
    public void testGetAllForUsersWithGroupsAndUserRoles() {
        List<VM> result = dao.getAllForUserWithGroupsAndUserRoles(FixturesTool.USER_EXISTING_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that it retrieves all VMs running on the specified VDS.
     */
    @Test
    public void testGetAllRunningForVds() {
        Map<Guid, VM> result = dao.getAllRunningByVds(FixturesTool.VDS_RHEL6_NFS_SPM);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that it retrieves all VMs running on or migrating to the specified VDS.
     */
    @Test
    public void getAllRunningOnOrMigratingToVds() {
        List<VM> result = dao.getAllRunningOnOrMigratingToVds(FixturesTool.VDS_RHEL6_NFS_SPM);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that all running VMs associated with a storage domain.
     */
    @Test
    public void testGetAllActiveForStorageDomain() {
        List<VM> result = dao.getAllActiveForStorageDomain(FixturesTool.STORAGE_DOAMIN_SCALE_SD5);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensure that all VMS are belongs to Cluster return correct
     */
    @Test
    public void testGetAllForCluster() {
        List<VM> result = dao.getAllForCluster(FixturesTool.CLUSTER_RHEL6_ISCSI);

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

    /**
     * Ensures that only the correct vm is fetched.
     */
    @Test
    public void testGetAllForVnicProfile() {
        List<VM> result = dao.getAllForVnicProfile(FixturesTool.VM_NETWORK_INTERFACE_PROFILE);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(existingVm, result.get(0));
    }

    /**
     * Ensures that no vms are fetched
     */
    @Test
    public void testGetAllForVnicProfileEmpty() {
        List<VM> result = dao.getAllForVnicProfile(FixturesTool.VM_NETWORK_INTERFACE_PROFILE_NOT_USED);
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

    @Test
    public void testGetAllForVmPool() {
        List<VM> result = dao.getAllForVmPool(FixturesTool.STORAGE_POOL_FEDORA);

        assertNotNull(result);
        assertEquals("wrong number of VMs attached to pool", 2, result.size());
    }

    @Test
    public void testSaveIsInitialized() {
        Guid vmId = existingVm.getId();
        boolean origInitialized = existingVm.isInitialized();

        // Switch is_initialized
        dao.saveIsInitialized(vmId, !origInitialized);
        VM updatedVm = dao.get(vmId);
        assertEquals("VM's is_initiazlied was not updated", !origInitialized, updatedVm.isInitialized());

        // Switch it back, just to make sure
        dao.saveIsInitialized(vmId, origInitialized);
        updatedVm = dao.get(vmId);
        assertEquals("VM's is_initiazlied was not updated", origInitialized, updatedVm.isInitialized());
    }

    @Test
    public void testFailedAutoStartVms() {
        List<VM> result = dao.getAllFailedAutoStartVms();

        assertNotNull(result);
        assertEquals("wrong number of failed HA VMs", 1, result.size());
    }

    @Test
    public void testUpdateOriginalTemplateName() {
        dao.updateOriginalTemplateName(
                FixturesTool.VM_TEMPLATE_RHEL6_2,
                "renamed"
        );

        List<String> renamedTemplates = Arrays.asList(
                "77296e00-0cad-4e5a-9299-008a7b6f4354",
                "77296e00-0cad-4e5a-9299-008a7b6f4355",
                "77296e00-0cad-4e5a-9299-008a7b6f4356"
        );

        List<String> notRenamedTemplate = Arrays.asList(
                "77296e00-0cad-4e5a-9299-008a7b6f4359"
        );

        // all will be renamed
        assertOriginalTemplateNameIs(renamedTemplates, "renamed");

        // since connected to other "original template" it will stay untouched
        assertOriginalTemplateNameIs(notRenamedTemplate, "otherTemplateName");
    }

    private void assertOriginalTemplateNameIs(List<String> vmIds, String expectedTemplateName) {
        for (String vmId : vmIds) {
            assertEquals(expectedTemplateName, dao.get(new Guid(vmId)).getOriginalTemplateName());
        }
    }

    /**
     * Ensures that it retrieves all running VMs for cluster
     */
    @Test
    public void testAllRunningByCluster() {
        List<VM> result = dao.getAllRunningByCluster(FixturesTool.CLUSTER_RHEL6_ISCSI);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetVmIdsForVersionUpdate() {
        List<Guid> vmIdsToUpdate = dao.getVmIdsForVersionUpdate(FixturesTool.VM_TEMPLATE_RHEL5);

        assertTrue(vmIdsToUpdate.contains(FixturesTool.VM_RHEL5_POOL_52));
    }

    /**
     * Ensures that there are no VMs with disks on different storage domains.
     */
    @Test
    public void testGetAllVMsWithDisksOnOtherStorageDomain() {
        List<VM> result = dao.getAllVMsWithDisksOnOtherStorageDomain(FixturesTool.STORAGE_DOAMIN_SCALE_SD5);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testEmptyGetAllForCpuProfiles() {
        List<VM> result = dao.getAllForCpuProfiles(Collections.singletonList(Guid.newGuid()));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllForCpuProfiles() {
        List<VM> result = dao.getAllForCpuProfiles(Collections.singleton(FixturesTool.CPU_PROFILE_1));

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testEmptyGetAllForDiskProfiles() {
        List<VM> result = dao.getAllForDiskProfiles(Collections.singletonList(Guid.newGuid()));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllForDiskProfiles() {
        List<VM> result = dao.getAllForDiskProfiles(Collections.singleton(FixturesTool.DISK_PROFILE_1));

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    private void createHostedEngineVm(Guid id) {
        VmStatic vmStatic = new VmStatic();
        vmStatic.setId(id);
        vmStatic.setClusterId(FixturesTool.CLUSTER_RHEL6_ISCSI);
        vmStatic.setName("HostedEngine");
        vmStatic.setOrigin(OriginType.HOSTED_ENGINE);
        getDbFacade().getVmStaticDao().save(vmStatic);

        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setId(id);
        getDbFacade().getVmDynamicDao().save(vmDynamic);

        VmStatistics vmStatistics = new VmStatistics();
        vmStatistics.setId(id);
        getDbFacade().getVmStatisticsDao().save(vmStatistics);
    }

    @Test
    public void testGetVmsByOrigins() {
        Guid heVmId = Guid.newGuid();
        List<OriginType> list1 = Arrays.asList(OriginType.HOSTED_ENGINE, OriginType.MANAGED_HOSTED_ENGINE);
        List<OriginType> list2 = Arrays.asList(OriginType.HOSTED_ENGINE);
        List<OriginType> list3 = Arrays.asList(OriginType.MANAGED_HOSTED_ENGINE);

        int count1 = dao.getVmsByOrigins(list1).size();
        int count2 = dao.getVmsByOrigins(list2).size();
        int count3 = dao.getVmsByOrigins(list3).size();

        createHostedEngineVm(heVmId);

        assertEquals(count1 + 1, dao.getVmsByOrigins(list1).size());
        assertEquals(count2 + 1, dao.getVmsByOrigins(list2).size());
        assertEquals(count3, dao.getVmsByOrigins(list3).size());

        dao.remove(heVmId);
    }

}

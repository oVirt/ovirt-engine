package org.ovirt.engine.core.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.compat.Guid;

public class VmDaoTest extends BaseDaoTestCase<VmDao> {
    private static final int VM_COUNT = 10;
    private VM existingVm;

    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private VmStatisticsDao vmStatisticsDao;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        existingVm = dao.get(FixturesTool.VM_RHEL5_POOL_57);
        existingVm.setStatus(VMStatus.Up);

        VM newVm = new VM();
        newVm.setId(Guid.newGuid());
        newVm.setClusterId(FixturesTool.CLUSTER_RHEL6_ISCSI);
        newVm.setBiosType(BiosType.Q35_SEA_BIOS);
        newVm.setVmtGuid(FixturesTool.VM_TEMPLATE_RHEL5);

        VmStatic newVmStatic = new VmStatic();
        newVmStatic.setName("New Virtual Machine");
        newVmStatic.setClusterId(FixturesTool.CLUSTER_RHEL6_ISCSI);
        newVmStatic.setBiosType(BiosType.Q35_SEA_BIOS);
        newVmStatic.setVmtGuid(FixturesTool.VM_TEMPLATE_RHEL5);
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
     * Ensures that get by name and namespace for cluster works as expected
     */
    @Test
    public void testGetByNameAndNamespaceForCluster() {
        VM result = dao.getByNameAndNamespaceForCluster(existingVm.getClusterId(),
                existingVm.getName(),
                existingVm.getNamespace());

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
        assertEquals(1, result.getDbGeneration(), "Vm db generation wasn't loaded as expected");
        assertEquals(result, existingVm);
    }

    /**
     * Gets the VM associated with the specified image.
     */
    @Test
    public void testGetForDisk() {
        Map<Boolean, List<VM>> result = dao.getForDisk(FixturesTool.IMAGE_GROUP_ID, true);

        assertNotNull(result);
        assertEquals(1, result.get(Boolean.TRUE).size(), "wrong number of VMs with unplugged image");
    }

    /**
     * Gets list of VMs which associated with the specified disk id.
     */
    @Test
    public void testGetVmsListForDisk() {
        List<VM> result = dao.getVmsListForDisk(FixturesTool.IMAGE_GROUP_ID, false);

        assertNotNull(result);
        assertEquals(1, result.size(), "wrong number of VMs");
    }

    /**
     * Ensures that getting all VMs works as expected.
     */
    @Test
    public void testGetAll() {
        List<VM> result = dao.getAll();

        assertCorrectGetAllResult(result);
    }

    @Test
    public void testGetVmsByIds() {
        List<VM> result = dao.getVmsByIds(Arrays.asList(FixturesTool.VM_RHEL5_POOL_60, FixturesTool.VM_RHEL5_POOL_59));
        assertEquals(2, result.size(), "loaded templates list isn't in the expected size");
        Collection<Guid> recieved = result.stream().map(VM::getId).collect(Collectors.toList());
        assertTrue(recieved.contains(FixturesTool.VM_RHEL5_POOL_60), "the received list didn't contain an expected VM");
        assertTrue(recieved.contains(FixturesTool.VM_RHEL5_POOL_59), "the received list didn't contain an expected VM");
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
        List<VM> result = dao.getVmsListByInstanceType(FixturesTool.INSTANCE_TYPE);
        assertEquals(1, result.size());
        assertEquals(FixturesTool.VM_RHEL5_POOL_50, result.iterator().next().getId());
    }

    /**
     * Ensures that retrieving VMs works as expected with filtering disabled for an unprivileged user.
     */
    @Test
    public void testGetAllWithPermissionsDisabledUnprivilegedUser() {
        List<VM> result = dao.getAll(UNPRIVILEGED_USER_ID, false);

        assertCorrectGetAllResult(result);
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
            assertEquals(FixturesTool.QUOTA_GENERAL, vm.getQuotaId(), "Wrong quota id");
            assertEquals("Quota General", vm.getQuotaName(), "Wrong quota name");
            assertFalse(vm.isQuotaDefault(), "Quota shouldn't be default");
            assertEquals(QuotaEnforcementTypeEnum.DISABLED, vm.getQuotaEnforcementType(),
                    "Wrong quota enforcement type");
        }
    }

    /**
     * Ensures that getting all VMs for a storage domain works as expected for a domain without VMs.
     */
    @Test
    public void testGetAllForStorageDomainWithVms() {
        List<VM> result = dao.getAllForStorageDomain(FixturesTool.STORAGE_DOMAIN_SCALE_SD5);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(FixturesTool.VM_RHEL5_POOL_57, result.get(0).getId());
    }

    /**
     * Ensures that getting all VMs for a storage domain works as expected for a domain without VMs.
     */
    @Test
    public void testGetAllForStorageDomainWithoutVMs() {
        List<VM> result = dao.getAllForStorageDomain(FixturesTool.STORAGE_DOMAIN_SCALE_SD6);

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
    public void testGetMonitoredVmsRunningByVds() {
        List<VM> result = dao.getMonitoredVmsRunningByVds(FixturesTool.VDS_RHEL6_NFS_SPM);

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
        List<VM> result = dao.getAllActiveForStorageDomain(FixturesTool.STORAGE_DOMAIN_SCALE_SD5);

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
        List<VM> result = dao.getAllWithTemplate(FixturesTool.VM_TEMPLATE_RHEL5);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VM vm : result) {
            assertEquals(FixturesTool.VM_TEMPLATE_RHEL5, vm.getVmtGuid());
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
            assertEquals(1, vm.getDbGeneration(), "Vm db generation wasn't loaded as expected");
        }
    }

    @Test
    public void testGetAllForVmPool() {
        List<VM> result = dao.getAllForVmPool(FixturesTool.STORAGE_POOL_FEDORA);

        assertNotNull(result);
        assertEquals(2, result.size(), "wrong number of VMs attached to pool");
    }

    @Test
    public void testSaveIsInitialized() {
        Guid vmId = existingVm.getId();
        boolean origInitialized = existingVm.isInitialized();

        // Switch is_initialized
        dao.saveIsInitialized(vmId, !origInitialized);
        VM updatedVm = dao.get(vmId);
        assertEquals(!origInitialized, updatedVm.isInitialized(), "VM's is_initiazlied was not updated");

        // Switch it back, just to make sure
        dao.saveIsInitialized(vmId, origInitialized);
        updatedVm = dao.get(vmId);
        assertEquals(origInitialized, updatedVm.isInitialized(), "VM's is_initiazlied was not updated");
    }

    @Test
    public void testFailedAutoStartVms() {
        List<VM> result = dao.getAllFailedAutoStartVms();

        assertNotNull(result);
        assertEquals(1, result.size(), "wrong number of failed HA VMs");
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

        List<String> notRenamedTemplate = Collections.singletonList("77296e00-0cad-4e5a-9299-008a7b6f4359");

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
        List<VM> result = dao.getAllVMsWithDisksOnOtherStorageDomain(FixturesTool.STORAGE_DOMAIN_SCALE_SD5);

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
        assertEquals(6, result.size());
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
        assertEquals(4, result.size());
    }

    private void createHostedEngineVm(Guid id) {
        VmStatic vmStatic = new VmStatic();
        vmStatic.setId(id);
        vmStatic.setClusterId(FixturesTool.CLUSTER_RHEL6_ISCSI);
        vmStatic.setBiosType(BiosType.Q35_SEA_BIOS);
        vmStatic.setName("HostedEngine");
        vmStatic.setOrigin(OriginType.HOSTED_ENGINE);
        vmStatic.setCpuProfileId(FixturesTool.CPU_PROFILE_1);
        vmStaticDao.save(vmStatic);

        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setId(id);
        vmDynamicDao.save(vmDynamic);

        VmStatistics vmStatistics = new VmStatistics();
        vmStatistics.setId(id);
        vmStatisticsDao.save(vmStatistics);
    }

    @Test
    public void testGetVmsByOrigins() {
        Guid heVmId = Guid.newGuid();
        List<OriginType> list1 = Arrays.asList(OriginType.HOSTED_ENGINE, OriginType.MANAGED_HOSTED_ENGINE);
        List<OriginType> list2 = Collections.singletonList(OriginType.HOSTED_ENGINE);
        List<OriginType> list3 = Collections.singletonList(OriginType.MANAGED_HOSTED_ENGINE);

        int count1 = dao.getVmsByOrigins(list1).size();
        int count2 = dao.getVmsByOrigins(list2).size();
        int count3 = dao.getVmsByOrigins(list3).size();

        createHostedEngineVm(heVmId);

        assertEquals(count1 + 1, dao.getVmsByOrigins(list1).size());
        assertEquals(count2 + 1, dao.getVmsByOrigins(list2).size());
        assertEquals(count3, dao.getVmsByOrigins(list3).size());

        dao.remove(heVmId);
    }

    @Test
    public void testGetVmsPinnedToHost() {
        List<VM> vms = dao.getAllPinnedToHost(FixturesTool.VDS_RHEL6_NFS_SPM);

        assertNotNull(vms);
        assertThat(vms)
                .hasSize(5)
                .extracting(VM::getId)
                .contains(FixturesTool.VM_RHEL5_POOL_57,
                        FixturesTool.VM_RHEL5_POOL_52,
                        FixturesTool.VM_RHEL5_POOL_50,
                        FixturesTool.VM_RHEL5_POOL_51,
                        FixturesTool.VM_WITH_NO_ATTACHED_DISKS);
    }

}

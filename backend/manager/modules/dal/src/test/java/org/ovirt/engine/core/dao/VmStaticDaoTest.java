package org.ovirt.engine.core.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.ConsoleDisconnectAction;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
public class VmStaticDaoTest extends BaseGenericDaoTestCase<Guid, VmStatic, VmStaticDao> {
    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.ApplicationMode, 255));
    }

    protected static final Guid[] HOST_GUIDS = { FixturesTool.HOST_WITH_NO_VFS_CONFIGS_ID,
            FixturesTool.HOST_ID,
            FixturesTool.GLUSTER_BRICK_SERVER1};

    private static final String RUNNING_NAME_WITH_LEASE_ON_STORAGE_DOMAIN = "rhel5-pool-57";

    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private PermissionDao permissionsDao;
    @Inject
    private DiskDao diskDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private VmDeviceDao vmDeviceDao;

    @Override
    protected VmStatic generateNewEntity() {
        VmStatic newVmStatic = new VmStatic();
        newVmStatic.setId(Guid.newGuid());
        newVmStatic.setName("New Virtual Machine");
        newVmStatic.setClusterId(FixturesTool.CLUSTER);
        newVmStatic.setBiosType(BiosType.Q35_SEA_BIOS);
        newVmStatic.setVmtGuid(FixturesTool.VM_TEMPLATE_RHEL5);
        newVmStatic.setOrigin(OriginType.OVIRT);
        newVmStatic.setQuotaId(FixturesTool.QUOTA_GENERAL);
        newVmStatic.setCpuProfileId(FixturesTool.CPU_PROFILE_1);
        newVmStatic.setSmallIconId(FixturesTool.SMALL_ICON_ID);
        newVmStatic.setLargeIconId(FixturesTool.LARGE_ICON_ID);
        newVmStatic.setConsoleDisconnectAction(ConsoleDisconnectAction.REBOOT);
        newVmStatic.setUseTscFrequency(true);
        return newVmStatic;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setDescription("updated");
        existingEntity.setCpuProfileId(FixturesTool.CPU_PROFILE_2);
        existingEntity.setProviderId(null);

        List<Guid> hostGuidsList = Arrays.asList(HOST_GUIDS);
        existingEntity.setDedicatedVmForVdsList(hostGuidsList);
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
        return 3;
    }

    /**
     * Ensures that all VMs are returned.
     */
    @Test
    public void testGetAllStaticByName() {
        List<VmStatic> result = dao.getAllByName(FixturesTool.VM_RHEL5_POOL_50_NAME);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VmStatic vm : result) {
            assertEquals(FixturesTool.VM_RHEL5_POOL_50_NAME, vm.getName());
        }
    }

    /**
     * Ensures that all VMs are returned from storage pool
     */
    @Test
    public void testGetAllStaticByStoragePool() {
        List<VmStatic> result = dao.getAllByStoragePoolId(FixturesTool.DATA_CENTER);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that all static vm details for the specified group and network are returned.
     */
    @Test
    public void testGetAllByGroupAndNetwork() {
        List<VmStatic> result = dao.getAllByGroupAndNetworkName(
                FixturesTool.CLUSTER, "engine");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VmStatic vm : result) {
            assertEquals(FixturesTool.CLUSTER, vm.getClusterId());
        }
    }

    /**
     * Ensures that all static VMs for the specified VDS group are returned.
     */
    @Test
    public void testGetAllByCluster() {
        List<VmStatic> result = dao.getAllByCluster(FixturesTool.CLUSTER);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VmStatic vm : result) {
            assertEquals(FixturesTool.CLUSTER, vm.getClusterId());
        }
    }

    @Test
    public void testGetAll() {
        assertThrows(UnsupportedOperationException.class, () -> dao.getAll());
    }

    @Test
    @Override
    public void testRemove() {
        for (Snapshot s : snapshotDao.getAll()) {
            snapshotDao.remove(s.getId());
        }

        dao.remove(getExistingEntityId());
        VmStatic result = dao.get(getExistingEntityId());
        assertNull(result);
        assertEquals(0, permissionsDao.getAllForEntity(getExistingEntityId()).size(), "vm permissions wasn't removed");
    }

    @Test
    public void testRemoveWithoutPermissions() {
        for (Snapshot s : snapshotDao.getAll()) {
            snapshotDao.remove(s.getId());
        }

        int numberOfPermissionsBeforeRemove = permissionsDao.getAllForEntity(getExistingEntityId()).size();

        dao.remove(getExistingEntityId(), false);
        VmStatic result = dao.get(getExistingEntityId());
        assertNull(result);

        assertEquals(numberOfPermissionsBeforeRemove, permissionsDao.getAllForEntity(getExistingEntityId()).size(), "vm permissions changed during remove although shouldnt have.");
    }

    private void checkDisks(Guid id, boolean hasDisks) {
        assertEquals(hasDisks, !diskDao.getAllForVm(id).isEmpty());
    }

    private void checkVmsDcAndDisks(List<Guid> vmIds, Guid storagePoolId, boolean hasDisks) {
        for (Guid vmId : vmIds) {
            assertEquals(storagePoolId, vmDao.get(vmId).getStoragePoolId());
            checkDisks(vmId, hasDisks);
        }
    }

    private void checkTemplatesDcAndDisks(List<Guid> templateIds, Guid storagePoolId, boolean hasDisks) {
        for (Guid templateId : templateIds) {
            assertEquals(storagePoolId, vmTemplateDao.get(templateId).getStoragePoolId());
            checkDisks(templateId, hasDisks);
        }
    }

    @Test
    public void getVmAndTemplatesIdsWithoutAttachedImageDisks() {
        List<Guid> disklessVms = Arrays.asList(FixturesTool.VM_WITH_NO_ATTACHED_DISKS, FixturesTool.VM_RHEL5_POOL_51);
        List<Guid> disklessTemplates = Collections.singletonList(FixturesTool.VM_TEMPLATE_RHEL5_2);
        List<Guid> diskVms = Collections.singletonList(FixturesTool.VM_RHEL5_POOL_57);
        List<Guid> diskTemplates = Collections.singletonList(FixturesTool.VM_TEMPLATE_RHEL5);

        Guid dataCenterId = FixturesTool.DATA_CENTER;

        checkTemplatesDcAndDisks(disklessTemplates, dataCenterId, false);
        checkVmsDcAndDisks(disklessVms, dataCenterId, false);
        checkTemplatesDcAndDisks(diskTemplates, dataCenterId, true);
        checkVmsDcAndDisks(diskVms, dataCenterId, true);

        // attaching shareable and snapshots disk to a diskless vm
        addVmDevice(disklessVms.get(0), FixturesTool.IMAGE_GROUP_ID_2, null);
        addVmDevice(disklessVms.get(0), FixturesTool.DISK_ID, FixturesTool.EXISTING_SNAPSHOT_ID);

        List<Guid> ids =
                dao.getVmAndTemplatesIdsWithoutAttachedImageDisks(dataCenterId, false);

        assertTrue(ids.containsAll(disklessVms));
        assertTrue(ids.containsAll(disklessTemplates));
        assertTrue(Collections.disjoint(ids, diskVms));
        assertTrue(Collections.disjoint(ids, diskTemplates));

        assertTrue(dao.getVmAndTemplatesIdsWithoutAttachedImageDisks(Guid.newGuid(), false).isEmpty());
    }

    private void addVmDevice(Guid vmId, Guid diskId, Guid snapshotId) {
        VmDevice device = new VmDevice(new VmDeviceId(diskId, vmId),
                VmDeviceGeneralType.DISK,
                VmDeviceType.DISK.getName(),
                "",
                null,
                true,
                false,
                false,
                "",
                null,
                snapshotId,
                null);
        vmDeviceDao.save(device);
    }

    @Test
    public void testGetDbGeneration() {
        Long version = dao.getDbGeneration(FixturesTool.VM_RHEL5_POOL_50);
        assertNotNull(version, "db generation shouldn't be null");
        assertEquals(1, version.longValue(), "db generation should be 1 by default for vm");
    }

    @Test
    public void testIncrementDbGenerationForAllInStoragePool() {
        dao.incrementDbGenerationForAllInStoragePool(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);

        Long version = dao.getDbGeneration(FixturesTool.VM_RHEL5_POOL_50);
        assertEquals(2, version.longValue(), "db geneeration wasn't incremented to all vms in pool");
        version = dao.getDbGeneration(FixturesTool.VM_RHEL5_POOL_51);
        assertEquals(2, version.longValue(), "db generation wasn't incremented to all vms in pool");
        version = dao.getDbGeneration(FixturesTool.VM_RHEL5_POOL_60);
        assertEquals(1, version.longValue(), "db generation was incremented for vm in different pool");
    }

    @Test
    public void testIncrementDbGeneration() {
        dao.incrementDbGeneration(FixturesTool.VM_RHEL5_POOL_50);
        Long version = dao.getDbGeneration(FixturesTool.VM_RHEL5_POOL_50);
        assertEquals(2, version.longValue(), "db generation wasn't incremented as expected");
    }

    /**
     * Checking if the function gets the VmStatics in correct order according to priority
     */
    @Test
    public void testGetOrderedVmGuidsForRunMultipleActionsByPriority() {
        List<VmStatic> vmStatics = dao.getAllByName(FixturesTool.VM_RHEL5_POOL_50_NAME);
        VmStatic[] vmStaticArrayInDescOrder = initVmStaticsOrderedByPriority(vmStatics);

        // execute
        List<Guid> vmStaticGuidsInDb =
                dao.getOrderedVmGuidsForRunMultipleActions(getListOfGuidFromListOfVmStatics(vmStatics));
        assertNotNull(vmStaticGuidsInDb);
        Guid[] guidsArrayToBeChecked = vmStaticGuidsInDb.toArray(new Guid[vmStaticGuidsInDb.size()]);

        boolean result = compareGuidArrays(guidsArrayToBeChecked, vmStaticArrayInDescOrder);
        assertTrue(result);
    }

    /**
     * Checking if the function gets the VmStatics in correct order according to auto_startup
     */
    @Test
    public void testGetOrderedVmGuidsForRunMultipleActionsByAutoStartup() {
        List<VmStatic> vmStatics = dao.getAllByName(FixturesTool.VM_RHEL5_POOL_50_NAME);
        VmStatic[] vmStaticArrayInDescOrder = initVmStaticsOrderedByAutoStartup(vmStatics);

        // execute
        List<Guid> vmStaticGuidsInDb =
                dao.getOrderedVmGuidsForRunMultipleActions(getListOfGuidFromListOfVmStatics(vmStatics));
        assertNotNull(vmStaticGuidsInDb);
        Guid[] guidsArrayToBeChecked = vmStaticGuidsInDb.toArray(new Guid[vmStaticGuidsInDb.size()]);

        boolean result = compareGuidArrays(guidsArrayToBeChecked, vmStaticArrayInDescOrder);
        assertTrue(result);
    }

    /**
     * Checking if the function gets the VmStatics in correct order according to MigrationSupport
     */
    @Test
    public void testGetOrderedVmGuidsForRunMultipleActionsByMigrationSupport() {
        List<VmStatic> vmStatics = dao.getAllByName(FixturesTool.VM_RHEL5_POOL_50_NAME);
        if (vmStatics.size() > 3) { // migration support has only 3 possible values
            vmStatics = vmStatics.subList(0, 3);
        }
        VmStatic[] vmStaticArrayInDescOrder = initVmStaticsOrderedByMigrationSupport(vmStatics);

        // execute
        List<Guid> vmStaticGuidsInDb =
                dao.getOrderedVmGuidsForRunMultipleActions(getListOfGuidFromListOfVmStatics(vmStatics));
        assertNotNull(vmStaticGuidsInDb);
        Guid[] guidsArrayToBeChecked = vmStaticGuidsInDb.toArray(new Guid[vmStaticGuidsInDb.size()]);

        boolean result = compareGuidArrays(guidsArrayToBeChecked, vmStaticArrayInDescOrder);
        assertTrue(result);
    }

    /**
     * {@code initVmStaticsOrderedByAutoStartup(List)} is the first method in VMs order selection tests. The other init
     * methods: <br>
     * {@code initVmStaticsOrderedByPriority} and {@code initVmStaticsOrderedByAutoStartup} are relying on each other
     * for creating an array of VM Static objects.<br>
     * Each of the methods modifies the VM static array according to the column which is being tested, started from the
     * least important column to the most.<br>
     * That way prioritizing a preceded column should be reflected in the selection and therefore to validate the order
     * is maintained.
     *
     * @return an array of VmStatics, in descending order according to: auto_startup, priority, MigrationSupport.<br>
     *         The MigrationSupport is the one being checked.<br>
     */
    private VmStatic[] initVmStaticsOrderedByMigrationSupport(List<VmStatic> vmStatics) {
        VmStatic[] vmStaticArray = new VmStatic[getEntitiesTotalCount()];

        vmStaticArray = vmStatics.toArray(vmStaticArray);

        // initialize the VMs with equal settings: non HA, priority 1 and MIGRATABLE
        for (VmStatic element : vmStaticArray) {
            element.setAutoStartup(false);
            element.setPriority(1);
            element.setMigrationSupport(MigrationSupport.MIGRATABLE);
        }

        // set higher migration support value for the first VM
        vmStaticArray[0].setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
        vmStaticArray[1].setMigrationSupport(MigrationSupport.IMPLICITLY_NON_MIGRATABLE);
        updateArrayOfVmStaticsInDb(vmStaticArray);
        return vmStaticArray;
    }

    /**
     * Creates an array of VM static which was initiated for MigrationSupport order, and modified the priority to
     * reflect the precedence of the priority column on top the MigrationSupport.
     *
     * @return an array of VmStatics, in descending order according to: auto_startup, priority, MigrationSupport. The
     *         priority is the one being checked.
     */
    private VmStatic[] initVmStaticsOrderedByPriority(List<VmStatic> vmStatics) {
        VmStatic[] vmStaticArray = initVmStaticsOrderedByMigrationSupport(vmStatics);

        // Swapping the first two VmStatics
        VmStatic tempVmStatic = vmStaticArray[0];
        vmStaticArray[0] = vmStaticArray[1];
        vmStaticArray[1] = tempVmStatic;

        int arrayLength = vmStaticArray.length;

        // Setting the array in descending order due to their priorities to maintain its correctness
        for (int i = 0; i < arrayLength; i++) {
            vmStaticArray[i].setPriority(arrayLength - i + 1);
        }

        updateArrayOfVmStaticsInDb(vmStaticArray);
        return vmStaticArray;
    }

    /**
     * Creates an array of VM static which was initiated for Priority and MigrationSupport order, and modified the
     * auto-startup to reflect the precedence of the auto-startup column on top the Priority.
     *
     * @return an array of VmStatics, in descending order according to: auto_startup, priority, MigrationSupport. The
     *         auto_startup is the one being checked
     */
    private VmStatic[] initVmStaticsOrderedByAutoStartup(List<VmStatic> vmStatics) {
        VmStatic[] vmStaticArray = initVmStaticsOrderedByPriority(vmStatics);

        // Swapping the first two VmStatics
        VmStatic tempVmStatic = vmStaticArray[0];
        vmStaticArray[0] = vmStaticArray[1];
        vmStaticArray[1] = tempVmStatic;

        // Maintaining the order correctness of the elements by incrementing the auto_startup of the first element
        vmStaticArray[0].setAutoStartup(true);

        updateArrayOfVmStaticsInDb(vmStaticArray);
        return vmStaticArray;
    }

    /**
     * Updates the given array of vmStatics in the Database
     */
    private void updateArrayOfVmStaticsInDb(VmStatic[] vmStaticArray) {
        for (VmStatic element : vmStaticArray) {
            dao.update(element);
        }
    }

    /**
     * Converts a list of vmStatics to a list if Guids
     */
    private static List<Guid> getListOfGuidFromListOfVmStatics(List<VmStatic> vmStatics) {
        return vmStatics.stream().map(VmBase::getId).collect(Collectors.toList());
    }

    /**
     * Compares between the two given guid arrays, returns true if they are equal and false otherwise
     */
    private static boolean compareGuidArrays(Guid[] guidsArrayToBeChecked, VmStatic[] vmStaticArrayInDescOrder) {
        return guidsArrayToBeChecked.length == vmStaticArrayInDescOrder.length &&
                IntStream.range(0, guidsArrayToBeChecked.length)
                        .allMatch(i -> guidsArrayToBeChecked[i].equals(vmStaticArrayInDescOrder[i].getId()));
    }

    @Test
    public void testUpdateVmCpuProfileIdForClusterId() {
        Guid clusterId = FixturesTool.CLUSTER_RHEL6_ISCSI;
        Guid cpuProfileId = FixturesTool.CPU_PROFILE_2;

        testAllCpuProfileValuesEqualTo(clusterId, cpuProfileId, false);
        dao.updateVmCpuProfileIdForClusterId(clusterId, cpuProfileId);
        testAllCpuProfileValuesEqualTo(clusterId, cpuProfileId, true);
    }

    private void testAllCpuProfileValuesEqualTo(Guid clusterId, Guid cpuProfileId, boolean isAllNull) {
        List<VmStatic> allByCluster = dao.getAllByCluster(clusterId);
        assertNotNull(allByCluster);
        assertFalse(allByCluster.isEmpty());
        boolean allValues = true;
        for (VmStatic vmStatic : allByCluster) {
            allValues &= Objects.equals(vmStatic.getCpuProfileId(), cpuProfileId);
        }
        assertEquals(isAllNull, allValues);
    }

    @Test
    public void testGetAllWithLeaseOnStorageDomain() {
        List<Guid> vmAndTemplatesWithLeasesIds = dao.getAllWithLeaseOnStorageDomain(FixturesTool.STORAGE_DOMAIN_NFS2_1)
                .stream().map(VmBase::getId).collect(Collectors.toList());
        assertThat(vmAndTemplatesWithLeasesIds,
                Matchers.contains(FixturesTool.VM_RHEL5_POOL_57));
    }

    @Test
    public void testGetAllActiveWithLeaseOnForStorageDomain() {
        List<String> runningVmsWithLeasesIds = dao.getAllRunningNamesWithLeaseOnStorageDomain(FixturesTool.STORAGE_DOMAIN_NFS2_1);
        assertThat(runningVmsWithLeasesIds, Matchers.contains(RUNNING_NAME_WITH_LEASE_ON_STORAGE_DOMAIN));
    }

    @Test
    public void testGetAllRunningForVds() {
        List<VmStatic> vms = dao.getAllRunningForVds(FixturesTool.VDS_RHEL6_NFS_SPM);
        assertEquals(4, vms.size());
        List<Guid> vmIds = vms.stream().map(VmStatic::getId).collect(Collectors.toList());
        assertTrue(vmIds.contains(FixturesTool.VM_RHEL5_POOL_57));
        assertFalse(vmIds.contains(FixturesTool.VM_RHEL5_POOL_52));
    }

    @Test
    public void testGetAllRunningNamesWithIsoOnStorageDomain() {
        List<String> vms = dao.getAllRunningNamesWithIsoOnStorageDomain(FixturesTool.STORAGE_DOMAIN_WITH_ISO);

        assertNotNull(vms);
        assertTrue(vms.contains(FixturesTool.VM_NAME_WITH_MOUNTED_ISO));
    }
}

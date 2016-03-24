package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.ConsoleDisconnectAction;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;

public class VmStaticDaoTest extends BaseDaoTestCase {
    private static final Guid EXISTING_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355");
    private static final Guid VDS_STATIC_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6");
    private static final Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
    private static final Guid QUOTA_ID = new Guid("88296e00-0cad-4e5a-9291-008a7b7f4399");
    private static final Guid SMALL_ICON_ID = new Guid("38fc5e1a-f96b-339b-9894-def6f366daf5");
    private static final Guid LARGE_ICON_ID = new Guid("a3b954f0-31ff-3166-b7a1-28b23202b198");
    private static final Guid EXISTING_PROVIDER_ID = new Guid("1115c1c6-cb15-4832-b2a4-023770607111");
    protected static final Guid[] HOST_GUIDS = {new Guid("afce7a39-8e8c-4819-ba9c-796d316592e8"),
        new Guid("afce7a39-8e8c-4819-ba9c-796d316592e7"),
        new Guid("23f6d691-5dfb-472b-86dc-9e1d2d3c18f3")};
    private static final String STATIC_VM_NAME = "rhel5-pool-50";
    private static final int NUM_OF_VM_STATIC_IN_FIXTURES = 3;

    private VmStaticDao dao;
    private VmStatic existingVmStatic;
    private VmStatic newVmStatic;
    private VmTemplate vmtemplate;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getVmStaticDao();
        existingVmStatic = dao.get(EXISTING_VM_ID);
        vmtemplate = dbFacade.getVmTemplateDao().get(
                new Guid("1b85420c-b84c-4f29-997e-0eb674b40b79"));
        newVmStatic = new VmStatic();
        newVmStatic.setId(Guid.newGuid());
        newVmStatic.setName("New Virtual Machine");
        newVmStatic.setClusterId(CLUSTER_ID);
        newVmStatic.setVmtGuid(vmtemplate.getId());
        newVmStatic.setOrigin(OriginType.OVIRT);
        newVmStatic.setQuotaId(QUOTA_ID);
        newVmStatic.setCpuProfileId(FixturesTool.CPU_PROFILE_1);
        newVmStatic.setSmallIconId(SMALL_ICON_ID);
        newVmStatic.setLargeIconId(LARGE_ICON_ID);
    }

    /**
     * Ensures that get requires a valid id.
     */
    @Test
    public void testGetWithInvalidId() {
        VmStatic result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that get works as expected.
     */
    @Test
    public void testGet() {
        VmStatic result = dao.get(existingVmStatic.getId());

        assertNotNull(result);
        assertEquals(result, existingVmStatic);
    }

    /**
     * Ensures that all VMs are returned.
     */
    @Test
    public void testGetAllStaticByName() {
        List<VmStatic> result = dao.getAllByName("rhel5-pool-50");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VmStatic vm : result) {
            assertEquals("rhel5-pool-50", vm.getName());
        }
    }

    /**
     * Ensures that all VMs are returned from storage pool
     */
    @Test
    public void testGetAllStaticByStoragePool() {
        Guid spID = dbFacade.getClusterDao().get(newVmStatic.getClusterId()).getStoragePoolId();

        assertNotNull(spID);

        List<VmStatic> result = dao.getAllByStoragePoolId(spID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that all static vm details for the specified group and network are returned.
     */
    @Test
    public void testGetAllByGroupAndNetwork() {
        List<VmStatic> result = dao.getAllByGroupAndNetworkName(
                CLUSTER_ID, "engine");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VmStatic vm : result) {
            assertEquals(CLUSTER_ID, vm.getClusterId());
        }
    }

    /**
     * Ensures that all static VMs for the specified VDS group are returned.
     */
    @Test
    public void testGetAllByCluster() {
        List<VmStatic> result = dao.getAllByCluster(CLUSTER_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VmStatic vm : result) {
            assertEquals(CLUSTER_ID, vm.getClusterId());
        }
    }

    /**
     * Ensures that the right set of VMs are returned.
     */
    @Test
    public void testGetAllWithFailbackByVds() {
        List<VmStatic> result = dao.getAllWithFailbackByVds(VDS_STATIC_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VmStatic vm : result) {
            assertEquals(CLUSTER_ID, vm.getClusterId());
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetAll() {
        dao.getAll();
    }

    @Test
    public void testSave() {
        newVmStatic.setDedicatedVmForVdsList(Arrays.asList(HOST_GUIDS));
        dao.save(newVmStatic);
        VmStatic result = dao.get(newVmStatic.getId());
        assertNotNull(result);
        assertTrue("Add 3 dedicated hosts", CollectionUtils.isEqualCollection(result.getDedicatedVmForVdsList(),
                newVmStatic.getDedicatedVmForVdsList()));
        assertEquals(newVmStatic, result);
    }

    @Test
    public void testUpdate() {
        assertEquals(existingVmStatic.getProviderId(), EXISTING_PROVIDER_ID);
        existingVmStatic.setDescription("updated");
        existingVmStatic.setCpuProfileId(FixturesTool.CPU_PROFILE_2);
        existingVmStatic.setProviderId(null);
        List<Guid> hostGuidsList = Arrays.asList(HOST_GUIDS);
        existingVmStatic.setDedicatedVmForVdsList(hostGuidsList);
        dao.update(existingVmStatic);
        VmStatic result = dao.get(EXISTING_VM_ID);
        assertNotNull(result);
        assertTrue("Update dedicated hosts", CollectionUtils.isEqualCollection(result.getDedicatedVmForVdsList(),
                existingVmStatic.getDedicatedVmForVdsList()));
        assertEquals(existingVmStatic, result);
        assertNull(result.getProviderId());

        hostGuidsList = new LinkedList<>();
        hostGuidsList.add(HOST_GUIDS[0]);
        hostGuidsList.add(HOST_GUIDS[1]);
        existingVmStatic.setDedicatedVmForVdsList(hostGuidsList);
        dao.update(existingVmStatic);
        result = dao.get(EXISTING_VM_ID);
        // assert 1 dedicated hosts
        assertTrue("Reduce dedicated hosts", CollectionUtils.isEqualCollection(result.getDedicatedVmForVdsList(),
                existingVmStatic.getDedicatedVmForVdsList()));
    }

    @Test
    public void testRemove() {
        for (Snapshot s : dbFacade.getSnapshotDao().getAll()) {
            dbFacade.getSnapshotDao().remove(s.getId());
        }

        dao.remove(EXISTING_VM_ID);
        VmStatic result = dao.get(EXISTING_VM_ID);
        assertNull(result);
        PermissionDao permissionsDao = dbFacade.getPermissionDao();
        assertEquals("vm permissions wasn't removed", 0, permissionsDao.getAllForEntity(EXISTING_VM_ID).size());
    }

    @Test
    public void testRemoveWithoutPermissions() {
        for (Snapshot s : dbFacade.getSnapshotDao().getAll()) {
            dbFacade.getSnapshotDao().remove(s.getId());
        }

        PermissionDao permissionsDao = dbFacade.getPermissionDao();
        int numberOfPermissionsBeforeRemove = permissionsDao.getAllForEntity(EXISTING_VM_ID).size();

        dao.remove(EXISTING_VM_ID, false);
        VmStatic result = dao.get(EXISTING_VM_ID);
        assertNull(result);

        assertEquals("vm permissions changed during remove although shouldnt have.", numberOfPermissionsBeforeRemove, permissionsDao.getAllForEntity(EXISTING_VM_ID).size());
    }

    private void checkDisks(Guid id, boolean hasDisks) {
        assertEquals(hasDisks, !dbFacade.getDiskDao().getAllForVm(id).isEmpty());
    }

    private void checkVmsDcAndDisks(List<Guid> vmIds, Guid storagePoolId, boolean hasDisks) {
        for (Guid vmId : vmIds) {
            assertEquals(storagePoolId, dbFacade.getVmDao().get(vmId).getStoragePoolId());
            checkDisks(vmId, hasDisks);
        }
    }

    private void checkTemplatesDcAndDisks(List<Guid> templateIds, Guid storagePoolId, boolean hasDisks) {
        for (Guid templateId : templateIds) {
            assertEquals(storagePoolId, dbFacade.getVmTemplateDao().get(templateId).getStoragePoolId());
            checkDisks(templateId, hasDisks);
        }
    }

    @Test
    public void getVmAndTemplatesIdsWithoutAttachedImageDisks() {
        List<Guid> disklessVms = Arrays.asList(FixturesTool.VM_WITH_NO_ATTACHED_DISKS, FixturesTool.VM_RHEL5_POOL_51);
        List<Guid> disklessTemplates = Arrays.asList(FixturesTool.VM_TEMPLATE_RHEL5_2);
        List<Guid> diskVms = Arrays.asList(FixturesTool.VM_RHEL5_POOL_57);
        List<Guid> diskTemplates = Arrays.asList(FixturesTool.VM_TEMPLATE_RHEL5);

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
                0,
                null,
                true,
                false,
                false,
                "",
                null,
                snapshotId,
                null);
        dbFacade.getVmDeviceDao().save(device);
    }

    @Test
    public void testGetAllNamesPinnedToHostReturnsNothingForRandomHost() throws Exception {
        assertTrue(dao.getAllNamesPinnedToHost(Guid.newGuid()).isEmpty());
    }


    @Test
    public void testGetDbGeneration() {
        Long version = dao.getDbGeneration(FixturesTool.VM_RHEL5_POOL_50);
        assertNotNull("db generation shouldn't be null", version);
        assertEquals("db generation should be 1 by default for vm", 1, version.longValue());
    }

    @Test
    public void testIncrementDbGenerationForAllInStoragePool() {
        dao.incrementDbGenerationForAllInStoragePool(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);

        Long version = dao.getDbGeneration(FixturesTool.VM_RHEL5_POOL_50);
        assertEquals("db geneeration wasn't incremented to all vms in pool", 2, version.longValue());
        version = dao.getDbGeneration(FixturesTool.VM_RHEL5_POOL_51);
        assertEquals("db generation wasn't incremented to all vms in pool", 2, version.longValue());
        version = dao.getDbGeneration(FixturesTool.VM_RHEL5_POOL_60);
        assertEquals("db generation was incremented for vm in different pool", 1, version.longValue());
    }

    @Test
    public void testIncrementDbGeneration() {
        dao.incrementDbGeneration(FixturesTool.VM_RHEL5_POOL_50);
        Long version = dao.getDbGeneration(FixturesTool.VM_RHEL5_POOL_50);
        assertEquals("db generation wasn't incremented as expected", 2, version.longValue());
    }

    @Test
    public void testGetAllNamesPinnedToHostReturnsNothingForHostButNotPinned() throws Exception {
        assertTrue(dao.getAllNamesPinnedToHost(new Guid("afce7a39-8e8c-4819-ba9c-796d316592e7")).isEmpty());
    }

    @Test
    public void testGetAllNamesPinnedToHostReturnsVmNameForHostPinned() throws Exception {
        List<String> namesPinnedToHost = dao.getAllNamesPinnedToHost(VDS_STATIC_ID);

        assertFalse(namesPinnedToHost.isEmpty());
        assertTrue(namesPinnedToHost.contains(existingVmStatic.getName()));
    }

    /**
     * Checking if the function gets the VmStatics in correct order according to priority
     */
    @Test
    public void testGetOrderedVmGuidsForRunMultipleActionsByPriority() {
        List<VmStatic> vmStatics = dao.getAllByName(STATIC_VM_NAME);
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
        List<VmStatic> vmStatics = dao.getAllByName(STATIC_VM_NAME);
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
        List<VmStatic> vmStatics = dao.getAllByName(STATIC_VM_NAME);
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
        VmStatic[] vmStaticArray = new VmStatic[NUM_OF_VM_STATIC_IN_FIXTURES];

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
        List<Guid> listOfGuidToReturn = new ArrayList<>();
        for (VmStatic vmStatic : vmStatics) {
            listOfGuidToReturn.add(vmStatic.getId());
        }
        return listOfGuidToReturn;
    }

    /**
     * Compares between the two given guid arrays, returns true if they are equal and false otherwise
     */
    private static boolean compareGuidArrays(Guid[] guidsArrayToBeChecked, VmStatic[] vmStaticArrayInDescOrder) {
        boolean returnValue = true;
        if (guidsArrayToBeChecked.length == vmStaticArrayInDescOrder.length) {
            for (int i = 0; i < guidsArrayToBeChecked.length; i++) {
                if (!guidsArrayToBeChecked[i].equals(vmStaticArrayInDescOrder[i].getId())) {
                    returnValue = false;
                    break;
                }
            }
        }

        return returnValue;
    }

    @Test
    public void testUpdateVmCpuProfileIdForClusterId() {
        updateCpuProfile(FixturesTool.CLUSTER_RHEL6_ISCSI, FixturesTool.CPU_PROFILE_2);
    }

    @Test
    public void testUpdateNullVmCpuProfileIdForClusterId() {
        updateCpuProfile(FixturesTool.CLUSTER_RHEL6_ISCSI, null);
    }

    private void updateCpuProfile(Guid clusterId, Guid cpuProfileId) {
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
    public void testConsoleDisconnectActionDefault() {
        assertEquals(newVmStatic.getConsoleDisconnectAction(), ConsoleDisconnectAction.LOCK_SCREEN);
    }

    @Test
    public void testConsoleDisconnectActionSaved() {
        newVmStatic.setConsoleDisconnectAction(ConsoleDisconnectAction.REBOOT);
        dao.save(newVmStatic);
        VmStatic loaded = dao.get(newVmStatic.getId());
        assertEquals(loaded.getConsoleDisconnectAction(), ConsoleDisconnectAction.REBOOT);
    }
}

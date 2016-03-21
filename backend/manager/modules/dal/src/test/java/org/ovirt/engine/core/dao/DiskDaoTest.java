package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.compat.Guid;

public class DiskDaoTest extends BaseReadDaoTestCase<Guid, Disk, DiskDao> {

    private static final int TOTAL_DISK_IMAGES = 8;

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.DISK_ID;
    }

    @Override
    protected DiskDao prepareDao() {
        return dbFacade.getDiskDao();
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.Empty;
    }

    @Override
    protected int getEneitiesTotalCount() {
        return TOTAL_DISK_IMAGES + DiskLunMapDaoTest.TOTAL_DISK_LUN_MAPS;
    }

    @Override
    @Test
    public void testGet() {
        Disk result = dao.get(getExistingEntityId());

        assertNotNull(result);
        assertEquals(getExistingEntityId().toString(), result.getId().toString());
    }

    @Test
    public void testGetFilteredWithPermissions() {
        Disk result = dao.get(getExistingEntityId(), PRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertEquals(getExistingEntityId().toString(), result.getId().toString());
    }

    @Test
    public void testGetFilteredWithoutPermissions() {
        Disk result = dao.get(getExistingEntityId(), UNPRIVILEGED_USER_ID, true);

        assertNull(result);
    }

    @Test
    public void testGetFilteredWithoutPermissionsNoFilter() {
        Disk result = dao.get(getExistingEntityId(), UNPRIVILEGED_USER_ID, false);

        assertNotNull(result);
        assertEquals(getExistingEntityId().toString(), result.getId().toString());
    }

    @Test
    public void testGetFilteredWithPermissionsNoFilter() {
        Disk result = dao.get(getExistingEntityId(), PRIVILEGED_USER_ID, false);

        assertNotNull(result);
        assertEquals(getExistingEntityId().toString(), result.getId().toString());
    }

    @Test
    public void testGetAllForVMFilteredWithPermissions() {
        // test user 3 - has permissions
        List<Disk> disks = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_57, PRIVILEGED_USER_ID, true);
        assertFullGetAllForVMResult(disks);
    }

    @Test
    public void testGetAllForVMFilteredWithPermissionsNoPermissions() {
        // test user 2 - hasn't got permissions
        List<Disk> disks = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_57, UNPRIVILEGED_USER_ID, true);
        assertTrue("VM should have no disks viewable to the user", disks.isEmpty());
    }

    @Test
    public void testGetAllForVMFilteredWithPermissionsNoPermissionsAndNoFilter() {
        // test user 2 - hasn't got permissions, but no filtering was requested
        List<Disk> disks = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_57, UNPRIVILEGED_USER_ID, false);
        assertFullGetAllForVMResult(disks);
    }

    @Test
    public void testGetPluggedForVMFilteredWithPermissions() {
        // test user 3 - has permissions
        List<Disk> disks = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_57, true, PRIVILEGED_USER_ID, true);
        assertPluggedGetAllForVMResult(disks);
    }

    @Test
    public void testGetPluggedForVMFilteredWithPermissionsNoPermissions() {
        // test user 2 - hasn't got permissions
        List<Disk> disks = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_57, true, UNPRIVILEGED_USER_ID, true);
        assertTrue("VM should have no disks viewable to the user", disks.isEmpty());
    }

    @Test
    public void testGetPluggedForVMFilteredWithPermissionsNoPermissionsAndNoFilter() {
        // test user 2 - hasn't got permissions, but no filtering was requested
        List<Disk> disks = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_57, true, UNPRIVILEGED_USER_ID, false);
        assertPluggedGetAllForVMResult(disks);
    }

    @Test
    public void testGetAllForVM() {
        List<Disk> disks = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_57);
        assertFullGetAllForVMResult(disks);
    }

    @Test
    public void testGetAllForVMs() {
        Map<Guid, List<Disk>> vmDisksMap = dao.getAllForVms(Arrays.asList(FixturesTool.VM_RHEL5_POOL_57));

        // TODO - add other VM
        assertTrue(vmDisksMap.containsKey(FixturesTool.VM_RHEL5_POOL_57));
        assertFullGetAllForVMResult(vmDisksMap.get(FixturesTool.VM_RHEL5_POOL_57));
    }

    @Test
    public void testGetAllAttachableDisksByPoolIdNoDisks() {
        List<Disk> result =
                dao.getAllAttachableDisksByPoolId(FixturesTool.STORAGE_POOL_NFS,
                        null,
                        null,
                        false);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllAttachableDisksByPoolIdNull() {
        List<Disk> result =
                dao.getAllAttachableDisksByPoolId(null, null, null, false);

        assertFullGetAllAttachableDisksByPoolId(result);
    }

    @Test
    public void testGetAllAttachableDisksByPoolWithPermissions() {
        List<Disk> result =
                dao.getAllAttachableDisksByPoolId(null, null, PRIVILEGED_USER_ID, true);

        assertFullGetAllAttachableDisksByPoolId(result);
    }

    @Test
    public void testGetAllAttachableDisksByPoolWithNoPermissions() {
        List<Disk> result =
                dao.getAllAttachableDisksByPoolId(null, null, UNPRIVILEGED_USER_ID, true);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllAttachableDisksByPoolWithNoPermissionsFilterDisabled() {
        List<Disk> result =
                dao.getAllAttachableDisksByPoolId(null, null, UNPRIVILEGED_USER_ID, false);

        assertFullGetAllAttachableDisksByPoolId(result);
    }

    @Test
    public void testGetVmBootActiveDisk() {
        Disk bootDisk = dao.getVmBootActiveDisk(FixturesTool.VM_RHEL5_POOL_57);
        assertNotNull("VM should have a boot disk attached", bootDisk);
        assertEquals("Wrong boot disk for VM", bootDisk.getId(), FixturesTool.BOOTABLE_DISK_ID);
    }

    @Test
    public void testGetVmPartialData() {
        List<Disk> disks = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_57, PRIVILEGED_USER_ID, true);
        assertFullGetAllForVMResult(disks);
        assertEquals("New Description", disks.get(0).getDiskDescription());
        assertNotNull(disks.get(0).getDiskAlias());
    }

    @Test
    public void testGetAllFromDisksByDiskStorageType() {
        List<Disk> disks = dao.getAllFromDisksByDiskStorageType(DiskStorageType.CINDER, PRIVILEGED_USER_ID, true);
        assertEquals("We should have one disk", 1, disks.size());
    }

    /**
     * Asserts the result of {@link DiskImageDao#getAllForVm(Guid)} contains the correct disks.
     * @param disks
     *            The result to check
     */
    private static void assertFullGetAllForVMResult(List<Disk> disks) {
        assertEquals("VM should have six disks", 6, disks.size());
    }

    /**
     * Asserts the result of {@link DiskImageDao#getAllForVm(Guid)} contains the correct plugged disks.
     * @param disks
     *            The result to check
     */
    private static void assertPluggedGetAllForVMResult(List<Disk> disks) {
        Integer numberOfDisks = 5;
        assertEquals("VM should have " + numberOfDisks + " plugged disk", numberOfDisks.intValue(), disks.size());
    }

    /**
     * Asserts the result of {@link DiskDao#getAllAttachableDisksByPoolId} contains the floating disk.
     * @param disks
     *            The result to check
     */
    private static void assertFullGetAllAttachableDisksByPoolId(List<Disk> disks) {
        assertEquals("There should be only four attachable disks", 4, disks.size());
        Set<Guid> expectedFloatingDiskIds =
                new HashSet<>(Arrays.asList(FixturesTool.FLOATING_DISK_ID, FixturesTool.FLOATING_LUN_ID,
                        FixturesTool.FLOATING_CINDER_DISK_ID));
        Set<Guid> actualFloatingDiskIds = new HashSet<>();
        for (Disk disk : disks) {
            actualFloatingDiskIds.add(disk.getId());
        }
        assertEquals("Wrong attachable disks", expectedFloatingDiskIds, actualFloatingDiskIds);
    }
}

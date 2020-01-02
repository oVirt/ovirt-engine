package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.compat.Guid;

public class DiskDaoTest extends BaseReadDaoTestCase<Guid, Disk, DiskDao> {

    private static final int TOTAL_DISK_IMAGES = 10;

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.DISK_ID;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.Empty;
    }

    @Override
    protected int getEntitiesTotalCount() {
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
        assertTrue(disks.isEmpty(), "VM should have no disks viewable to the user");
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
        assertTrue(disks.isEmpty(), "VM should have no disks viewable to the user");
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
        Map<Guid, List<Disk>> vmDisksMap = dao.getAllForVms(Collections.singletonList(FixturesTool.VM_RHEL5_POOL_57));

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
        assertNotNull(bootDisk, "VM should have a boot disk attached");
        assertEquals(FixturesTool.BOOTABLE_DISK_ID, bootDisk.getId(), "Wrong boot disk for VM");
    }

    @Test
    public void testGetVmBootActiveSharedDisk() {
        Disk bootDiskVm1 = dao.getVmBootActiveDisk(FixturesTool.VM_VM1_SHARED_BOOTABLE_DISK);
        Disk bootDiskVm2 = dao.getVmBootActiveDisk(FixturesTool.VM_VM2_SHARED_NONBOOTABLE_DISK);
        assertNull(bootDiskVm2, "VM2 should not have a bootable disk attached");
        assertNotNull(bootDiskVm1, "VM1 should have a bootable disk attached");
        assertEquals(FixturesTool.BOOTABLE_SHARED_DISK_ID, bootDiskVm1.getId(), "Wrong boot disk for VM");
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
        assertEquals(1, disks.size(), "We should have one disk");
    }

    /**
     * Asserts the result of {@link DiskDao#getAllForVm(Guid)} contains the correct disks.
     * @param disks
     *            The result to check
     */
    private static void assertFullGetAllForVMResult(List<Disk> disks) {
        assertEquals(6, disks.size(), "VM should have six disks");
    }

    /**
     * Asserts the result of {@link DiskDao#getAllForVm(Guid)} contains the correct plugged disks.
     * @param disks
     *            The result to check
     */
    private static void assertPluggedGetAllForVMResult(List<Disk> disks) {
        Integer numberOfDisks = 5;
        assertEquals(numberOfDisks.intValue(), disks.size(), "VM should have " + numberOfDisks + " plugged disk");
    }

    /**
     * Asserts the result of {@link DiskDao#getAllAttachableDisksByPoolId} contains the floating disk.
     * @param disks
     *            The result to check
     */
    private static void assertFullGetAllAttachableDisksByPoolId(List<Disk> disks) {
        assertEquals(5, disks.size(), "There should be only five attachable disks");
        Set<Guid> expectedFloatingDiskIds = new HashSet<>(Arrays.asList(
                FixturesTool.FLOATING_DISK_ID,
                FixturesTool.FLOATING_LUN_ID,
                FixturesTool.FLOATING_CINDER_DISK_ID,
                FixturesTool.BOOTABLE_SHARED_DISK_ID));
        Set<Guid> actualFloatingDiskIds = disks.stream().map(BaseDisk::getId).collect(Collectors.toSet());
        assertEquals(expectedFloatingDiskIds, actualFloatingDiskIds, "Wrong attachable disks");
    }

    @Test
    public void testGetAllFromDisksIncludingSnapshots() {
        List<Disk> result = dao.getAllFromDisksIncludingSnapshots(null, false);
        assertEquals(17, result.size(), "wrong number of returned disks");
    }

    @Test
    public void testGetAllFromDisksIncludingSnapshotsForUnprivilegedUserWithFilter() {
        List<Disk> result = dao.getAllFromDisksIncludingSnapshots(UNPRIVILEGED_USER_ID, true);
        assertEquals(0, result.size(), "wrong number of returned disks");
    }

    @Test
    public void testGetAllFromDisksIncludingSnapshotsForUnprivilegedUserWithoutFilter() {
        List<Disk> result = dao.getAllFromDisksIncludingSnapshots(UNPRIVILEGED_USER_ID, false);
        assertEquals(17, result.size(), "wrong number of returned disks");
    }

    @Test
    public void testGetAllFromDisksIncludingSnapshotsForPrivilegedUserWithoutFilter() {
        List<Disk> result = dao.getAllFromDisksIncludingSnapshots(PRIVILEGED_USER_ID, false);
        assertEquals(17, result.size(), "wrong number of returned disks");
    }

    @Test
    public void testGetAllFromDisksIncludingSnapshotsForPrivilegedUserWithFilter() {
        List<Disk> result = dao.getAllFromDisksIncludingSnapshots(PRIVILEGED_USER_ID, true);
        assertEquals(15, result.size(), "wrong number of returned disks");
    }

    @Test
    public void testGetAllFromDisksIncludingSnapshotsByDiskId() {
        List<Disk> result = dao.getAllFromDisksIncludingSnapshotsByDiskId(FixturesTool.IMAGE_GROUP_ID, null, false);
        assertEquals(4, result.size(), "wrong number of returned disks");
    }

    @Test
    public void testGetAllFromDisksIncludingSnapshotsByDiskIdForPrivilegedUserWithFilter() {
        List<Disk> result =
                dao.getAllFromDisksIncludingSnapshotsByDiskId(FixturesTool.IMAGE_GROUP_ID, PRIVILEGED_USER_ID, true);
        assertEquals(4, result.size(), "wrong number of returned disks");
    }

    @Test
    public void testGetAllFromDisksIncludingSnapshotsByDiskIdForPrivilegedUserWithoutFilter() {
        List<Disk> result =
                dao.getAllFromDisksIncludingSnapshotsByDiskId(FixturesTool.IMAGE_GROUP_ID, PRIVILEGED_USER_ID, false);
        assertEquals(4, result.size(), "wrong number of returned disks");
    }

    @Test
    public void testGetAllFromDisksIncludingSnapshotsByDiskIdForUnPrivilegedUserWithoutFilter() {
        List<Disk> result =
                dao.getAllFromDisksIncludingSnapshotsByDiskId(FixturesTool.IMAGE_GROUP_ID, UNPRIVILEGED_USER_ID, false);
        assertEquals(4, result.size(), "wrong number of returned disks");
    }

    @Test
    public void testGetAllFromDisksIncludingSnapshotsByDiskIdForUnPrivilegedUserWithFilter() {
        List<Disk> result =
                dao.getAllFromDisksIncludingSnapshotsByDiskId(FixturesTool.IMAGE_GROUP_ID, UNPRIVILEGED_USER_ID, true);
        assertEquals(0, result.size(), "wrong number of returned disks");
    }

    @Test
    public void testGetImagesWithMoreThanOneActiveSnapshotForVm() {
        List<Guid> result =
                dao.getImagesWithDamagedSnapshotForVm(FixturesTool.VM_RHEL5_POOL_57);
        assertEquals(1, result.size(), "wrong number of returned disks");
    }

    @Test
    public void testGetImagesWithNotExistsSnapshotForVm() {
        List<Guid> result =
                dao.getImagesWithDamagedSnapshotForVm(FixturesTool.VM_RHEL5_POOL_60);
        assertEquals(1, result.size(), "wrong number of returned disks");
    }
}

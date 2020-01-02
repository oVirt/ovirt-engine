package org.ovirt.engine.core.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.compat.Guid;

public class DiskVmElementDaoTest extends BaseReadDaoTestCase<VmDeviceId, DiskVmElement, DiskVmElementDao>{

    private static final int NUM_OF_DISKS_ATTACHED_TO_VM = 4;
    private static final int NUM_OF_DISKS_PLUGGED_TO_VM = 3;

    private static final Guid PLUGGED_DISK_ID = FixturesTool.LUN_DISK_ID;
    private static final Guid UNPLUGGED_DISK_ID = FixturesTool.IMAGE_GROUP_ID_2;

    @Test
    public void testGetFilteredWithPermissions() {
        DiskVmElement result = dao.get(getExistingEntityId(), PRIVILEGED_USER_ID, true);
        assertNotNull(result);
        assertEquals(getExistingEntityId().toString(), result.getId().toString());
    }

    @Test
    public void testGetFilteredWithoutPermissions() {
        DiskVmElement result = dao.get(getExistingEntityId(), UNPRIVILEGED_USER_ID, true);
        assertNull(result);
    }

    @Test
    public void testGetAllDiskElementsByDisksIds() {
        List<DiskVmElement> vmElements = dao.getAllDiskVmElementsByDisksIds(Arrays.asList(
                FixturesTool.DISK_ID, FixturesTool.DISK_ID_2, FixturesTool.BOOTABLE_DISK_ID));
        assertEquals(2, vmElements.size());
    }

    @Test
    public void testGetAllForVm() {
        List<DiskVmElement> dves = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_57);
        assertThat(dves.size(), is(NUM_OF_DISKS_ATTACHED_TO_VM));
    }

    @Test
    public void testGetAllForVmWithPermissions() {
        List<DiskVmElement> dves = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_57, PRIVILEGED_USER_ID, true);
        assertThat(dves.size(), is(NUM_OF_DISKS_ATTACHED_TO_VM));
    }

    @Test
    public void testGetAllForVmWithoutPermissions() {
        List<DiskVmElement> dves = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_57, UNPRIVILEGED_USER_ID, true);
        assertThat(dves.size(), is(0));
    }

    @Test
    public void testGetAllPluggedToVm() {
        List<DiskVmElement> dves = dao.getAllPluggedToVm(FixturesTool.VM_RHEL5_POOL_57);
        assertThat(dves.size(), is(NUM_OF_DISKS_PLUGGED_TO_VM));
    }

    @Test
    public void testVmElementDiskPluggedStatus() {
        DiskVmElement dvePlugged = dao.get(new VmDeviceId(PLUGGED_DISK_ID, FixturesTool.VM_RHEL5_POOL_57));
        assertTrue(dvePlugged.isPlugged());
    }

    @Test
    public void testVmElementDiskLogicalName() {
        DiskVmElement dveWithLogicalName = dao.get(new VmDeviceId(PLUGGED_DISK_ID, FixturesTool.VM_RHEL5_POOL_57));
        assertEquals("logical_name", dveWithLogicalName.getLogicalName());
    }

    @Test
    public void testVmElementDiskUnpluggedStatus() {
        DiskVmElement dveUnplugged = dao.get(new VmDeviceId(UNPLUGGED_DISK_ID, FixturesTool.VM_RHEL5_POOL_57));
        assertFalse(dveUnplugged.isPlugged());
    }

    @Test
    public void testNUllVmElementForFloatingDisk() {
        List<DiskVmElement> allDves = dao.getAll();
        assertTrue(allDves.stream().noneMatch(dve -> dve.getDiskId().equals(FixturesTool.FLOATING_DISK_ID)));
    }

    @Test
    public void testUpdateVmDeviceUsingScsiReservationProperty() {
        DiskVmElement dve = dao.get(getExistingEntityId());
        boolean usingScsiReservation = !dve.isUsingScsiReservation();
        dve.setUsingScsiReservation(usingScsiReservation);
        dao.update(dve);
        DiskVmElement dveFromDb = dao.get(getExistingEntityId());
        assertEquals(dveFromDb.isUsingScsiReservation(), usingScsiReservation);
    }



    @Override
    protected VmDeviceId getExistingEntityId() {
        return new VmDeviceId(PLUGGED_DISK_ID, FixturesTool.VM_RHEL5_POOL_57);
    }

    @Override
    protected VmDeviceId generateNonExistingId() {
        return new VmDeviceId(Guid.newGuid(), Guid.newGuid());
    }

    @Override
    protected int getEntitiesTotalCount() {
        return 9;
    }
}

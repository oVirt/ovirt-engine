package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.RandomUtils;

/** A test case for the {@link VmHandler} class. */
public class VmHandlerTest {

    @Rule
    public InjectorRule injectorRule = new InjectorRule();

    @Mock
    CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Before
    public void setUp() {
        injectorRule.bind(CpuFlagsManagerHandler.class, cpuFlagsManagerHandler);
        VmHandler.init();
    }

    @Test
    public void testUpdateFieldsNameInStatusUp() {
        VmStatic src = new VmStatic();
        src.setName(RandomUtils.instance().nextString(10));
        src.setInterfaces(new ArrayList<>(2));

        VmStatic dest = new VmStatic();
        dest.setName(RandomUtils.instance().nextString(10));

        assertTrue("Update should be valid for different names",
                VmHandler.isUpdateValid(src, dest));
    }

    @Test
    public void filterDisksForVmDiskSnapshots() {
        DiskImage snapshotDisk1 = createDiskImage(false);
        DiskImage snapshotDisk2 = createDiskImage(false);
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        List<Disk> disks = new LinkedList<>();
        disks.add(snapshotDisk1);
        disks.add(snapshotDisk2);
        populateVmWithDisks(disks, vm);
        VmHandler.filterImageDisksForVM(vm);
        assertTrue(vm.getDiskList().isEmpty());
        assertTrue(vm.getManagedVmDeviceMap().isEmpty());
    }

    @Test
    public void filterDisksForVmMixedDiskTypes() {
        DiskImage snapshotDisk = createDiskImage(false);
        DiskImage regularDisk = createDiskImage(true);
        LunDisk lunDisk = createLunDisk();
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        populateVmWithDisks(Arrays.asList(snapshotDisk, regularDisk, lunDisk), vm);
        VmHandler.filterImageDisksForVM(vm);
        assertFalse(vm.getDiskList().contains(snapshotDisk));
        assertTrue(vm.getDiskList().contains(regularDisk));
        assertTrue(vm.getManagedVmDeviceMap().containsKey(regularDisk.getId()));
        assertFalse(vm.getManagedVmDeviceMap().containsKey(lunDisk.getId()));
        assertFalse(vm.getManagedVmDeviceMap().containsKey(snapshotDisk.getId()));
    }

    private void populateVmWithDisks(List<Disk> disks, VM vm) {
        VmHandler.updateDisksForVm(vm, disks);
        for (Disk disk : disks) {
            VmDevice device = new VmDevice(new VmDeviceId(disk.getId(), vm.getId()),
                    VmDeviceGeneralType.DISK,
                    VmDeviceType.DISK.getName(),
                    "",
                    0,
                    null,
                    true,
                    true,
                    false,
                    "",
                    null,
                    disk.getDiskStorageType() == DiskStorageType.IMAGE ? ((DiskImage)disk).getSnapshotId() : null,
                    null);
            vm.getManagedVmDeviceMap().put(disk.getId(), device);
        }
    }

    private LunDisk createLunDisk() {
        LunDisk lunDisk = new LunDisk();
        lunDisk.setId(Guid.newGuid());
        return lunDisk;
    }

    private static DiskImage createDiskImage(boolean active) {
        DiskImage di = new DiskImage();
        di.setActive(active);
        di.setId(Guid.newGuid());
        di.setImageId(Guid.newGuid());
        di.setParentId(Guid.newGuid());
        di.setImageStatus(ImageStatus.OK);
        return di;
    }
}

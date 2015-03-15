package org.ovirt.engine.core.bll;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.ovirt.engine.core.bll.memory.MemoryUtils;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.RandomUtils;

/** A test case for the {@link VmHandler} class. */
public class VmHandlerTest {

    public static final long META_DATA_SIZE_IN_GB = 1;
    public static final Integer LOW_SPACE_IN_GB = 3;
    public static final Integer ENOUGH_SPACE_IN_GB = 4;
    public static final Integer THRESHOLD_IN_GB = 4;
    public static final Integer THRESHOLD_HIGH_GB = 10;
    public static final int VM_SPACE_IN_MB = 2000;

    @Rule
    public MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.FreeSpaceCriticalLowInGB, THRESHOLD_IN_GB));

    @Before
    public void setUp() {
        VmHandler.init();
    }

    @Test
    public void testUpdateFieldsNameInStatusUp() {
        VmStatic src = new VmStatic();
        src.setName(RandomUtils.instance().nextString(10));
        src.setInterfaces(new ArrayList<VmNetworkInterface>(2));

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

    @Test
    public void verifyDomainForMemory() {
        Guid sdId = Guid.newGuid();
        List<StorageDomain> storageDomains = createStorageDomains(sdId);
        long vmSpaceInBytes = SizeConverter.convert(VM_SPACE_IN_MB, SizeConverter.SizeUnit.MB, SizeConverter.SizeUnit.BYTES).intValue();
        List<DiskImage> disksList =  MemoryUtils.createDiskDummies(vmSpaceInBytes, META_DATA_SIZE_IN_GB);

        StorageDomain storageDomain = VmHandler.findStorageDomainForMemory(storageDomains, disksList);
        assertThat(storageDomain, notNullValue());
        if (storageDomain != null) {
            Guid selectedId = storageDomain.getId();
            assertThat(selectedId.equals(sdId), is(true));
        }

        mcr.mockConfigValue(ConfigValues.FreeSpaceCriticalLowInGB, THRESHOLD_HIGH_GB);

        storageDomain = VmHandler.findStorageDomainForMemory(storageDomains, disksList);
        assertThat(storageDomain, nullValue());
    }

    private static List<StorageDomain> createStorageDomains(Guid sdIdToBeSelected) {
        StorageDomain sd1 = createStorageDomain(Guid.newGuid(), StorageType.NFS, LOW_SPACE_IN_GB);
        StorageDomain sd2 = createStorageDomain(Guid.newGuid(), StorageType.NFS, LOW_SPACE_IN_GB);
        StorageDomain sd3 = createStorageDomain(sdIdToBeSelected, StorageType.NFS, ENOUGH_SPACE_IN_GB);
        List<StorageDomain> storageDomains = Arrays.asList(sd1, sd2, sd3);
        return storageDomains;
    }

    private static StorageDomain createStorageDomain(Guid guid, StorageType storageType, Integer size) {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(guid);
        storageDomain.setStorageDomainType(StorageDomainType.Data);
        storageDomain.setStorageType(storageType);
        storageDomain.setStatus(StorageDomainStatus.Active);
        storageDomain.setAvailableDiskSize(size);
        return storageDomain;
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
                    disk.getDiskStorageType() == Disk.DiskStorageType.IMAGE ? ((DiskImage)disk).getSnapshotId() : null,
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

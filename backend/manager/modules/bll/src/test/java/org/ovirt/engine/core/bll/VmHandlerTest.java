package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.enterprise.concurrent.ManagedScheduledExecutorService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.storage.domain.IsoDomainListSynchronizer;
import org.ovirt.engine.core.common.businessentities.GuestAgentStatus;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.utils.RandomUtils;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class VmHandlerTest {

    @Mock
    private ManagedScheduledExecutorService executor;

    @Spy
    private IsoDomainListSynchronizer isoDomainListSynchronizer;

    @InjectMocks
    private VmHandler vmHandler = new VmHandler();


    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.WindowsGuestAgentUpdateCheckInternal, 180),
                MockConfigDescriptor.of(ConfigValues.GuestToolsSetupIsoPrefix, ".*rhe?v-toolssetup_"));
    }

    @BeforeEach
    public void setUp() {
        vmHandler.init();
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
        vmHandler.filterImageDisksForVM(vm);
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
        vmHandler.filterImageDisksForVM(vm);
        assertFalse(vm.getDiskList().contains(snapshotDisk));
        assertTrue(vm.getDiskList().contains(regularDisk));
        assertTrue(vm.getManagedVmDeviceMap().containsKey(regularDisk.getId()));
        assertFalse(vm.getManagedVmDeviceMap().containsKey(lunDisk.getId()));
        assertFalse(vm.getManagedVmDeviceMap().containsKey(snapshotDisk.getId()));
    }

    private void populateVmWithDisks(List<Disk> disks, VM vm) {
        vmHandler.updateDisksForVm(vm, disks);
        for (Disk disk : disks) {
            VmDevice device = new VmDevice(new VmDeviceId(disk.getId(), vm.getId()),
                    VmDeviceGeneralType.DISK,
                    VmDeviceType.DISK.getName(),
                    "",
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

    @Test
    public void testInvalidUpdateOfNonEditableFieldOnRunningVm() {
        // Given
        VmStatic src = new VmStatic();
        src.setId(Guid.newGuid());
        VmStatic dest = new VmStatic();
        dest.setId(Guid.newGuid());

        // When
        boolean updateIsValid = vmHandler.isUpdateValid(src, dest);

        // Then
        assertFalse(
                updateIsValid, "Update should be invalid for different IDs");
    }

    @Test
    public void testValidUpdateOfEditableFieldOnDownVm() {
        // Given
        VmStatic src = new VmStatic();
        src.setClusterId(Guid.newGuid());
        VmStatic dest = new VmStatic();
        dest.setClusterId(Guid.newGuid());

        // When
        boolean updateIsValid = vmHandler.isUpdateValid(src, dest, VMStatus.Down, false);

        // Then
        assertTrue(updateIsValid, "Update should be valid for different cluster IDs in the down state");
    }

    @Test
    public void testInValidUpdateOfStatusRestrictedEditableFieldOnRunningVm() {
        // Given
        VmStatic src = new VmStatic();
        src.setMultiQueuesEnabled(true);
        VmStatic dest = new VmStatic();
        dest.setMultiQueuesEnabled(false);

        // When
        boolean updateIsValid = vmHandler.isUpdateValid(src, dest, VMStatus.Up, false);

        // Then
        assertFalse(updateIsValid,
                "Update should be invalid for different single QXL PCI statuses on a running VM");
    }

    @Test
    public void testValidUpdateOfHostedEngineEditableFieldOnRunningVm() {
        // Given
        VmStatic src = new VmStatic();
        src.setOrigin(OriginType.MANAGED_HOSTED_ENGINE);
        src.setDescription(RandomUtils.instance().nextString(10));
        VmStatic dest = new VmStatic();
        dest.setOrigin(OriginType.MANAGED_HOSTED_ENGINE);
        dest.setDescription(RandomUtils.instance().nextString(10));

        // When
        boolean updateIsValid = vmHandler.isUpdateValid(src, dest, VMStatus.Up, false);

        // Then
        assertTrue(updateIsValid,
                "Update should be valid for different descriptions on a running, hosted engine VM");
    }

    @Test
    public void testInvalidUpdateOfHostedEngineNonEditableFieldOnRunningVm() {
        // Given
        VmStatic src = new VmStatic();
        src.setOrigin(OriginType.MANAGED_HOSTED_ENGINE);
        src.setName(RandomUtils.instance().nextString(10));
        VmStatic dest = new VmStatic();
        dest.setOrigin(OriginType.MANAGED_HOSTED_ENGINE);
        dest.setName(RandomUtils.instance().nextString(10));

        // When
        boolean updateIsValid = vmHandler.isUpdateValid(src, dest, VMStatus.Up, false);

        // Then
        assertTrue(updateIsValid,
                "Update should be valid for different names on a running, hosted engine VM");
    }

    @Test
    public void testValidUpdateOfHotSetEditableFieldOnRunningVm() {
        // Given
        int srcNumOfSockets = 2;
        int destNumOfSockets = 4;
        VmStatic src = new VmStatic();
        src.setNumOfSockets(srcNumOfSockets);
        VmStatic dest = new VmStatic();
        dest.setNumOfSockets(destNumOfSockets);

        // When
        boolean updateIsValid = vmHandler.isUpdateValid(src, dest, VMStatus.Up, true);

        // Then
        assertTrue(updateIsValid, "Update should be valid for different number of sockets on a running VM");
    }

    @Test
    public void testGetLatestGuestToolsVersion() {
        Set<String> isoNames = new HashSet<>();
        isoNames.add("Rhv-toolsSetup_4.2_8.iso");
        isoNames.add("Rhev-toolSsetup_4.2_5.iso");

        // When
        String latestVersion = vmHandler.getLatestGuestToolsVersion(isoNames, isoDomainListSynchronizer.getRegexToolPattern());

        // Then
        assertEquals(latestVersion, "4.2.8");
    }

    @Test
    public void testGetLatestGuestVirtioVersion() {
        Set<String> isoNames = new HashSet<>();
        isoNames.add("virtio-win-0.1.185.iso");
        isoNames.add("virtio-win-1.9.12.iso");

        // When
        String latestVersion = vmHandler.getLatestGuestToolsVersion(isoNames, vmHandler.getRegexVirtIoIsoPattern());

        // Then
        assertEquals(latestVersion, "1.9.12");
    }

    @Test
    public void testIsQemuAgentInAppsList() {
        // Given
        String appsWithLinuxAgent = "a-package-1.2.3,qemu-guest-agent-3.2.1,more-packages";
        // When
        GuestAgentStatus linuxAgentStatus = vmHandler.isQemuAgentInAppsList(appsWithLinuxAgent);
        // Then
        assertEquals(GuestAgentStatus.Exists, linuxAgentStatus);

        // Given
        String appsWithWindowsAgent = "Important Application,Another application,QEMU guest agent";
        // When
        GuestAgentStatus windowsAgentStatus = vmHandler.isQemuAgentInAppsList(appsWithWindowsAgent);
        // Then
        assertEquals(GuestAgentStatus.Exists, windowsAgentStatus);

        // Given
        String appsWithNoLinuxAgent = "a-package-1.2.3,no-guest-agent-3.2.1,more-packages";
        // When
        GuestAgentStatus noAgentStatus = vmHandler.isQemuAgentInAppsList(appsWithNoLinuxAgent);
        // Then
        assertEquals(GuestAgentStatus.DoesntExist, noAgentStatus);
    }

}

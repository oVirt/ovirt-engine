package org.ovirt.engine.core.common.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class VmDeviceCommonUtilsTest {

    private static final String NIC_1_NAME = "nic1";
    private static final String NIC_2_NAME = "nic2";

    @Mock
    private VM vm;

    @Test
    public void testUpdateVmDevicesBootOrder() {
        Map<Guid, Disk> idToDisk = new HashMap<>();
        List<VmNetworkInterface> interfaces = new LinkedList<>();

        VmDevice nic1 = createNetworkInterface(true, NIC_1_NAME, interfaces);
        VmDevice unmanagedNic = createUnmanagedNetworkInterface(true);
        VmDevice nic2 = createNetworkInterface(true, NIC_2_NAME, interfaces);
        VmDevice nonBootableNic = createNetworkInterface(false, "", interfaces);

        VmDevice bootableDisk = createDiskDevice(true, idToDisk);
        VmDevice nonBootableDisk = createDiskDevice(false, idToDisk);

        VmDevice cd = createCdRomDevice();

        doReturn(idToDisk).when(vm).getDiskMap();
        doReturn(interfaces).when(vm).getInterfaces();
        doReturn(false).when(vm).isRunOnce();
        doReturn(BootSequence.DNC).when(vm).getDefaultBootSequence();

        // it is important that nic2 will be before nic1 to ensure their boot order is
        // ordered according to their names and not according to their position in the list
        VmDeviceCommonUtils.updateVmDevicesBootOrder(
                vm,
                Arrays.asList(bootableDisk, nic2, cd, nic1, nonBootableDisk, unmanagedNic));

        int index = 1;
        assertEquals("Wrong boot order for CD", index++, cd.getBootOrder());
        assertEquals("Wrong boot order for nic1", index++, nic1.getBootOrder());
        assertEquals("Wrong boot order for nic2", index++, nic2.getBootOrder());
        assertEquals("Wrong boot order for non bootable nic", 0, nonBootableNic.getBootOrder());
        assertEquals("Wrong boot order for unmanaged nic", 0, unmanagedNic.getBootOrder());
        assertEquals("Wrong boot order for bootable disk", index++, bootableDisk.getBootOrder());
        assertEquals("Wrong boot order for non bootable disk", 0, nonBootableDisk.getBootOrder());
    }

    private VmDevice createNetworkInterface(boolean plugged, String name,
            List<VmNetworkInterface> interfaces) {
        Guid id = Guid.newGuid();

        VmNetworkInterface vmNic = new VmNetworkInterface();
        vmNic.setId(id);
        vmNic.setName(name);
        interfaces.add(vmNic);

        VmDevice device = createNetworkInterfaceDevice(plugged, id);
        device.setIsManaged(true);
        return device;
    }

    private VmDevice createUnmanagedNetworkInterface(boolean plugged) {
        VmDevice device = createNetworkInterfaceDevice(plugged, Guid.newGuid());
        device.setIsManaged(false);
        return device;
    }

    private VmDevice createNetworkInterfaceDevice(boolean plugged, Guid id) {
        VmDevice device = new VmDevice();
        device.setType(VmDeviceGeneralType.INTERFACE);
        device.setDevice(VmDeviceType.BRIDGE.getName());
        device.setIsPlugged(plugged);
        device.setId(new VmDeviceId(id, null));
        return device;
    }

    private VmDevice createDiskDevice(boolean boot, Map<Guid, Disk> idTodisk) {
        Guid id = Guid.newGuid();
        VmDevice device = new VmDevice();
        device.setType(VmDeviceGeneralType.DISK);
        device.setDevice(VmDeviceType.DISK.getName());
        device.setId(new VmDeviceId(id, null));

        idTodisk.put(id, createDisk(id, boot));

        return device;
    }

    private Disk createDisk(Guid id, boolean boot) {
        Disk disk = new DiskImageBase();
        disk.setId(id);

        DiskVmElement dve = new DiskVmElement(new VmDeviceId(id, null));
        dve.setBoot(boot);
        disk.setDiskVmElements(Collections.singletonList(dve));

        return disk;
    }

    private VmDevice createCdRomDevice() {
        Guid id = Guid.newGuid();
        VmDevice device = new VmDevice();
        device.setType(VmDeviceGeneralType.DISK);
        device.setDevice(VmDeviceType.CDROM.getName());
        device.setIsPlugged(true);
        device.setId(new VmDeviceId(id, null));

        return device;
    }
}

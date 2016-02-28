package org.ovirt.engine.core.vdsbroker.monitoring;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.FullListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;

@RunWith(MockitoJUnitRunner.class)
public class VmDevicesMonitoringTest {

    @ClassRule
    public static MockEJBStrategyRule ejbRule = new MockEJBStrategyRule();

    private VmDevicesMonitoring vmDevicesMonitoring;

    @Mock
    private VmDynamicDao vmDynamicDao;
    @Mock
    private VmDeviceDao vmDeviceDao;
    @Mock
    private ResourceManager resourceManager;
    @Mock
    private VdsManager vdsManager;

    private static final Guid VDS_ID = new Guid("b7dfe5e6-5667-4e40-8ecb-6d97c8df504d");
    private static final Guid VM_ID = new Guid("7cfc3666-5185-4438-8381-646de77ca9a7");
    private static final Guid VIDEO_DEVICE_ID = new Guid("5987c100-a653-4a6e-87ae-fe1f808225ed");
    private static final Guid CDROM_DEVICE_ID = new Guid("dbf244e9-b91c-4304-a96e-f6868b362443");
    private static final Guid SERIAL_DEVICE_ID = new Guid("77819a89-6910-4c77-a386-b741b69d5d80");
    private static final String VIDEO_DEVICE_ADDRESS = "address1";
    private static final String CDROM_DEVICE_ADDRESS = "address2";
    private static final String SERIAL_DEVICE_ADDRESS = "address3";
    private static final String INITIAL_HASH = "123";
    private static final String NEW_HASH = "012";

    @Before
    public void init() {
        vmDevicesMonitoring = spy(new VmDevicesMonitoring());

        List<Pair<Guid, String>> initialHashes = new ArrayList<>();
        initialHashes.add(new Pair<>(VM_ID, INITIAL_HASH));
        doReturn(initialHashes).when(vmDynamicDao).getAllDevicesHashes();

        doReturn(vdsManager).when(resourceManager).getVdsManager(VDS_ID);

        doReturn(vmDynamicDao).when(vmDevicesMonitoring).getVmDynamicDao();
        doReturn(vmDeviceDao).when(vmDevicesMonitoring).getVmDeviceDao();
        doReturn(resourceManager).when(vmDevicesMonitoring).getResourceManager();
    }

    private static Map<String, Object> getDeviceInfo(Guid id, String deviceType, String device, String address) {
        Map<String, Object> deviceInfo = new HashMap<>();
        if (id != null) {
            deviceInfo.put("deviceId", id.toString());
        }
        deviceInfo.put("deviceType", deviceType);
        deviceInfo.put("device", device);
        deviceInfo.put("type", deviceType);
        deviceInfo.put("alias", device + "0");
        deviceInfo.put("specParams", Collections.EMPTY_MAP);
        if (address != null) {
            deviceInfo.put("address", address);
        }
        return deviceInfo;
    }

    private static VmDevice getVmDevice(Guid deviceId,
            Guid vmId,
            VmDeviceGeneralType type,
            String device,
            boolean isManaged) {
        return new VmDevice(new VmDeviceId(deviceId, vmId),
                type,
                device,
                "",
                0,
                Collections.EMPTY_MAP,
                isManaged,
                true,
                false,
                "",
                Collections.EMPTY_MAP,
                null,
                null);
    }

    private void initDevices(VmDevice... devices) {
        doReturn(Arrays.asList(devices)).when(vmDeviceDao).getVmDeviceByVmId(VM_ID);
        for (VmDevice device : devices) {
            doReturn(Collections.singletonList(device)).when(vmDeviceDao)
                    .getVmDevicesByDeviceId(device.getDeviceId(), device.getVmId());
        }
    }

    private Map<String, Object> getFullList(Guid vmId, Map<String, Object>... deviceInfos) {
        Map<String, Object> vmInfo = new HashMap<>();
        vmInfo.put("vmId", vmId.toString());
        vmInfo.put("devices", deviceInfos);
        return vmInfo;
    }

    private void initFullList(Map<String, Object>... deviceInfos) {
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setReturnValue(new Map[] { getFullList(VM_ID, deviceInfos) });
        returnValue.setSucceeded(true);
        doReturn(returnValue).when(resourceManager).runVdsCommand(eq(VDSCommandType.FullList),
                any(FullListVDSCommandParameters.class));
    }

    @Test
    public void testIgnoreOutdatedHash() {
        initDevices();
        initFullList();

        vmDevicesMonitoring.initDevicesStatuses(3L);

        VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(VDS_ID, 2L);
        change.updateVm(VM_ID, NEW_HASH);
        change.flush();

        verify(resourceManager, never()).runVdsCommand(eq(VDSCommandType.FullList), any());
        verify(vmDeviceDao, never()).updateAllInBatch(any());
        verify(vmDeviceDao, never()).removeAll(any());
        verify(vmDeviceDao, never()).saveAll(any());
        verify(vmDynamicDao, never()).updateDevicesHashes(any());
    }

    @Test
    public void testHashNotChanged() {
        initDevices();
        initFullList();

        vmDevicesMonitoring.initDevicesStatuses(1L);

        VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(VDS_ID, 2L);
        change.updateVm(VM_ID, INITIAL_HASH);
        change.flush();

        verify(resourceManager, never()).runVdsCommand(eq(VDSCommandType.FullList), any());
        verify(vmDeviceDao, never()).updateAllInBatch(any());
        verify(vmDeviceDao, never()).removeAll(any());
        verify(vmDeviceDao, never()).saveAll(any());
        verify(vmDynamicDao, never()).updateDevicesHashes(any());
    }

    @Test
    public void testUpdateVm() {
        final Guid usbControllerId = Guid.newGuid();
        initDevices(
                getVmDevice(usbControllerId, VM_ID, VmDeviceGeneralType.CONTROLLER, "usb", false),
                getVmDevice(VIDEO_DEVICE_ID, VM_ID, VmDeviceGeneralType.VIDEO, "cirrus", true),
                getVmDevice(CDROM_DEVICE_ID, VM_ID, VmDeviceGeneralType.DISK, "cdrom", true)
        );
        initFullList(
                getDeviceInfo(null, "balloon", "memballoon", null),
                getDeviceInfo(null, "controller", "virtio-serial", SERIAL_DEVICE_ADDRESS),
                getDeviceInfo(VIDEO_DEVICE_ID, "video", "cirrus", VIDEO_DEVICE_ADDRESS),
                getDeviceInfo(CDROM_DEVICE_ID, "disk", "cdrom", CDROM_DEVICE_ADDRESS)
        );

        vmDevicesMonitoring.initDevicesStatuses(1L);

        VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(VDS_ID, 2L);
        change.updateVm(VM_ID, NEW_HASH);
        change.flush();

        ArgumentCaptor<Collection> updateCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(vmDeviceDao, times(1)).updateAllInBatch(updateCaptor.capture());
        assertEquals(updateCaptor.getValue().size(), 2);

        ArgumentCaptor<List> removeCaptor = ArgumentCaptor.forClass(List.class);
        verify(vmDeviceDao, times(1)).removeAll(removeCaptor.capture());
        List removedDeviceIds = removeCaptor.getValue();
        assertEquals(removedDeviceIds.size(), 1);
        VmDeviceId deviceId = (VmDeviceId) removedDeviceIds.get(0);
        assertEquals(deviceId.getDeviceId(), usbControllerId);
        assertEquals(deviceId.getVmId(), VM_ID);

        ArgumentCaptor<List> saveCaptor = ArgumentCaptor.forClass(List.class);
        verify(vmDeviceDao, times(1)).saveAll(saveCaptor.capture());
        List savedDevices = saveCaptor.getValue();
        assertEquals(savedDevices.size(), 1);
        VmDevice device = (VmDevice) savedDevices.get(0);
        assertEquals(device.getAddress(), SERIAL_DEVICE_ADDRESS);

        ArgumentCaptor<List> updateHashesCaptor = ArgumentCaptor.forClass(List.class);
        verify(vmDynamicDao, times(1)).updateDevicesHashes(updateHashesCaptor.capture());
        List updatedHashes = updateHashesCaptor.getValue();
        assertEquals(updatedHashes.size(), 1);
        Pair<Guid, String> hashInfo = (Pair<Guid, String>) updatedHashes.get(0);
        assertEquals(hashInfo.getFirst(), VM_ID);
        assertEquals(hashInfo.getSecond(), NEW_HASH);
    }

    @Test
    public void testUpdateVmFromFullList() {
        initDevices();
        initFullList();

        VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(VDS_ID, 1L);
        Map<String, Object> vmInfo = getFullList(VM_ID,
                getDeviceInfo(null, "balloon", "memballoon", null),
                getDeviceInfo(null, "controller", "virtio-serial", SERIAL_DEVICE_ADDRESS),
                getDeviceInfo(VIDEO_DEVICE_ID, "video", "cirrus", VIDEO_DEVICE_ADDRESS),
                getDeviceInfo(CDROM_DEVICE_ID, "disk", "cdrom", CDROM_DEVICE_ADDRESS)
        );
        change.updateVmFromFullList(vmInfo);
        change.flush();

        change = vmDevicesMonitoring.createChange(VDS_ID, 1L);
        change.updateVm(VM_ID, INITIAL_HASH);
        change.flush();

        verify(resourceManager, never()).runVdsCommand(eq(VDSCommandType.FullList), any());
        verify(vmDeviceDao, never()).updateAllInBatch(any());
        verify(vmDeviceDao, never()).removeAll(any());
        verify(vmDeviceDao, times(1)).saveAll(any());

        ArgumentCaptor<List> updateHashesCaptor = ArgumentCaptor.forClass(List.class);
        verify(vmDynamicDao, times(2)).updateDevicesHashes(updateHashesCaptor.capture());
        List<List> values = updateHashesCaptor.getAllValues();

        List updatedHashes = values.get(0);
        assertEquals(updatedHashes.size(), 1);
        Pair<Guid, String> hashInfo = (Pair<Guid, String>) updatedHashes.get(0);
        assertEquals(hashInfo.getFirst(), VM_ID);
        assertEquals(hashInfo.getSecond(), VmDevicesMonitoring.UPDATE_HASH);

        updatedHashes = values.get(1);
        assertEquals(updatedHashes.size(), 1);
        hashInfo = (Pair<Guid, String>) updatedHashes.get(0);
        assertEquals(hashInfo.getFirst(), VM_ID);
        assertEquals(hashInfo.getSecond(), INITIAL_HASH);
    }

    @Test
    public void testAddDevice() {
        initDevices();
        initFullList();

        vmDevicesMonitoring.initDevicesStatuses(1L);

        VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(2L);
        change.updateDevice(getVmDevice(CDROM_DEVICE_ID, VM_ID, VmDeviceGeneralType.DISK, "cdrom", true));
        change.flush();

        verify(resourceManager, never()).runVdsCommand(eq(VDSCommandType.FullList), any());
        verify(vmDeviceDao, never()).updateAllInBatch(any());
        verify(vmDeviceDao, never()).removeAll(any());
        verify(vmDeviceDao, times(1)).saveAll(any());
        verify(vmDynamicDao, never()).updateDevicesHashes(any());
    }

    @Test
    public void testUpdateDevice() {
        VmDevice device = getVmDevice(CDROM_DEVICE_ID, VM_ID, VmDeviceGeneralType.DISK, "cdrom", true);

        initDevices(device);
        initFullList();

        vmDevicesMonitoring.initDevicesStatuses(1L);

        VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(2L);
        change.updateDevice(device);
        change.flush();

        verify(resourceManager, never()).runVdsCommand(eq(VDSCommandType.FullList), any());
        verify(vmDeviceDao, times(1)).updateAllInBatch(any());
        verify(vmDeviceDao, never()).removeAll(any());
        verify(vmDeviceDao, never()).saveAll(any());
        verify(vmDynamicDao, never()).updateDevicesHashes(any());
    }

    @Test
    public void testRemoveDevice() {
        initDevices(
                getVmDevice(CDROM_DEVICE_ID, VM_ID, VmDeviceGeneralType.DISK, "cdrom", true)
        );
        initFullList();

        vmDevicesMonitoring.initDevicesStatuses(1L);

        VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(2L);
        change.removeDevice(new VmDeviceId(CDROM_DEVICE_ID, VM_ID));
        change.flush();

        verify(resourceManager, never()).runVdsCommand(eq(VDSCommandType.FullList), any());
        verify(vmDeviceDao, never()).updateAllInBatch(any());
        verify(vmDeviceDao, times(1)).removeAll(any());
        verify(vmDeviceDao, never()).saveAll(any());
        verify(vmDynamicDao, never()).updateDevicesHashes(any());
    }

    @Test
    public void testAddAndUpdateDevice() {
        initDevices();
        initFullList();

        vmDevicesMonitoring.initDevicesStatuses(1L);

        VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(2L);
        VmDevice device = getVmDevice(CDROM_DEVICE_ID, VM_ID, VmDeviceGeneralType.DISK, "cdrom", true);
        change.updateDevice(device);
        change.flush();

        initDevices(device);

        change = vmDevicesMonitoring.createChange(3L);
        change.updateDevice(device);
        change.flush();

        verify(resourceManager, never()).runVdsCommand(eq(VDSCommandType.FullList), any());
        verify(vmDeviceDao, times(1)).updateAllInBatch(any());
        verify(vmDeviceDao, never()).removeAll(any());
        verify(vmDeviceDao, times(1)).saveAll(any());
        verify(vmDynamicDao, never()).updateDevicesHashes(any());
    }

    @Test
    public void testIgnoreOutdatedUpdateDevice() {
        initDevices();
        initFullList();

        vmDevicesMonitoring.initDevicesStatuses(1L);

        VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(3L);
        VmDevice device = getVmDevice(CDROM_DEVICE_ID, VM_ID, VmDeviceGeneralType.DISK, "cdrom", true);
        change.updateDevice(device);
        change.flush();

        initDevices(device);

        change = vmDevicesMonitoring.createChange(2L);
        change.updateDevice(device);
        change.flush();

        verify(resourceManager, never()).runVdsCommand(eq(VDSCommandType.FullList), any());
        verify(vmDeviceDao, never()).updateAllInBatch(any());
        verify(vmDeviceDao, never()).removeAll(any());
        verify(vmDeviceDao, times(1)).saveAll(any());
        verify(vmDynamicDao, never()).updateDevicesHashes(any());
    }

    @Test
    public void testIgnoreDeviceChangeBeforeFullList() {
        initDevices(
                getVmDevice(CDROM_DEVICE_ID, VM_ID, VmDeviceGeneralType.DISK, "cdrom", true)
        );
        initFullList();

        vmDevicesMonitoring.initDevicesStatuses(2L);

        VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(1L);
        change.removeDevice(new VmDeviceId(CDROM_DEVICE_ID, VM_ID));
        change.flush();

        verify(resourceManager, never()).runVdsCommand(eq(VDSCommandType.FullList), any());
        verify(vmDeviceDao, never()).updateAllInBatch(any());
        verify(vmDeviceDao, never()).removeAll(any());
        verify(vmDeviceDao, never()).saveAll(any());
        verify(vmDynamicDao, never()).updateDevicesHashes(any());
    }

    @Test
    public void testFullListErasesIndividualUpdateDevice() {
        initDevices();
        initFullList();

        vmDevicesMonitoring.initDevicesStatuses(1L);

        VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(4L);
        Guid controllerId = Guid.newGuid();
        VmDevice controller = getVmDevice(controllerId, VM_ID, VmDeviceGeneralType.CONTROLLER, "usb", false);
        change.updateDevice(controller);
        change.flush();

        initDevices(controller);

        change = vmDevicesMonitoring.createChange(VDS_ID, 2L);
        change.updateVm(VM_ID, NEW_HASH);
        change.flush();

        initDevices();
        doReturn(Collections.EMPTY_LIST).when(vmDeviceDao).getVmDeviceByVmId(VM_ID);
        doReturn(Collections.EMPTY_LIST).when(vmDeviceDao).getVmDevicesByDeviceId(controllerId, VM_ID);

        change = vmDevicesMonitoring.createChange(3L);
        change.updateDevice(controller);
        change.flush();

        verify(resourceManager, times(1)).runVdsCommand(eq(VDSCommandType.FullList), any());
        verify(vmDeviceDao, never()).updateAllInBatch(any());
        verify(vmDeviceDao, times(1)).removeAll(any());
        verify(vmDeviceDao, times(2)).saveAll(any());
        verify(vmDynamicDao, times(1)).updateDevicesHashes(any());
    }

}

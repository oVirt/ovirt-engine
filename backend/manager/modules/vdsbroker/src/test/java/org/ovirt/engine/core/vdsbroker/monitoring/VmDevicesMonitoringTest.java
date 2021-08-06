package org.ovirt.engine.core.vdsbroker.monitoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.transaction.TransactionManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.utils.InjectedMock;
import org.ovirt.engine.core.utils.InjectorExtension;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.VmManager;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class, InjectorExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class VmDevicesMonitoringTest {
    @InjectedMock
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    public TransactionManager transactionManager;
    @Mock
    private VmDynamicDao vmDynamicDao;
    @Mock
    private VmDeviceDao vmDeviceDao;
    @Mock
    private VdsManager vdsManager;
    @Mock
    private FullListAdapter fullListAdapter;
    @Mock
    private ResourceManager resourceManager;

    @InjectMocks
    private VmDevicesMonitoring vmDevicesMonitoring;

    private static final Guid VDS_ID = new Guid("b7dfe5e6-5667-4e40-8ecb-6d97c8df504d");
    private static final Guid VM_ID = new Guid("7cfc3666-5185-4438-8381-646de77ca9a7");
    private static final Guid VIDEO_DEVICE_ID = new Guid("5987c100-a653-4a6e-87ae-fe1f808225ed");
    private static final Guid CDROM_DEVICE_ID = new Guid("dbf244e9-b91c-4304-a96e-f6868b362443");
    private static final String VIDEO_DEVICE_ADDRESS = "address1";
    private static final String CDROM_DEVICE_ADDRESS = "address2";
    private static final String SERIAL_DEVICE_ADDRESS = "address3";
    private static final String INITIAL_HASH = "123";
    private static final String NEW_HASH = "012";

    @BeforeEach
    public void init() {
        List<Pair<Guid, String>> initialHashes = new ArrayList<>();
        initialHashes.add(new Pair<>(VM_ID, INITIAL_HASH));
        doReturn(initialHashes).when(vmDynamicDao).getAllDevicesHashes();
        doReturn(Version.getLast()).when(vdsManager).getCompatibilityVersion();
        doReturn(vdsManager).when(fullListAdapter).getVdsManager(any());
        VmManager vmManagerMock = mock(VmManager.class);
        doReturn(new ReentrantLock()).when(vmManagerMock).getVmDevicesLock();
        doReturn(vmManagerMock).when(resourceManager).getVmManager(eq(VM_ID));
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
        deviceInfo.put("specParams", Collections.emptyMap());
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
                Collections.emptyMap(),
                isManaged,
                true,
                false,
                "",
                Collections.emptyMap(),
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

    private Map<String, Object> getDumpXmls(Guid vmId, Map<String, Object>... deviceInfos) {
        Map<String, Object> vmInfo = new HashMap<>();
        vmInfo.put("vmId", vmId.toString());
        vmInfo.put("devices", deviceInfos);
        return vmInfo;
    }

    private void initDumpXmls(Map<String, Object>... deviceInfos) {
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setReturnValue(new Map[] { getDumpXmls(VM_ID, deviceInfos) });
        returnValue.setSucceeded(true);
        doReturn(returnValue).when(fullListAdapter).getVmFullList(any(), any(), anyBoolean());
    }

    @Test
    public void testIgnoreOutdatedHash() {
        initDevices();
        initDumpXmls();

        vmDevicesMonitoring.initDevicesStatuses(3L);

        VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(VDS_ID, 2L);
        change.updateVm(VM_ID, NEW_HASH);
        change.flush();

        verify(fullListAdapter, never()).getVmFullList(any(), any(), anyBoolean());
        verify(vmDeviceDao, never()).updateAllInBatch(any());
        verify(vmDeviceDao, never()).removeAll(any());
        verify(vmDeviceDao, never()).saveAll(any());
        verify(vmDynamicDao, never()).updateDevicesHashes(any());
    }

    @Test
    public void testHashNotChanged() {
        initDevices();
        initDumpXmls();

        vmDevicesMonitoring.initDevicesStatuses(1L);

        VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(VDS_ID, 2L);
        change.updateVm(VM_ID, INITIAL_HASH);
        change.flush();

        verify(fullListAdapter, never()).getVmFullList(any(), any(), anyBoolean());
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
                getVmDevice(VIDEO_DEVICE_ID, VM_ID, VmDeviceGeneralType.VIDEO, "vga", true),
                getVmDevice(CDROM_DEVICE_ID, VM_ID, VmDeviceGeneralType.DISK, "cdrom", true)
        );
        initDumpXmls(
                getDeviceInfo(null, "balloon", "memballoon", null),
                getDeviceInfo(null, "controller", "virtio-serial", SERIAL_DEVICE_ADDRESS),
                getDeviceInfo(VIDEO_DEVICE_ID, "video", "vga", VIDEO_DEVICE_ADDRESS),
                getDeviceInfo(CDROM_DEVICE_ID, "disk", "cdrom", CDROM_DEVICE_ADDRESS)
        );

        vmDevicesMonitoring.initDevicesStatuses(1L);

        VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(VDS_ID, 2L);
        change.updateVm(VM_ID, NEW_HASH);
        change.flush();

        ArgumentCaptor<Collection> updateCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(vmDeviceDao, times(1)).updateAllInBatch(updateCaptor.capture());
        assertEquals(2, updateCaptor.getValue().size());

        ArgumentCaptor<List> removeCaptor = ArgumentCaptor.forClass(List.class);
        verify(vmDeviceDao, times(1)).removeAll(removeCaptor.capture());
        List removedDeviceIds = removeCaptor.getValue();
        assertEquals(1, removedDeviceIds.size());
        VmDeviceId deviceId = (VmDeviceId) removedDeviceIds.get(0);
        assertEquals(deviceId.getDeviceId(), usbControllerId);
        assertEquals(VM_ID, deviceId.getVmId());

        ArgumentCaptor<List> saveCaptor = ArgumentCaptor.forClass(List.class);
        verify(vmDeviceDao, times(1)).saveAll(saveCaptor.capture());
        List savedDevices = saveCaptor.getValue();
        assertEquals(1, savedDevices.size());
        VmDevice device = (VmDevice) savedDevices.get(0);
        assertEquals(SERIAL_DEVICE_ADDRESS, device.getAddress());

        ArgumentCaptor<List> updateHashesCaptor = ArgumentCaptor.forClass(List.class);
        verify(vmDynamicDao, times(1)).updateDevicesHashes(updateHashesCaptor.capture());
        List updatedHashes = updateHashesCaptor.getValue();
        assertEquals(1, updatedHashes.size());
        Pair<Guid, String> hashInfo = (Pair<Guid, String>) updatedHashes.get(0);
        assertEquals(VM_ID, hashInfo.getFirst());
        assertEquals(NEW_HASH, hashInfo.getSecond());
    }

    @Test
    public void testUpdateVmFromFullList() {
        initDevices();
        initDumpXmls();

        VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(VDS_ID, 1L);
        Map<String, Object> vmInfo = getDumpXmls(VM_ID,
                getDeviceInfo(null, "balloon", "memballoon", null),
                getDeviceInfo(null, "controller", "virtio-serial", SERIAL_DEVICE_ADDRESS),
                getDeviceInfo(VIDEO_DEVICE_ID, "video", "vga", VIDEO_DEVICE_ADDRESS),
                getDeviceInfo(CDROM_DEVICE_ID, "disk", "cdrom", CDROM_DEVICE_ADDRESS)
        );
        change.updateVmFromFullList(vmInfo);
        change.flush();

        change = vmDevicesMonitoring.createChange(VDS_ID, 1L);
        change.updateVm(VM_ID, INITIAL_HASH);
        change.flush();

        verify(fullListAdapter, never()).getVmFullList(any(), any(), anyBoolean());
        verify(vmDeviceDao, never()).updateAllInBatch(any());
        verify(vmDeviceDao, never()).removeAll(any());
        verify(vmDeviceDao, times(1)).saveAll(any());

        ArgumentCaptor<List> updateHashesCaptor = ArgumentCaptor.forClass(List.class);
        verify(vmDynamicDao, times(2)).updateDevicesHashes(updateHashesCaptor.capture());
        List<List> values = updateHashesCaptor.getAllValues();

        List updatedHashes = values.get(0);
        assertEquals(1, updatedHashes.size());
        Pair<Guid, String> hashInfo = (Pair<Guid, String>) updatedHashes.get(0);
        assertEquals(VM_ID, hashInfo.getFirst());
        assertEquals(VmDevicesMonitoring.UPDATE_HASH, hashInfo.getSecond());

        updatedHashes = values.get(1);
        assertEquals(1, updatedHashes.size());
        hashInfo = (Pair<Guid, String>) updatedHashes.get(0);
        assertEquals(VM_ID, hashInfo.getFirst());
        assertEquals(INITIAL_HASH, hashInfo.getSecond());
    }

    @Test
    public void testAddDevice() {
        initDevices();
        initDumpXmls();

        vmDevicesMonitoring.initDevicesStatuses(1L);

        VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(2L);
        change.updateDevice(getVmDevice(CDROM_DEVICE_ID, VM_ID, VmDeviceGeneralType.DISK, "cdrom", true));
        change.flush();

        verify(fullListAdapter, never()).getVmFullList(any(), any(), anyBoolean());
        verify(vmDeviceDao, never()).updateAllInBatch(any());
        verify(vmDeviceDao, never()).removeAll(any());
        verify(vmDeviceDao, times(1)).saveAll(any());
        verify(vmDynamicDao, never()).updateDevicesHashes(any());
    }

    @Test
    public void testUpdateDevice() {
        VmDevice device = getVmDevice(CDROM_DEVICE_ID, VM_ID, VmDeviceGeneralType.DISK, "cdrom", true);

        initDevices(device);
        initDumpXmls();

        vmDevicesMonitoring.initDevicesStatuses(1L);

        VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(2L);
        change.updateDevice(device);
        change.flush();

        verify(fullListAdapter, never()).getVmFullList(any(), any(), anyBoolean());
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
        initDumpXmls();

        vmDevicesMonitoring.initDevicesStatuses(1L);

        VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(2L);
        change.removeDevice(new VmDeviceId(CDROM_DEVICE_ID, VM_ID));
        change.flush();

        verify(fullListAdapter, never()).getVmFullList(any(), any(), anyBoolean());
        verify(vmDeviceDao, never()).updateAllInBatch(any());
        verify(vmDeviceDao, times(1)).removeAll(any());
        verify(vmDeviceDao, never()).saveAll(any());
        verify(vmDynamicDao, never()).updateDevicesHashes(any());
    }

    @Test
    public void testAddAndUpdateDevice() {
        initDevices();
        initDumpXmls();

        vmDevicesMonitoring.initDevicesStatuses(1L);

        VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(2L);
        VmDevice device = getVmDevice(CDROM_DEVICE_ID, VM_ID, VmDeviceGeneralType.DISK, "cdrom", true);
        change.updateDevice(device);
        change.flush();

        initDevices(device);

        change = vmDevicesMonitoring.createChange(3L);
        change.updateDevice(device);
        change.flush();

        verify(fullListAdapter, never()).getVmFullList(any(), any(), anyBoolean());
        verify(vmDeviceDao, times(1)).updateAllInBatch(any());
        verify(vmDeviceDao, never()).removeAll(any());
        verify(vmDeviceDao, times(1)).saveAll(any());
        verify(vmDynamicDao, never()).updateDevicesHashes(any());
    }

    @Test
    public void testIgnoreOutdatedUpdateDevice() {
        initDevices();
        initDumpXmls();

        vmDevicesMonitoring.initDevicesStatuses(1L);

        VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(3L);
        VmDevice device = getVmDevice(CDROM_DEVICE_ID, VM_ID, VmDeviceGeneralType.DISK, "cdrom", true);
        change.updateDevice(device);
        change.flush();

        initDevices(device);

        change = vmDevicesMonitoring.createChange(2L);
        change.updateDevice(device);
        change.flush();

        verify(fullListAdapter, never()).getVmFullList(any(), any(), anyBoolean());
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
        initDumpXmls();

        vmDevicesMonitoring.initDevicesStatuses(2L);

        VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(1L);
        change.removeDevice(new VmDeviceId(CDROM_DEVICE_ID, VM_ID));
        change.flush();

        verify(fullListAdapter, never()).getVmFullList(any(), any(), anyBoolean());
        verify(vmDeviceDao, never()).updateAllInBatch(any());
        verify(vmDeviceDao, never()).removeAll(any());
        verify(vmDeviceDao, never()).saveAll(any());
        verify(vmDynamicDao, never()).updateDevicesHashes(any());
    }

    @Test
    public void testFullListErasesIndividualUpdateDevice() {
        initDevices();
        initDumpXmls();

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
        doReturn(Collections.emptyList()).when(vmDeviceDao).getVmDevicesByDeviceId(controllerId, VM_ID);

        change = vmDevicesMonitoring.createChange(3L);
        change.updateDevice(controller);
        change.flush();

        verify(fullListAdapter, times(1)).getVmFullList(any(), any(), anyBoolean());
        verify(vmDeviceDao, never()).updateAllInBatch(any());
        verify(vmDeviceDao, times(1)).removeAll(any());
        verify(vmDeviceDao, times(2)).saveAll(any());
        verify(vmDynamicDao, times(1)).updateDevicesHashes(any());
    }

}

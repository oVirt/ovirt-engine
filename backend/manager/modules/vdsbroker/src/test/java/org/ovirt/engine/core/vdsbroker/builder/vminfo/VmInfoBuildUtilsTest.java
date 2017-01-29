package org.ovirt.engine.core.vdsbroker.builder.vminfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.vdsbroker.vdsbroker.IoTuneUtils.MB_TO_BYTES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.network.NetworkFilter;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.NetworkFilterDao;
import org.ovirt.engine.core.dao.network.NetworkQoSDao;
import org.ovirt.engine.core.dao.network.VmNicFilterParameterDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.qos.StorageQosDao;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

@RunWith(MockitoJUnitRunner.class)
public class VmInfoBuildUtilsTest {

    private static final Guid VNIC_PROFILE_ID = Guid.newGuid();
    private static final Guid VM_NIC_ID = Guid.newGuid();
    private static final Guid NETWORK_FILTER_ID = Guid.newGuid();
    private static final String NETWORK_FILTER_NAME = "clean-traffic";
    private static final Guid NETWORK_FILTER_PARAMETER_0_ID = Guid.newGuid();
    private static final Guid NETWORK_FILTER_PARAMETER_1_ID = Guid.newGuid();
    private static final String NETWORK_FILTER_PARAMETER_0_NAME = "IP";
    private static final String NETWORK_FILTER_PARAMETER_0_VALUE = "10.0.0.1";
    private static final String NETWORK_FILTER_PARAMETER_1_NAME = "IP";
    private static final String NETWORK_FILTER_PARAMETER_1_VALUE = "10.0.0.2";
    private static final Guid VM_ID = Guid.newGuid();
    private static final Guid DISK_IMAGE_ID = Guid.newGuid();
    private static final Guid LUN_DISK_ID = Guid.newGuid();

    @Mock
    private NetworkDao networkDao;
    @Mock
    private NetworkFilterDao networkFilterDao;
    @Mock
    private NetworkClusterDao networkClusterDao;
    @Mock
    private NetworkQoSDao networkQosDao;
    @Mock
    private StorageQosDao storageQosDao;
    @Mock
    private VmDeviceDao vmDeviceDao;
    @Mock
    private VnicProfileDao vnicProfileDao;
    @Mock
    private VmNicFilterParameterDao vmNicFilterParameterDao;

    @InjectMocks
    private VmInfoBuildUtils underTest;

    private StorageQos qos;

    private VmDevice vmDevice;

    private DiskImage diskImage = new DiskImage();

    @Before
    public void setUp() {
        diskImage.setDiskProfileId(Guid.newGuid());

        qos = new StorageQos();
        qos.setId(Guid.newGuid());

        vmDevice = new VmDevice();

        VnicProfile vnicProfile = new VnicProfile();
        vnicProfile.setNetworkFilterId(NETWORK_FILTER_ID);
        when(vnicProfileDao.get(VNIC_PROFILE_ID)).thenReturn(vnicProfile);

        NetworkFilter networkFilter = new NetworkFilter();
        networkFilter.setName(NETWORK_FILTER_NAME);
        when(networkFilterDao.getNetworkFilterById(NETWORK_FILTER_ID)).thenReturn(networkFilter);

        when(vmNicFilterParameterDao.getAllForVmNic(VM_NIC_ID)).thenReturn(createVmNicFilterParameters());
    }

    List<VmNicFilterParameter> createVmNicFilterParameters() {
        List<VmNicFilterParameter> vmNicFilterParameters = new ArrayList();
        vmNicFilterParameters.add(new VmNicFilterParameter(
                NETWORK_FILTER_PARAMETER_0_ID,
                VM_NIC_ID,
                NETWORK_FILTER_PARAMETER_0_NAME,
                NETWORK_FILTER_PARAMETER_0_VALUE
        ));
        vmNicFilterParameters.add(new VmNicFilterParameter(
                NETWORK_FILTER_PARAMETER_1_ID,
                VM_NIC_ID,
                NETWORK_FILTER_PARAMETER_1_NAME,
                NETWORK_FILTER_PARAMETER_1_VALUE
        ));
        return vmNicFilterParameters;
    }

    void assertIoTune(Map<String, Long> ioTune,
                      long totalBytesSec, long readBytesSec, long writeBytesSec,
                      long totalIopsSec, long readIopsSec, long writeIopsSec) {
        assertEquals(ioTune.get(VdsProperties.TotalBytesSec).longValue(), totalBytesSec);
        assertEquals(ioTune.get(VdsProperties.ReadBytesSec).longValue(), readBytesSec);
        assertEquals(ioTune.get(VdsProperties.WriteBytesSec).longValue(), writeBytesSec);

        assertEquals(ioTune.get(VdsProperties.TotalIopsSec).longValue(), totalIopsSec);
        assertEquals(ioTune.get(VdsProperties.ReadIopsSec).longValue(), readIopsSec);
        assertEquals(ioTune.get(VdsProperties.WriteIopsSec).longValue(), writeIopsSec);
    }

    @Test
    public void testHandleIoTune() {
        when(storageQosDao.getQosByDiskProfileId(diskImage.getDiskProfileId())).thenReturn(qos);
        qos.setMaxThroughput(100);
        qos.setMaxIops(10000);

        underTest.handleIoTune(vmDevice, underTest.loadStorageQos(diskImage));

        assertIoTune(getIoTune(vmDevice), 100L * MB_TO_BYTES, 0, 0, 10000, 0, 0);
    }

    @Test
    public void testNoStorageQuotaAssigned() {
        underTest.handleIoTune(vmDevice, underTest.loadStorageQos(diskImage));
        assertNull(vmDevice.getSpecParams());
    }

    @Test
    public void testNoCpuProfileAssigned() {
        diskImage.setDiskProfileId(null);
        underTest.handleIoTune(vmDevice, underTest.loadStorageQos(diskImage));
        assertNull(vmDevice.getSpecParams());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Long> getIoTune(VmDevice vmDevice) {
        return (Map<String, Long>) vmDevice.getSpecParams().get(VdsProperties.Iotune);
    }

    @Test
    public void testAddNetworkFiltersToNic() {
        Map<String, Object> struct = new HashMap<>();
        VmNic vmNic = new VmNic();
        vmNic.setVnicProfileId(VNIC_PROFILE_ID);
        vmNic.setId(VM_NIC_ID);

        underTest.addNetworkFiltersToNic(struct, vmNic);
        List<Map<String, Object>> parametersList =
                (List<Map<String, Object>>) struct.get(VdsProperties.NETWORK_FILTER_PARAMETERS);

        assertNotNull(struct.get(VdsProperties.NW_FILTER));
        assertEquals(struct.get(VdsProperties.NW_FILTER), NETWORK_FILTER_NAME);
        assertNotNull(parametersList);

        assertEquals(2, parametersList.size());
        assertEquals(NETWORK_FILTER_PARAMETER_0_NAME, parametersList.get(0).get("name"));
        assertEquals(NETWORK_FILTER_PARAMETER_0_VALUE, parametersList.get(0).get("value"));
        assertEquals(NETWORK_FILTER_PARAMETER_1_NAME, parametersList.get(1).get("name"));
        assertEquals(NETWORK_FILTER_PARAMETER_1_VALUE, parametersList.get(1).get("value"));
    }

    private Map<Guid, Disk> mockUnsortedDisksMap(VmDevice lunDiskVmDevice, VmDevice diskImageVmDevice) {
        when(vmDeviceDao.get(lunDiskVmDevice.getId())).thenReturn(lunDiskVmDevice);
        when(vmDeviceDao.get(diskImageVmDevice.getId())).thenReturn(diskImageVmDevice);

        DiskVmElement nonBootDiskVmElement = new DiskVmElement(lunDiskVmDevice.getId());
        nonBootDiskVmElement.setBoot(false);
        nonBootDiskVmElement.setDiskInterface(DiskInterface.VirtIO_SCSI);

        DiskVmElement bootDiskVmElement = new DiskVmElement(diskImageVmDevice.getId());
        bootDiskVmElement.setBoot(true);
        bootDiskVmElement.setDiskInterface(DiskInterface.VirtIO_SCSI);

        LunDisk lunDisk = new LunDisk();
        lunDisk.setId(LUN_DISK_ID);
        lunDisk.setDiskVmElements(Collections.singleton(nonBootDiskVmElement));

        DiskImage diskImage = new DiskImage();
        diskImage.setId(DISK_IMAGE_ID);
        diskImage.setDiskVmElements(Collections.singleton(bootDiskVmElement));

        Map<Guid, Disk> map = new HashMap<>();
        map.put(lunDisk.getId(), lunDisk);
        map.put(diskImage.getId(), diskImage);

        return map;
    }

    @Test
    public void testGetVmDeviceUnitMapForScsiDisks() {
        VmDevice lunDiskVmDevice = new VmDevice(new VmDeviceId(LUN_DISK_ID, VM_ID),
                VmDeviceGeneralType.DISK,
                VmDeviceType.DISK.getName(),
                "",
                null,
                true,
                true,
                null,
                "",
                null,
                null,
                null);

        VmDevice diskImageVmDevice = new VmDevice(new VmDeviceId(DISK_IMAGE_ID, VM_ID),
                VmDeviceGeneralType.DISK,
                VmDeviceType.DISK.getName(),
                "",
                null,
                true,
                true,
                null,
                "",
                null,
                null,
                null);

        VM vm = new VM();
        vm.setId(VM_ID);
        vm.setDiskMap(mockUnsortedDisksMap(lunDiskVmDevice, diskImageVmDevice));

        Map<VmDevice, Integer> vmDeviceUnitMap =
                underTest.getVmDeviceUnitMapForScsiDisks(vm, DiskInterface.VirtIO_SCSI, false);

        // Ensures that the boot disk unit is lower
        assertEquals(vmDeviceUnitMap.get(lunDiskVmDevice), (Integer) 1);
        assertEquals(vmDeviceUnitMap.get(diskImageVmDevice), (Integer) 0);
    }

}

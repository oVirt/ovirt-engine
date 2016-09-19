package org.ovirt.engine.core.vdsbroker.builder.vminfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.vdsbroker.vdsbroker.IoTuneUtils.MB_TO_BYTES;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.NetworkFilterDao;
import org.ovirt.engine.core.dao.network.NetworkQoSDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.qos.StorageQosDao;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

@RunWith(MockitoJUnitRunner.class)
public class VmInfoBuildUtilsTest {

    @Mock
    private NetworkDao networkDao;
    @Mock
    private NetworkFilterDao networkFilterDao;
    @Mock
    private NetworkQoSDao networkQosDao;
    @Mock
    private StorageQosDao storageQosDao;
    @Mock
    private VmDeviceDao vmDeviceDao;
    @Mock
    private VnicProfileDao vnicProfileDao;

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

}

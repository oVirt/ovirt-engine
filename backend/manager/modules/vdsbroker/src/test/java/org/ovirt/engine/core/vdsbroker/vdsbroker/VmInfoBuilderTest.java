package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.vdsbroker.vdsbroker.IoTuneUtils.MB_TO_BYTES;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.qos.StorageQosDao;

@RunWith(MockitoJUnitRunner.class)
public class VmInfoBuilderTest {

    @Mock
    private DbFacade dbFacade;

    @Mock
    private StorageQosDao storageQosDao;

    private StorageQos qos;

    private VM vm;

    private VmDevice vmDevice;

    private DiskImage diskImage = new DiskImage();

    @Before
    public void setUp() {
        diskImage.setDiskProfileId(Guid.newGuid());
        SimpleDependencyInjector.getInstance().bind(DbFacade.class, dbFacade);
        DbFacade.setInstance(dbFacade);
        when(dbFacade.getStorageQosDao()).thenReturn(storageQosDao);

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

        VmInfoBuilder.handleIoTune(vmDevice, VmInfoBuilder.loadStorageQos(diskImage));

        assertIoTune(getIoTune(vmDevice), 100L * MB_TO_BYTES, 0, 0, 10000, 0, 0);
    }

    @Test
    public void testNoStorageQuotaAssigned() {
        when(storageQosDao.getQosByDiskProfileId(diskImage.getDiskProfileId())).thenReturn(null);
        VmInfoBuilder.handleIoTune(vmDevice, VmInfoBuilder.loadStorageQos(diskImage));
        assertNull(vmDevice.getSpecParams());
    }

    @Test
    public void testNoCpuProfileAssigned() {
        diskImage.setDiskProfileId(null);
        VmInfoBuilder.handleIoTune(vmDevice, VmInfoBuilder.loadStorageQos(diskImage));
        assertNull(vmDevice.getSpecParams());
    }

    private Map<String, Long> getIoTune(VmDevice vmDevice) {
        return (Map<String, Long>) vmDevice.getSpecParams().get(VdsProperties.Iotune);
    }

}

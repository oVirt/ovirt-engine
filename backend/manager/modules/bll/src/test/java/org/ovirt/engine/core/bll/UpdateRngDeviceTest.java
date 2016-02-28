package org.ovirt.engine.core.bll;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.ovirt.engine.core.common.action.RngDeviceParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmStaticDao;

public class UpdateRngDeviceTest extends BaseCommandTest {

    @Test
    public void testValidate() {
        UpdateRngDeviceCommand command = mockCommand();
        Assert.assertEquals(true, command.validate());
    }

    private UpdateRngDeviceCommand mockCommand() {
        final Guid vmId = new Guid("a09f57b1-5739-4352-bf88-a6f834ed46db");
        final Guid clusterId = new Guid("e862dae0-5c41-416a-922c-5395e7245c9b");
        final Guid deviceId = new Guid("b24ae590-f42b-49b6-b8f4-cbbc720b230d");
        final VmRngDevice dev = getDevice(deviceId, vmId);

        final VmStatic vmMock = Mockito.mock(VmStatic.class);
        Mockito.when(vmMock.getClusterId()).thenReturn(clusterId);
        final VmStaticDao vmDaoMock = Mockito.mock(VmStaticDao.class);
        Mockito.when(vmDaoMock.get(vmId)).thenReturn(vmMock);
        final VmDeviceDao vmDeviceDaoMock = Mockito.mock(VmDeviceDao.class);
        Mockito.when(vmDeviceDaoMock.getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.RNG)).thenReturn(Collections.singletonList(new VmDevice()));
        final Cluster cluster = Mockito.mock(Cluster.class);
        Mockito.when(cluster.getRequiredRngSources()).thenReturn(Collections.singleton(VmRngDevice.Source.RANDOM));
        final ClusterDao clusterMock = Mockito.mock(ClusterDao.class);
        Mockito.when(clusterMock.get(clusterId)).thenReturn(cluster);

        RngDeviceParameters params = new RngDeviceParameters(dev, true);
        UpdateRngDeviceCommand cmd = new UpdateRngDeviceCommand(params, null) {
            @Override
            public VmStaticDao getVmStaticDao() {
                return vmDaoMock;
            }

            @Override
            public ClusterDao getClusterDao() {
                return clusterMock;
            }

            @Override
            public VmDeviceDao getVmDeviceDao() {
                return vmDeviceDaoMock;
            }

            @Override
            public Guid getClusterId() {
                return clusterId;
            }
        };

        return cmd;
    }

    private VmRngDevice getDevice(Guid deviceId, Guid vmId) {
        VmRngDevice device = new VmRngDevice();
        device.setVmId(vmId);
        device.setDeviceId(deviceId);
        device.setBytes(12);
        device.setPeriod(34);
        device.setSource(VmRngDevice.Source.RANDOM);
        return device;
    }

}

package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
    private static final Guid vmId = new Guid("a09f57b1-5739-4352-bf88-a6f834ed46db");
    private static final Guid clusterId = new Guid("e862dae0-5c41-416a-922c-5395e7245c9b");
    private static final Guid deviceId = new Guid("b24ae590-f42b-49b6-b8f4-cbbc720b230d");

    @Mock
    private VmStaticDao vmDaoMock;

    @Mock
    private VmDeviceDao vmDeviceDaoMock;

    @Mock
    private ClusterDao clusterMock;

    @InjectMocks
    private UpdateRngDeviceCommand cmd =
            new UpdateRngDeviceCommand(new RngDeviceParameters(getDevice(deviceId, vmId), true), null);

    @Test
    public void testValidate() {
        UpdateRngDeviceCommand command = mockCommand();
        assertTrue(command.validate());
    }

    private UpdateRngDeviceCommand mockCommand() {
        final VmStatic vmMock = mock(VmStatic.class);
        when(vmMock.getClusterId()).thenReturn(clusterId);
        when(vmDaoMock.get(vmId)).thenReturn(vmMock);
        when(vmDeviceDaoMock.getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.RNG)).thenReturn(Collections.singletonList(new VmDevice()));
        final Cluster cluster = mock(Cluster.class);
        when(cluster.getRequiredRngSources()).thenReturn(Collections.singleton(VmRngDevice.Source.RANDOM));
        when(clusterMock.get(clusterId)).thenReturn(cluster);

        cmd.init();

        return cmd;
    }

    private static VmRngDevice getDevice(Guid deviceId, Guid vmId) {
        VmRngDevice device = new VmRngDevice();
        device.setVmId(vmId);
        device.setDeviceId(deviceId);
        device.setBytes(12);
        device.setPeriod(34);
        device.setSource(VmRngDevice.Source.RANDOM);
        return device;
    }

}

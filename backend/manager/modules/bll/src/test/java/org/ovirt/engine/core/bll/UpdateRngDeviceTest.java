package org.ovirt.engine.core.bll;

import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.ovirt.engine.core.bll.validator.VirtIoRngValidator;
import org.ovirt.engine.core.common.action.RngDeviceParameters;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VdsGroupDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmStaticDao;

public class UpdateRngDeviceTest {

    @Test
    public void testCanDoAction() {
        UpdateRngDeviceCommand command = mockCommand(Version.v3_5);
        Assert.assertEquals(true, command.canDoAction());
    }

    private UpdateRngDeviceCommand mockCommand(final Version mockCompatibilityVersion) {
        final Guid vmId = new Guid("a09f57b1-5739-4352-bf88-a6f834ed46db");
        final Guid clusterId = new Guid("e862dae0-5c41-416a-922c-5395e7245c9b");
        final Guid deviceId = new Guid("b24ae590-f42b-49b6-b8f4-cbbc720b230d");
        final VmRngDevice dev = getDevice(deviceId, vmId);

        final VmStatic vmMock = Mockito.mock(VmStatic.class);
        Mockito.when(vmMock.getVdsGroupId()).thenReturn(clusterId);
        final VmStaticDao vmDaoMock = Mockito.mock(VmStaticDao.class);
        Mockito.when(vmDaoMock.get(vmId)).thenReturn(vmMock);
        final VmDeviceDao vmDeviceDaoMock = Mockito.mock(VmDeviceDao.class);
        Mockito.when(vmDeviceDaoMock.getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.RNG)).thenReturn(Collections.singletonList(new VmDevice()));
        final VDSGroup cluster = Mockito.mock(VDSGroup.class);
        Mockito.when(cluster.getCompatibilityVersion()).thenReturn(mockCompatibilityVersion);
        Mockito.when(cluster.getRequiredRngSources()).thenReturn(Collections.singleton(VmRngDevice.Source.RANDOM));
        final VdsGroupDao vdsGroupMock = Mockito.mock(VdsGroupDao.class);
        Mockito.when(vdsGroupMock.get(clusterId)).thenReturn(cluster);

        RngDeviceParameters params = new RngDeviceParameters(dev, true);
        UpdateRngDeviceCommand cmd = new UpdateRngDeviceCommand(params) {
            @Override
            public VmStaticDao getVmStaticDao() {
                return vmDaoMock;
            }

            @Override
            public VdsGroupDao getVdsGroupDao() {
                return vdsGroupMock;
            }

            @Override
            protected VmDeviceDao getVmDeviceDao() {
                return vmDeviceDaoMock;
            }

            @Override
            public Guid getVdsGroupId() {
                return clusterId;
            }

            @Override
            protected VirtIoRngValidator getVirtioRngValidator() {
                return new VirtIoRngValidator() {
                    @Override
                    protected boolean isFeatureSupported(Version clusterVersion) {
                        return mockCompatibilityVersion.compareTo(Version.v3_5) >= 0;
                    }
                };
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

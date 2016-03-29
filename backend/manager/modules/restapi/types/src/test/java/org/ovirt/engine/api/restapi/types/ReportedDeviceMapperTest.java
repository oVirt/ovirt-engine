package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Ip;
import org.ovirt.engine.api.model.IpVersion;
import org.ovirt.engine.api.model.ReportedDevice;
import org.ovirt.engine.api.model.ReportedDeviceType;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;

public class ReportedDeviceMapperTest extends AbstractInvertibleMappingTest<ReportedDevice, VmGuestAgentInterface, VmGuestAgentInterface> {

    public ReportedDeviceMapperTest() {
        super(ReportedDevice.class, VmGuestAgentInterface.class, VmGuestAgentInterface.class);
    }

    @Override
    protected ReportedDevice postPopulate(ReportedDevice model) {
        model.setType(MappingTestHelper.shuffle(ReportedDeviceType.class));
        for (Ip ip : model.getIps().getIps()) {
            ip.setVersion(MappingTestHelper.shuffle(IpVersion.class));
            ip.setGateway(null);
            ip.setNetmask(null);
        }
        return model;
    }

    @Override
    protected void verify(ReportedDevice model, ReportedDevice transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertNotNull(transform.getVm());
        assertEquals(model.getVm().getId(), transform.getVm().getId());
        assertNotNull(transform.getMac());
        assertEquals(model.getMac().getAddress(), transform.getMac().getAddress());
        assertNotNull(transform.getType());
        assertEquals(model.getType(), transform.getType());
        assertNotNull(transform.getIps());
        assertTrue(transform.getIps().isSetIps());
        assertEquals(transform.getIps().getIps().size(), model.getIps().getIps().size());
        for (int i = 0; i < transform.getIps().getIps().size(); i++) {
            assertEquals(transform.getIps().getIps().get(i).getAddress(), model.getIps().getIps().get(i).getAddress());
            assertEquals(transform.getIps().getIps().get(i).getVersion(), model.getIps().getIps().get(i).getVersion());
        }
    }
}

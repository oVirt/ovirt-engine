package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.IP;
import org.ovirt.engine.api.model.ReportedDevice;
import org.ovirt.engine.api.model.ReportedDeviceType;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;

public class ReportedDeviceMapperTest extends AbstractInvertibleMappingTest<ReportedDevice, VmGuestAgentInterface, VmGuestAgentInterface> {

    public ReportedDeviceMapperTest() {
        super(ReportedDevice.class, VmGuestAgentInterface.class, VmGuestAgentInterface.class);
    }

    @Override
    protected ReportedDevice postPopulate(ReportedDevice model) {
        model.setType(MappingTestHelper.shuffle(ReportedDeviceType.class).value());
        for (IP ip : model.getIps().getIPs()) {
            ip.setVersion(MappingTestHelper.shuffle(IpVersion.class).value());
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
        assertTrue(transform.getIps().isSetIPs());
        assertEquals(transform.getIps().getIPs().size(), model.getIps().getIPs().size());
        for (int i = 0; i < transform.getIps().getIPs().size(); i++) {
            assertEquals(transform.getIps().getIPs().get(i).getAddress(), model.getIps().getIPs().get(i).getAddress());
            assertEquals(transform.getIps().getIPs().get(i).getVersion(), model.getIps().getIPs().get(i).getVersion());
        }
    }
}

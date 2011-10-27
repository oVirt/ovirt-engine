package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.NicInterface;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;

public class NicMapperTest extends AbstractInvertibleMappingTest<NIC, VmNetworkInterface, VmNetworkInterface> {

    protected NicMapperTest() {
        super(NIC.class, VmNetworkInterface.class, VmNetworkInterface.class);
    }

    @Override
    protected NIC postPopulate(NIC model) {
        model.setInterface(MappingTestHelper.shuffle(NicInterface.class).value());
        return model;
    }

    @Override
    protected void verify(NIC model, NIC transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertNotNull(transform.getVm());
        assertEquals(model.getVm().getId(), transform.getVm().getId());
        assertNotNull(transform.getNetwork());
        assertEquals(model.getNetwork().getName(), transform.getNetwork().getName());
        assertNotNull(transform.getMac());
        assertEquals(model.getMac().getAddress(), transform.getMac().getAddress());
        assertEquals(model.getInterface(), transform.getInterface());
    }
}

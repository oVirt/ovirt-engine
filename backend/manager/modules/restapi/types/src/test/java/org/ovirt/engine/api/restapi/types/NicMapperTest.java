package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.NicInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;

public class NicMapperTest extends AbstractInvertibleMappingTest<Nic, VmNetworkInterface, VmNetworkInterface> {

    public NicMapperTest() {
        super(Nic.class, VmNetworkInterface.class, VmNetworkInterface.class);
    }

    @Override
    protected Nic postPopulate(Nic model) {
        model.setInterface(MappingTestHelper.shuffle(NicInterface.class));
        return model;
    }

    @Override
    protected void verify(Nic model, Nic transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertNotNull(transform.getVm());
        assertEquals(model.getVm().getId(), transform.getVm().getId());
        assertNotNull(transform.getMac());
        assertEquals(model.getMac().getAddress(), transform.getMac().getAddress());
        assertEquals(model.getInterface(), transform.getInterface());
        assertEquals(model.isLinked(), transform.isLinked());
        assertEquals(model.isPlugged(), transform.isPlugged());
        assertNotNull(transform.getVnicProfile());
        assertEquals(model.getVnicProfile().getId(), transform.getVnicProfile().getId());
    }
}

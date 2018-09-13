package org.ovirt.engine.api.restapi.types;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.hamcrest.text.IsEqualIgnoringCase;
import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.NicInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;

public class NicMapperTest extends AbstractInvertibleMappingTest<Nic, VmNetworkInterface, VmNetworkInterface> {

    public NicMapperTest() {
        super(Nic.class, VmNetworkInterface.class, VmNetworkInterface.class);
    }

    @Override
    protected Nic postPopulate(Nic model) {
        model.setInterface(NicInterface.E1000);
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
        assertThat(model.getMac().getAddress(), IsEqualIgnoringCase.equalToIgnoringCase(transform.getMac().getAddress()));
        assertEquals(model.getInterface(), transform.getInterface());
        assertEquals(model.isLinked(), transform.isLinked());
        assertEquals(model.isPlugged(), transform.isPlugged());
        assertNotNull(transform.getVnicProfile());
        assertEquals(model.getVnicProfile().getId(), transform.getVnicProfile().getId());
    }
}

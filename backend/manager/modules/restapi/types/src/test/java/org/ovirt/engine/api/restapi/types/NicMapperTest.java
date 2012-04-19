package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.api.model.NicInterface;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;

public class NicMapperTest extends AbstractInvertibleMappingTest<NIC, VmNetworkInterface, VmNetworkInterface> {

    protected NicMapperTest() {
        super(NIC.class, VmNetworkInterface.class, VmNetworkInterface.class);
    }

    @Override
    protected NIC postPopulate(NIC model) {
        model.setInterface(MappingTestHelper.shuffle(NicInterface.class).value());
        Network network = new Network();
        network.setName("rhel");
        Networks networks = new Networks();
        Network net = new Network();
        net.setName("rhel");
        model.getPortMirroring().setNetworks(networks);
        model.getPortMirroring().getNetworks().getNetworks().add(net);
        model.setNetwork(network);

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

        assertEquals(model.isSetPortMirroring(), transform.isSetPortMirroring());
        if (model.isSetPortMirroring()) {
            assertEquals(model.getPortMirroring().isSetNetworks(), transform.getPortMirroring().isSetNetworks());
            if (model.getPortMirroring().isSetNetworks()) {
                assertEquals(model.getPortMirroring().getNetworks().isSetNetworks(),
                            transform.getPortMirroring().getNetworks().isSetNetworks());
                if (transform.getPortMirroring().getNetworks().isSetNetworks()) {
                    assertEquals(model.getPortMirroring().getNetworks().getNetworks().size(),
                                transform.getPortMirroring().getNetworks().getNetworks().size());
                    assertEquals(model.getPortMirroring().getNetworks().getNetworks().get(0).getName(),
                                transform.getPortMirroring().getNetworks().getNetworks().get(0).getName());
                }
            }
        }
    }
}

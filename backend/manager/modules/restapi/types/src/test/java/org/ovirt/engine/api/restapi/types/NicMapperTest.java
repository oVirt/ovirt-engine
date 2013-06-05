package org.ovirt.engine.api.restapi.types;

import org.junit.Test;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.api.model.NicInterface;
import org.ovirt.engine.api.model.PortMirroring;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public class NicMapperTest extends AbstractInvertibleMappingTest<NIC, VmNetworkInterface, VmNetworkInterface> {

    public NicMapperTest() {
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
        assertEquals(model.isLinked(), transform.isLinked());
        assertEquals(model.isPlugged(), transform.isPlugged());
        assertEquals(CustomPropertiesParser.parse(model.getCustomProperties().getCustomProperty()), CustomPropertiesParser.parse(transform.getCustomProperties().getCustomProperty()));
    }

    @Test
    public void testPortMirorringMapping() {
        NIC nic = new NIC();
        String netId = Guid.newGuid().toString();
        Network network = new Network();
        network.setId(netId);
        Networks networks = new Networks();
        Network net = new Network();
        network.setId(netId);
        nic.setPortMirroring(new PortMirroring());
        nic.getPortMirroring().setNetworks(networks);
        nic.getPortMirroring().getNetworks().getNetworks().add(net);
        nic.setNetwork(network);
        VmNetworkInterface entity = NicMapper.map(nic, null);
        assertTrue(entity.isPortMirroring());

        nic.getPortMirroring().setNetworks(null);
        entity = NicMapper.map(nic, null);
        assertFalse(entity.isPortMirroring());
    }
}

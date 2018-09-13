package org.ovirt.engine.api.restapi.types;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hamcrest.text.IsEqualIgnoringCase;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.Bonding;
import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.HostNic;
import org.ovirt.engine.api.model.HostNics;
import org.ovirt.engine.api.model.Qos;
import org.ovirt.engine.api.model.QosType;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.RandomUtils;

public class HostNicMapperTest extends AbstractInvertibleMappingTest<HostNic, VdsNetworkInterface, VdsNetworkInterface> {

    public HostNicMapperTest() {
        super(HostNic.class, VdsNetworkInterface.class, VdsNetworkInterface.class);
    }

    @Override
    protected void verify(HostNic model, HostNic transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertNotNull(transform.getNetwork());
        assertEquals(model.getNetwork().getName(), transform.getNetwork().getName());
        assertNotNull(transform.getIp());
        assertEquals(model.getIp().getAddress(), transform.getIp().getAddress());
        assertEquals(model.getIp().getNetmask(), transform.getIp().getNetmask());
        assertEquals(model.getIp().getGateway(), transform.getIp().getGateway());
        assertNotNull(transform.getMac());
        assertThat(model.getMac().getAddress(), IsEqualIgnoringCase.equalToIgnoringCase(transform.getMac().getAddress()));
        assertNotNull(model.getBonding());
        assertEquals(model.getBonding().getOptions().getOptions().size(), transform.getBonding()
                .getOptions()
                .getOptions()
                .size());
        for (int i = 0; i < model.getBonding().getOptions().getOptions().size(); i++) {
            assertEquals(model.getBonding().getOptions().getOptions().get(i).getName(), transform.getBonding()
                    .getOptions()
                    .getOptions()
                    .get(i)
                    .getName());
            assertEquals(model.getBonding().getOptions().getOptions().get(i).getValue(), transform.getBonding()
                    .getOptions()
                    .getOptions()
                    .get(i)
                    .getValue());
        }

        assertNotNull(model.getProperties());
    }

    @Test
    public void testCustomNetworkConfigurationMapped() {
        VdsNetworkInterface entity = new VdsNetworkInterface();
        HostNic model = HostNicMapper.map(entity, null);
        assertFalse(model.isSetCustomConfiguration());

        entity.setNetworkImplementationDetails(new VdsNetworkInterface.NetworkImplementationDetails(false, true));
        model = HostNicMapper.map(entity, null);
        assertEquals(entity.getNetworkImplementationDetails().isInSync(), !model.isCustomConfiguration());

        entity.setNetworkImplementationDetails(new VdsNetworkInterface.NetworkImplementationDetails(true, true));
        model = HostNicMapper.map(entity, null);
        assertEquals(entity.getNetworkImplementationDetails().isInSync(), !model.isCustomConfiguration());
    }

    @Test
    public void testBondMapping() {
        HostNic model = new HostNic();
        model.setId(Guid.newGuid().toString());
        model.setName(RandomUtils.instance().nextString(10));
        model.setBonding(new Bonding());
        model.getBonding().setSlaves(new HostNics());
        HostNic slaveA = new HostNic();
        slaveA.setName(RandomUtils.instance().nextString(10));
        model.getBonding().getSlaves().getHostNics().add(slaveA);
        Bond entity = HostNicMapper.map(model, null);
        assertNotNull(entity);
        assertEquals(model.getId(), entity.getId().toString());
        assertEquals(model.getName(), entity.getName());
        assertEquals(model.getBonding().getSlaves().getHostNics().size(), entity.getSlaves().size());
        for (HostNic slave : model.getBonding().getSlaves().getHostNics()) {
            assertTrue(entity.getSlaves().contains(slave.getName()));
        }
    }

    @Override
    protected HostNic postPopulate(HostNic model) {
        HostNic hostNIC = super.postPopulate(model);
        Qos qos = hostNIC.getQos();
        qos.setType(QosType.HOSTNETWORK);
        qos.setName(null);
        qos.setDataCenter(null);

        model.setBootProtocol(BootProtocol.STATIC);

        return hostNIC;
    }
}

package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.NetworkStatus;

public class NetworkMapperTest extends AbstractInvertibleMappingTest<Network, org.ovirt.engine.core.common.businessentities.network.Network, org.ovirt.engine.core.common.businessentities.network.Network> {

    public NetworkMapperTest() {
        super(Network.class,
                org.ovirt.engine.core.common.businessentities.network.Network.class,
                org.ovirt.engine.core.common.businessentities.network.Network.class);
    }

    @Override
    protected void verify(Network model, Network transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getDescription(), transform.getDescription());
        assertEquals(model.getComment(), transform.getComment());
        assertNotNull(transform.getDataCenter());
        assertEquals(model.getDataCenter().getId(), transform.getDataCenter().getId());
        assertNotNull(transform.getIp());
        assertEquals(model.getIp().getAddress(), transform.getIp().getAddress());
        assertEquals(model.getIp().getNetmask(), transform.getIp().getNetmask());
        assertEquals(model.getIp().getGateway(), transform.getIp().getGateway());
        assertNotNull(transform.getVlan());
        assertEquals(model.getVlan().getId(), transform.getVlan().getId());
        assertEquals(model.isStp(), transform.isStp());
    }

    @Override
    protected Network postPopulate(Network model) {
        model.setStatus(StatusUtils.create(MappingTestHelper.shuffle(NetworkStatus.class)));
        return super.postPopulate(model);
    }
}

package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.NetworkStatus;
import org.ovirt.engine.api.model.NetworkUsage;

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
        final Set<NetworkUsage> usagesSet = new HashSet<>(model.getUsages().getUsages());
        assertTrue(usagesSet.contains(NetworkUsage.DISPLAY));
        assertTrue(usagesSet.contains(NetworkUsage.MIGRATION));
        assertTrue(usagesSet.contains(NetworkUsage.MANAGEMENT));
        assertTrue(usagesSet.contains(NetworkUsage.VM));
        assertTrue(usagesSet.contains(NetworkUsage.DEFAULT_ROUTE));
    }

    @Override
    protected Network postPopulate(Network model) {
        model.setStatus(NetworkStatus.NON_OPERATIONAL);
        model.setUsages(new Network.UsagesList());
        model.getUsages().getUsages().add(NetworkUsage.DISPLAY);
        model.getUsages().getUsages().add(NetworkUsage.MIGRATION);
        model.getUsages().getUsages().add(NetworkUsage.MANAGEMENT);
        model.getUsages().getUsages().add(NetworkUsage.VM);
        model.getUsages().getUsages().add(NetworkUsage.DEFAULT_ROUTE);
        return super.postPopulate(model);
    }
}

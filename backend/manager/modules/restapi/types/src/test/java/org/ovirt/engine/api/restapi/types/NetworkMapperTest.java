package org.ovirt.engine.api.restapi.types;

import java.util.HashSet;
import java.util.Set;

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
        final Set<String> usagesSet = new HashSet<>(model.getUsages().getUsages());
        assertTrue(usagesSet.contains(NetworkUsage.DISPLAY.value()));
        assertTrue(usagesSet.contains(NetworkUsage.MIGRATION.value()));
        assertTrue(usagesSet.contains(NetworkUsage.MANAGEMENT.value()));
        assertTrue(usagesSet.contains(NetworkUsage.VM.value()));
    }

    @Override
    protected Network postPopulate(Network model) {
        model.setStatus(MappingTestHelper.shuffle(NetworkStatus.class));
        model.setUsages(new Network.UsagesList());
        model.getUsages().getUsages().add("aaa");
        model.getUsages().getUsages().add(NetworkUsage.DISPLAY.value());
        model.getUsages().getUsages().add(NetworkUsage.MIGRATION.value());
        model.getUsages().getUsages().add(NetworkUsage.MANAGEMENT.value());
        model.getUsages().getUsages().add(NetworkUsage.VM.value());
        return super.postPopulate(model);
    }
}

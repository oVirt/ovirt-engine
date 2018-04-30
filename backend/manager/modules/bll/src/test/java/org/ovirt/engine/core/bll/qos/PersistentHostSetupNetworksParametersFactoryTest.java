package org.ovirt.engine.core.bll.qos;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.action.PersistentHostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.network.AnonymousHostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;

@ExtendWith(MockitoExtension.class)
public class PersistentHostSetupNetworksParametersFactoryTest {

    @Mock
    private NetworkAttachmentDao networkAttachmentDao;

    @InjectMocks
    private PersistentHostSetupNetworksParametersFactory underTest;

    private Guid hostId = Guid.newGuid();
    private Network networkA = createNetwork("NetworkA");
    private Network networkB = createNetwork("NetworkB");
    private NetworkAttachment networkAttachmentA = createNetworkAttachment(networkA, new HostNetworkQos());
    private NetworkAttachment networkAttachmentB = createNetworkAttachment(networkB, null);

    @Test
    public void testCreate() {
        when(networkAttachmentDao.getAllForHost(hostId))
                .thenReturn(Arrays.asList(networkAttachmentA, networkAttachmentB));

        PersistentHostSetupNetworksParameters parameters =
                underTest.create(hostId, Arrays.asList(networkA, networkB));

        assertThat(parameters.getShouldBeLogged(), is(false));
        assertThat(parameters.rollbackOnFailure(), is(true));
        String expectedSubstitutedNetworkNames = networkB.getName();
        assertThat(parameters.getNetworkNames(), is(expectedSubstitutedNetworkNames));
        assertThat(parameters.getNetworkAttachments(), hasSize(1));
        NetworkAttachment soleAttachment = parameters.getNetworkAttachments().iterator().next();
        assertThat(soleAttachment.getId(), is(networkAttachmentB.getId()));
        assertThat(soleAttachment.isOverrideConfiguration(), is(true));
    }

    private Network createNetwork(String name) {
        Network network = new Network();
        network.setId(Guid.newGuid());
        network.setName(name);
        return network;
    }

    private NetworkAttachment createNetworkAttachment(Network network, HostNetworkQos hostNetworkQos) {
        NetworkAttachment networkAttachment = new NetworkAttachment();
        networkAttachment.setId(Guid.newGuid());
        networkAttachment.setHostNetworkQos(AnonymousHostNetworkQos.fromHostNetworkQos(hostNetworkQos));
        networkAttachment.setNetworkId(network.getId());
        return networkAttachment;
    }
}

package org.ovirt.engine.core.bll.network.host;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

public class NetworkMtuValidatorTest {

    @Test
    public void testNetworksOnNicMatchMtuWhenNoNetworksAreProvided() throws Exception {

        Map<String, List<Network>> networksOnNics =
            Collections.singletonMap("nicName", Collections.<Network> emptyList());

        NetworkMtuValidator networkMtuValidator =
            new NetworkMtuValidator(new BusinessEntityMap<>(Collections.<Network> emptyList()));

        assertThat(networkMtuValidator.validateMtu(networksOnNics), isValid());
    }

    @Test
    public void testNetworksOnNicMatchMtu() throws Exception {
        List<Network> networks = Arrays.asList(createNetwork(1, false), createNetwork(1, false));

        Map<String, List<Network>> networksOnNics = Collections.singletonMap("nicName", networks);

        NetworkMtuValidator networkMtuValidator = new NetworkMtuValidator(new BusinessEntityMap<>(networks));
        assertThat(networkMtuValidator.validateMtu(networksOnNics), isValid());
    }

    @Test
    public void testNetworksOnNicMatchMtuNetworkMtuDoesNotMatch() throws Exception {
        Network networkA = createNetwork(1, false);
        networkA.setVlanId(11);

        Network networkB = createNetwork(2, false);

        List<Network> networks = Arrays.asList(networkA, networkB);

        Map<String, List<Network>> networksOnNics = Collections.singletonMap("nicName", networks);

        NetworkMtuValidator networkMtuValidator = new NetworkMtuValidator(new BusinessEntityMap<>(networks));

        assertThat(networkMtuValidator.validateMtu(networksOnNics), failsWith(EngineMessage.NETWORK_MTU_DIFFERENCES));
    }

    /**
     * this is probably not a valid scenario, but validation method allows this.
     */
    @Test
    public void testNetworksOnNicMatchMtuIgnoreMtuDifferenceWhenBothNetworksAreVmNetworks() throws Exception {
        List<Network> networks = Arrays.asList(createNetwork(1, true), createNetwork(2, true));

        Map<String, List<Network>> networksOnNics = Collections.singletonMap("nicName", networks);

        NetworkMtuValidator networkMtuValidator = new NetworkMtuValidator(new BusinessEntityMap<>(networks));
        assertThat(networkMtuValidator.validateMtu(networksOnNics), isValid());
    }

    @Test
    public void testGetNetworksOnNics() throws Exception {
        Network networkA = createNetwork(1, true);
        Network networkB = createNetwork(2, true);
        Network networkC = createNetwork(2, true);
        List<Network> networks = Arrays.asList(networkA, networkB, networkC);
        NetworkMtuValidator networkMtuValidatorSpy = spy(new NetworkMtuValidator(new BusinessEntityMap<>(networks)));

        String nicAName = "NicA";
        String nicCName = "NicC";
        NetworkAttachment networkAttachmentA = createNetworkAttachment(networkA, nicAName);
        NetworkAttachment networkAttachmentB = createNetworkAttachment(networkB, nicAName);
        NetworkAttachment networkAttachmentC = createNetworkAttachment(networkC, nicCName);

        List<NetworkAttachment> networkAttachments =
            Arrays.asList(networkAttachmentA, networkAttachmentB, networkAttachmentC);

        Map<String, List<Network>> networksOnNics = networkMtuValidatorSpy.getNetworksOnNics(networkAttachments);
        assertThat(networksOnNics.keySet().size(), is(2));
        assertThat(networksOnNics.containsKey(nicAName), is(true));
        assertThat(networksOnNics.containsKey(nicCName), is(true));

        assertThat(networksOnNics.get(nicAName).size(), is(2));
        assertThat(networksOnNics.get(nicAName), CoreMatchers.hasItems(networkA, networkB));
        assertThat(networksOnNics.get(nicCName).size(), is(1));
        assertThat(networksOnNics.get(nicCName), CoreMatchers.hasItems(networkC));
    }

    private NetworkAttachment createNetworkAttachment(Network networkA, String nicAName) {
        NetworkAttachment networkAttachmentA = new NetworkAttachment();
        networkAttachmentA.setId(Guid.newGuid());
        networkAttachmentA.setNicName(nicAName);
        networkAttachmentA.setNetworkId(networkA.getId());
        return networkAttachmentA;
    }

    private Network createNetwork(int mtu, boolean isVmNetwork) {
        Network network = new Network();
        network.setId(Guid.newGuid());
        network.setMtu(mtu);
        network.setVmNetwork(isVmNetwork);
        return network;
    }

}

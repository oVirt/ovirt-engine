package org.ovirt.engine.core.bll.network.host;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class NetworkMtuValidatorTest {

    @ClassRule
    public static final MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.DefaultMTU, 1500));

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
        List<Network> networks = Collections.singletonList(createNetwork(1, false, "netA"));

        Map<String, List<Network>> networksOnNics = Collections.singletonMap("nicName", networks);

        NetworkMtuValidator networkMtuValidator = new NetworkMtuValidator(new BusinessEntityMap<>(networks));
        assertThat(networkMtuValidator.validateMtu(networksOnNics), isValid());
    }

    @Test
    public void testNetworksOnNicMatchMtuAllDefault() throws Exception {
        Network networkA = createNetwork(0, false, "netA");
        networkA.setVlanId(11);

        Network networkB = createNetwork(NetworkUtils.getDefaultMtu(), false, "netB");

        List<Network> networks = Arrays.asList(networkA, networkB);
        Map<String, List<Network>> networksOnNics = Collections.singletonMap("nicName", networks);
        NetworkMtuValidator networkMtuValidator = new NetworkMtuValidator(new BusinessEntityMap<>(networks));

        assertThat(networkMtuValidator.validateMtu(networksOnNics), isValid());
    }

    @Test
    public void testNetworksOnNicMatchMtuNetworkMtuDoesNotMatch() throws Exception {
        Network networkA = createNetwork(1, false, "netA");
        networkA.setVlanId(11);

        Network networkB = createNetwork(2, false, "netB");

        List<Network> networks = Arrays.asList(networkA, networkB);
        Map<String, List<Network>> networksOnNics = Collections.singletonMap("nicName", networks);
        NetworkMtuValidator networkMtuValidator = new NetworkMtuValidator(new BusinessEntityMap<>(networks));

        assertThat(networkMtuValidator.validateMtu(networksOnNics),
            failsWith(EngineMessage.NETWORK_MTU_DIFFERENCES,
                ReplacementUtils.replaceWith(NetworkMtuValidator.VAR_NETWORK_MTU_DIFFERENCES_LIST,
                    Arrays.asList("netA(1)", "netB(2)"))));
    }

    @Test
    public void testGetNetworksOnNics() throws Exception {
        Network networkA = createNetwork(1, true, "netA");
        Network networkB = createNetwork(2, true, "netB");
        Network networkC = createNetwork(2, true, "netC");
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

    private Network createNetwork(int mtu, boolean isVmNetwork, String networkName) {
        Network network = new Network();
        network.setId(Guid.newGuid());
        network.setName(networkName);
        network.setMtu(mtu);
        network.setVmNetwork(isVmNetwork);
        return network;
    }

}

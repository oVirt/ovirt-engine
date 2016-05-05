package org.ovirt.engine.core.bll.network;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.InjectorRule;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

@RunWith(MockitoJUnitRunner.class)
public class NetworkConfiguratorTest {

    private static final String NETWORK_NAME1 = "networkName";
    private static final String NETWORK_NAME2 = "not" + NETWORK_NAME1;
    private static final String IPV4_ADDRESS = "ipv4 address";
    private static final String IPV6_ADDRESS = "ipv6 address";
    private static final CommandContext COMMAND_CONTEXT = CommandContext.createContext("context");

    @Rule
    public InjectorRule injectorRule = new InjectorRule();

    @Mock
    private ManagementNetworkUtil managementNetworkUtil;

    private VdsNetworkInterface nic = new VdsNetworkInterface();

    private NetworkConfigurator underTest;

    @Before
    public void setUp() {
        injectorRule.bind(ManagementNetworkUtil.class, managementNetworkUtil);

        nic.setNetworkName(NETWORK_NAME1);

        VDS vds = new VDS();
        vds.getInterfaces().add(nic);

        underTest = new NetworkConfigurator(vds, COMMAND_CONTEXT);
    }

    @Test
    public void getIpv4AddressOfNetwork() {
        nic.setIpv4Address(IPV4_ADDRESS);
        assertThat(underTest.getIpv4AddressOfNetwork(NETWORK_NAME1), is(IPV4_ADDRESS));
    }

    @Test
    public void getIpv6AddressOfNetwork() {
        nic.setIpv6Address(IPV6_ADDRESS);
        assertThat(underTest.getIpv6AddressOfNetwork(NETWORK_NAME1), is(IPV6_ADDRESS));
    }

    @Test
    public void getIpv4AddressOfNetworkReturnsNullWhenThereIsNoIpSet() {
        assertThat(underTest.getIpv4AddressOfNetwork(NETWORK_NAME1), nullValue());
    }

    @Test
    public void getIpv6AddressOfNetworkReturnsNullWhenThereIsNoIpSet() {
        assertThat(underTest.getIpv6AddressOfNetwork(NETWORK_NAME1), nullValue());
    }

    @Test
    public void getIpv4AddressOfNetworkReturnsNullWhenThereIsNoNetworkOfGivenName() {
        nic.setIpv4Address(IPV4_ADDRESS);
        assertThat(underTest.getIpv4AddressOfNetwork(NETWORK_NAME2), nullValue());
    }

    @Test
    public void getIpv6AddressOfNetworkReturnsNullWhenThereIsNoNetworkOfGivenName() {
        nic.setIpv6Address(IPV6_ADDRESS);
        assertThat(underTest.getIpv6AddressOfNetwork(NETWORK_NAME2), nullValue());
    }
}

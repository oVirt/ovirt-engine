package org.ovirt.engine.core.bll.network;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.Collection;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.InjectorRule;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.IpV6Address;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

@RunWith(MockitoJUnitRunner.class)
public class NetworkConfiguratorTest {

    private static final String NETWORK_NAME1 = "networkName";
    private static final String NETWORK_NAME2 = "not" + NETWORK_NAME1;
    private static final String IPV4_ADDRESS = "ipv4 address";
    private static final String IPV4_GATEWAY = "ipv4 gateway";
    private static final String IPV4_SUBNET = "ipv4 subnet";
    private static final String IPV6_ADDRESS = "ipv6 address";
    private static final String IPV6_GATEWAY = "ipv6 gateway";
    private static final int IPV6_PREFIX = 666;
    private static final CommandContext COMMAND_CONTEXT = CommandContext.createContext("context");
    private static final Guid MANAGEMENT_NETWORK_ID = Guid.newGuid();
    private static final Guid CLUSTER_ID = Guid.newGuid();
    private static final String NIC_NAME = "nic name";

    @Rule
    public InjectorRule injectorRule = new InjectorRule();

    @Rule
    public MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.Ipv6Supported, Version.v3_6, false),
            mockConfig(ConfigValues.Ipv6Supported, Version.v4_0, true));

    @Mock
    private ManagementNetworkUtil mockManagementNetworkUtil;

    private VDS host;
    private VdsNetworkInterface nic = new VdsNetworkInterface();
    private Network managementNetwork = new Network();

    private NetworkConfigurator underTest;

    @Before
    public void setUp() {
        injectorRule.bind(ManagementNetworkUtil.class, mockManagementNetworkUtil);

        managementNetwork.setId(MANAGEMENT_NETWORK_ID);
        when(mockManagementNetworkUtil.getManagementNetwork(CLUSTER_ID)).thenReturn(managementNetwork);

        nic.setNetworkName(NETWORK_NAME1);
        nic.setName(NIC_NAME);

        host = new VDS();
        host.setClusterId(CLUSTER_ID);
        host.setClusterCompatibilityVersion(Version.v4_0);
        host.getInterfaces().add(nic);

        underTest = new NetworkConfigurator(host, COMMAND_CONTEXT);
    }

    @Test
    public void getIpv4AddressOfNetwork() {
        setIpv4Details(nic);
        assertThat(underTest.getIpv4AddressOfNetwork(NETWORK_NAME1), is(IPV4_ADDRESS));
    }

    @Test
    public void getIpv6AddressOfNetwork() {
        setIpv6Details(nic);
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

    @Test
    public void createSetupNetworkParamsInLegacyCluster() {
        createSetupNetworkParamsTest(Version.v3_6, empty());
    }

    @Test
    public void createSetupNetworkParamsInNewCluster() {
        final IpConfiguration actual =
                createSetupNetworkParamsTest(Version.v4_0, hasSize(1));
        assertIpv6Details(actual);
    }

    private IpConfiguration createSetupNetworkParamsTest(
            Version clusterVersion,
            Matcher<Collection<? extends IpV6Address>> ipv6AddressMatcher) {

        setIpDetails(nic);
        host.setClusterCompatibilityVersion(clusterVersion);

        final HostSetupNetworksParameters actual = underTest.createSetupNetworkParams(nic);

        assertThat(actual.getNetworkAttachments(), hasSize(1));
        final IpConfiguration ipConfiguration = actual.getNetworkAttachments().get(0).getIpConfiguration();
        assertIpv4Details(ipConfiguration);
        assertThat(ipConfiguration.getIpV6Addresses(), ipv6AddressMatcher);

        return ipConfiguration;
    }

    private void assertIpv4Details(IpConfiguration ipConfiguration) {
        assertThat(ipConfiguration.getIPv4Addresses(), hasSize(1));
        final IPv4Address ipv4Address = ipConfiguration.getIPv4Addresses().get(0);
        assertThat(ipv4Address.getAddress(), is(IPV4_ADDRESS));
        assertThat(ipv4Address.getBootProtocol(), is(Ipv4BootProtocol.STATIC_IP));
        assertThat(ipv4Address.getAddress(), is(IPV4_ADDRESS));
    }

    private void assertIpv6Details(IpConfiguration ipConfiguration) {
        assertThat(ipConfiguration.getIpV6Addresses(), hasSize(1));
        final IpV6Address ipv6Address = ipConfiguration.getIpV6Addresses().get(0);
        assertThat(ipv6Address.getAddress(), is(IPV6_ADDRESS));
        assertThat(ipv6Address.getBootProtocol(), is(Ipv6BootProtocol.STATIC_IP));
        assertThat(ipv6Address.getAddress(), is(IPV6_ADDRESS));
    }

    private void setIpDetails(VdsNetworkInterface nic) {
        setIpv4Details(nic);

        setIpv6Details(nic);
    }

    private void setIpv4Details(VdsNetworkInterface nic) {
        nic.setIpv4Address(IPV4_ADDRESS);
        nic.setIpv4BootProtocol(Ipv4BootProtocol.STATIC_IP);
        nic.setIpv4Gateway(IPV4_GATEWAY);
        nic.setIpv4Subnet(IPV4_SUBNET);
    }

    private void setIpv6Details(VdsNetworkInterface nic) {
        nic.setIpv6Address(IPV6_ADDRESS);
        nic.setIpv6BootProtocol(Ipv6BootProtocol.STATIC_IP);
        nic.setIpv6Gateway(IPV6_GATEWAY);
        nic.setIpv6Prefix(IPV6_PREFIX);
    }

}

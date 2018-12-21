package org.ovirt.engine.core.bll.network;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Map;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.network.NetworkConfigurator.NetworkConfiguratorException;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.IpV6Address;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.utils.InjectedMock;
import org.ovirt.engine.core.utils.InjectorExtension;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class, InjectorExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
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
    private static final String MANAGEMENT_NETWORK_NAME = "management network name";
    private static final int NIC_VLAN_ID = 666;
    private static final int MANAGMENT_NETWORK_VLAN_ID = 777;
    private static final String HOST_NAME = "host name";

    @Mock
    @InjectedMock
    public ManagementNetworkUtil mockManagementNetworkUtil;

    @Mock
    private BackendInternal backend;
    @Mock
    private AuditLogDirector auditLogDirector;
    @Captor
    private ArgumentCaptor<AuditLogable> auditLogableArgumentCaptor;

    private VDS host;
    private VdsNetworkInterface nic = new VdsNetworkInterface();
    private Network managementNetwork = new Network();

    private NetworkConfigurator underTest;

    @BeforeEach
    public void setUp() {
        managementNetwork.setId(MANAGEMENT_NETWORK_ID);
        managementNetwork.setName(MANAGEMENT_NETWORK_NAME);
        when(mockManagementNetworkUtil.getManagementNetwork(CLUSTER_ID)).thenReturn(managementNetwork);

        nic.setNetworkName(NETWORK_NAME1);
        nic.setName(NIC_NAME);

        host = new VDS();
        host.setVdsName(HOST_NAME);
        host.setClusterId(CLUSTER_ID);
        host.setClusterCompatibilityVersion(Version.v4_3);
        host.getInterfaces().add(nic);

        underTest = new NetworkConfigurator(host, COMMAND_CONTEXT, auditLogDirector);
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
    public void createSetupNetworkParamsInNewCluster() {
        final IpConfiguration actual =
                createSetupNetworkParamsTest(Version.v4_3, hasSize(1));
        assertIpv6Details(actual);
    }

    @Test
    public void testCreateManagementNetworkIfRequiredFailedOnSetupNetworks() {
        host.setActiveNic(NIC_NAME);

        final NetworkConfigurator spiedUnderTest = spy(underTest);
        doReturn(backend).when(spiedUnderTest).getBackend();
        when(backend.runInternalAction(eq(ActionType.HostSetupNetworks), any(), any()))
                .thenReturn(createReturnValue(false));

        verifyAuditLoggableBaseFilledProperly(
                spiedUnderTest,
                AuditLogType.SETUP_NETWORK_FAILED_FOR_MANAGEMENT_NETWORK_CONFIGURATION);
    }

    @Test
    public void testCreateManagementNetworkIfRequiredFailedOnCommitNetworkChanges() {
        host.setActiveNic(NIC_NAME);

        final NetworkConfigurator spiedUnderTest = spy(underTest);
        doReturn(backend).when(spiedUnderTest).getBackend();
        when(backend.runInternalAction(eq(ActionType.HostSetupNetworks), any(), any()))
                .thenReturn(createReturnValue(true));
        when(backend.runInternalAction(
                eq(ActionType.CommitNetworkChanges), any(), any())).thenReturn(createReturnValue(false));

        verifyAuditLoggableBaseFilledProperly(
                spiedUnderTest,
                AuditLogType.PERSIST_NETWORK_FAILED_FOR_MANAGEMENT_NETWORK);
    }

    @Test
    public void testCreateManagementNetworkIfRequiredFailsOnDifferentVlanId() {
        host.setActiveNic(NIC_NAME);
        nic.setVlanId(NIC_VLAN_ID);
        managementNetwork.setVlanId(MANAGMENT_NETWORK_VLAN_ID);

        final Map<String, String> capturedCustomValues = verifyAuditLoggableBaseFilledProperly(
                underTest,
                AuditLogType.VLAN_ID_MISMATCH_FOR_MANAGEMENT_NETWORK_CONFIGURATION);
        assertThat(capturedCustomValues, allOf(
                hasEntry("vlanid", String.valueOf(NIC_VLAN_ID)),
                hasEntry("mgmtvlanid", String.valueOf(MANAGMENT_NETWORK_VLAN_ID)),
                hasEntry("interfacename", NIC_NAME)));
    }

    private Map<String, String> verifyAuditLoggableBaseFilledProperly(NetworkConfigurator underTest, AuditLogType auditLogType) {
        try {
            underTest.createManagementNetworkIfRequired();
        } catch (NetworkConfiguratorException e) {
            verify(auditLogDirector).log(
                    auditLogableArgumentCaptor.capture(),
                    eq(auditLogType));
            final AuditLogable capturedEvent = auditLogableArgumentCaptor.getValue();
            assertThat(capturedEvent.getVdsName(), is(HOST_NAME));
            return capturedEvent.getCustomValues();
        }
        fail("The test should lead to NetworkConfiguratorException");
        return null;
    }

    private ActionReturnValue createReturnValue(boolean succeed) {
        final ActionReturnValue setupNetsReturnValue = new ActionReturnValue();
        setupNetsReturnValue.setSucceeded(succeed);
        return setupNetsReturnValue;
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

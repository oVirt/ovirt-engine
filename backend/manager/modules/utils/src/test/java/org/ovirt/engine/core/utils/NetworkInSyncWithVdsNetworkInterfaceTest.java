package org.ovirt.engine.core.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.IpV6Address;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NameServer;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfiguration;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfigurationType;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfigurations;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.network.SwitchType;
import org.ovirt.engine.core.compat.Version;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
public class NetworkInSyncWithVdsNetworkInterfaceTest {

    private static final int DEFAULT_MTU_VALUE = 1500;
    private static final int VALUE_DENOTING_THAT_MTU_SHOULD_BE_SET_TO_DEFAULT_VALUE = 0;

    private static final Ipv4BootProtocol IPV4_BOOT_PROTOCOL =
            Ipv4BootProtocol.forValue(RandomUtils.instance().nextInt(Ipv4BootProtocol.values().length));
    private static final Ipv6BootProtocol IPV6_BOOT_PROTOCOL =
            Ipv6BootProtocol.forValue(RandomUtils.instance().nextInt(Ipv6BootProtocol.values().length));

    private static final String IPV4_ADDRESS = "ipv4 address";
    private static final String IPV4_NETMASK = "ipv4 netmask";
    private static final String IPV4_GATEWAY = "ipv4 gateway";

    private static final String IPV6_ADDRESS = "ipv6 address";
    private static final Integer IPV6_PREFIX = 666;
    private static final String IPV6_GATEWAY = "ipv6 gateway";

    private VdsNetworkInterface iface;
    private Network network;
    private HostNetworkQos ifaceQos;
    private HostNetworkQos networkQos;
    private NetworkAttachment testedNetworkAttachment;
    private Cluster cluster;

    private IPv4Address ipv4Address = new IPv4Address();
    private IpV6Address ipv6Address = new IpV6Address();

    private DnsResolverConfiguration sampleDnsResolverConfiguration;
    private DnsResolverConfiguration sampleDnsResolverConfiguration2;
    private DnsResolverConfiguration sampleDnsResolverConfigurationWithReversedNameServers;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.DefaultMTU, 1500),
                MockConfigDescriptor.of(ConfigValues.DefaultRouteReportedByVdsm, Version.v4_2, true),
                MockConfigDescriptor.of(ConfigValues.DefaultRouteReportedByVdsm, Version.v4_1, false)
        );
    }

    @BeforeEach
    public void setUp() {
        sampleDnsResolverConfiguration = new DnsResolverConfiguration();
        sampleDnsResolverConfiguration.setNameServers(Arrays.asList(
                        new NameServer("192.168.1.1"),
                        new NameServer("2001:0db8:85a3:0000:0000:8a2e:0370:7334")));

        sampleDnsResolverConfiguration2 = new DnsResolverConfiguration();
        sampleDnsResolverConfiguration2.setNameServers(Arrays.asList(
                new NameServer("192.168.1.2"),
                new NameServer("2002:0db8:85a3:0000:0000:8a2e:0370:7334")));

        sampleDnsResolverConfigurationWithReversedNameServers = reverseNameServersOrder(sampleDnsResolverConfiguration);

        ifaceQos = new HostNetworkQos();
        networkQos = new HostNetworkQos();
        iface = new VdsNetworkInterface();
        //needed because network is vm network by default
        iface.setBridged(true);
        iface.setQos(ifaceQos);
        iface.setReportedSwitchType(SwitchType.LEGACY);
        iface.setIpv4DefaultRoute(false);
        iface.setMtu(getDefaultHostMtu());

        network = new Network();
        network.setDnsResolverConfiguration(sampleDnsResolverConfiguration);

        testedNetworkAttachment = new NetworkAttachment();
        testedNetworkAttachment.setIpConfiguration(new IpConfiguration());

        cluster = new Cluster();
        cluster.setCompatibilityVersion(Version.v4_2);
        cluster.setRequiredSwitchTypeForCluster(SwitchType.LEGACY);
    }

    @Test
    public void testIsNetworkInSyncWhenMtuDifferent() {
        iface.setMtu(1);
        network.setMtu(2);
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenMtuSameViaDefault() {
        NetworkInSyncWithVdsNetworkInterface testedInstanceWithSameNonQosValues =
                createTestedInstanceWithSameNonQosValues();

        iface.setMtu(DEFAULT_MTU_VALUE);
        network.setMtu(VALUE_DENOTING_THAT_MTU_SHOULD_BE_SET_TO_DEFAULT_VALUE);

        assertThat(testedInstanceWithSameNonQosValues.isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenVlanIdDifferent() {
        iface.setMtu(1);
        network.setMtu(1);

        iface.setVlanId(1);
        network.setVlanId(2);
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenBridgedFlagDifferent() {
        iface.setMtu(1);
        network.setMtu(1);

        iface.setVlanId(1);
        network.setVlanId(1);

        iface.setBridged(true);
        network.setVmNetwork(false);
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenIfaceQosEqual() {
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIfaceQosIsNull() {
        iface.setQos(null);
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenNetworkQosIsNull() {
        networkQos = null;
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenBothQosIsNull() {
        iface.setQos(null);
        networkQos = null;
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIfaceQosIsNullIfaceQosOverridden() {
        iface.setQos(null);
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenNetworkQosIsNullIfaceQosOverridden() {
        networkQos = null;
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenAverageLinkShareDifferent() {
        ifaceQos.setOutAverageLinkshare(1);
        networkQos.setOutAverageLinkshare(2);
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenAverageUpperLimitDifferent() {
        ifaceQos.setOutAverageUpperlimit(1);
        networkQos.setOutAverageUpperlimit(2);
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenAverageRealTimeDifferent() {
        ifaceQos.setOutAverageRealtime(1);
        networkQos.setOutAverageRealtime(2);
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(false));
    }

    private int getDefaultHostMtu() {
        return NetworkUtils.getHostMtuActualValue(new Network());
    }

    private NetworkInSyncWithVdsNetworkInterface createTestedInstanceWithSameNonQosValues() {
        return createTestedInstanceWithSameNonQosValues(false);
    }

    private NetworkInSyncWithVdsNetworkInterface createTestedInstanceWithSameNonQosValues(boolean isDefaultRouteNetwork) {
        iface.setMtu(1);
        network.setMtu(1);

        iface.setVlanId(1);
        network.setVlanId(1);

        iface.setBridged(true);
        network.setVmNetwork(true);
        return createTestedInstance(isDefaultRouteNetwork, sampleDnsResolverConfiguration);
    }

    private NetworkInSyncWithVdsNetworkInterface createTestedInstance() {
        return createTestedInstance(false, sampleDnsResolverConfiguration);
    }

    private NetworkInSyncWithVdsNetworkInterface createTestedInstance(boolean isDefaultRouteNetwork,
            DnsResolverConfiguration reportedDnsResolverConfiguration) {
        return new NetworkInSyncWithVdsNetworkInterface(iface,
                network,
                networkQos,
                testedNetworkAttachment,
                reportedDnsResolverConfiguration,
                cluster,
                isDefaultRouteNetwork);
    }

    @Test
    public void testReportConfigurationsOnHost() {
        NetworkInSyncWithVdsNetworkInterface testedInstanceWithSameNonQosValues =
                createTestedInstanceWithSameNonQosValues();
        ifaceQos.setOutAverageLinkshare(1);
        ifaceQos.setOutAverageUpperlimit(1);
        ifaceQos.setOutAverageRealtime(1);

        ReportedConfigurations reportedConfigurations = testedInstanceWithSameNonQosValues.reportConfigurationsOnHost();


        assertThat(reportedConfigurations.isNetworkInSync(), is(false));
        List<ReportedConfiguration> reportedConfigurationList = reportedConfigurations.getReportedConfigurationList();

        List<ReportedConfiguration> expectedReportedConfigurations = addReportedConfigurations(
                combineReportedConfigurations(createBasicReportedConfigurations(),
                        reportQos(false)),
                defaultRouteReportedConfiguration(false)
        );

        assertThat(reportedConfigurationList.containsAll(expectedReportedConfigurations), is(true));
        assertThat(reportedConfigurationList.size(), is(expectedReportedConfigurations.size()));
    }

    private ReportedConfiguration defaultRouteReportedConfiguration(boolean expectedValue) {
        return new ReportedConfiguration(ReportedConfigurationType.DEFAULT_ROUTE,
                iface.isIpv4DefaultRoute(),
                expectedValue,
                Objects.equals(iface.isIpv4DefaultRoute(), expectedValue));
    }

    @Test
    public void testReportConfigurationsOnHostWhenSwitchTypeIsOutOfSync() {
        cluster.setRequiredSwitchTypeForCluster(SwitchType.OVS);

        ReportedConfigurations reportedConfigurations = createTestedInstance().reportConfigurationsOnHost();

        assertThat(reportedConfigurations.isNetworkInSync(), is(false));
        List<ReportedConfiguration> reportedConfigurationList = reportedConfigurations.getReportedConfigurationList();

        ReportedConfiguration expectedReportedConfiguration = new ReportedConfiguration(ReportedConfigurationType.SWITCH_TYPE,
                SwitchType.LEGACY,
                SwitchType.OVS,
                false);

        assertThat(reportedConfigurationList.contains(expectedReportedConfiguration), is(true));
    }

    @Test
    public void testReportConfigurationsOnHostWhenDefaultRouteDiffers() {
        //cannot use initIpv4ConfigurationBootProtocol because of 'randomized tests' technique.
        iface.setIpv4BootProtocol(Ipv4BootProtocol.DHCP);
        IPv4Address address = new IPv4Address();
        address.setBootProtocol(Ipv4BootProtocol.DHCP);
        testedNetworkAttachment.getIpConfiguration().setIPv4Addresses(Collections.singletonList(address));

        //network has default route role
        NetworkInSyncWithVdsNetworkInterface testedInstance = createTestedInstanceWithSameNonQosValues(true);
        ReportedConfigurations reportedConfigurations = testedInstance.reportConfigurationsOnHost();

        assertThat(reportedConfigurations.isNetworkInSync(), is(false));
        List<ReportedConfiguration> reportedConfigurationList = reportedConfigurations.getReportedConfigurationList();

        List<ReportedConfiguration> expectedReportedConfigurations = addReportedConfigurations(
                combineReportedConfigurations(createBasicReportedConfigurations(), reportQos(true)),

                new ReportedConfiguration(ReportedConfigurationType.IPV4_BOOT_PROTOCOL,
                        iface.getIpv4BootProtocol().name(),
                        /*ipv4Address*/address.getBootProtocol().name(),
                        true),

                new ReportedConfiguration(ReportedConfigurationType.DNS_CONFIGURATION,
                        addressesAsString(sampleDnsResolverConfiguration.getNameServers()),
                        "192.168.1.1,2001:0db8:85a3:0000:0000:8a2e:0370:7334",
                        true),

                new ReportedConfiguration(ReportedConfigurationType.DEFAULT_ROUTE, false, true, false)
        );

        assertThat(reportedConfigurationList.containsAll(expectedReportedConfigurations), is(true));
        assertThat(reportedConfigurationList.size(), is(expectedReportedConfigurations.size()));
    }

    @Test
    public void testReportConfigurationsOnHostWhenDnsConfigurationResolverOutOfSync() {
        iface.setIpv4DefaultRoute(true);
        //cannot use initIpv4ConfigurationBootProtocol because of 'randomized tests' technique.
        iface.setIpv4BootProtocol(Ipv4BootProtocol.DHCP);
        IPv4Address address = new IPv4Address();
        address.setBootProtocol(Ipv4BootProtocol.DHCP);
        testedNetworkAttachment.getIpConfiguration().setIPv4Addresses(Collections.singletonList(address));


        network.setDnsResolverConfiguration(sampleDnsResolverConfiguration2);
        ReportedConfigurations reportedConfigurations = createTestedInstanceWithSameNonQosValues(true).reportConfigurationsOnHost();

        assertThat(reportedConfigurations.isNetworkInSync(), is(false));
        List<ReportedConfiguration> reportedConfigurationList = reportedConfigurations.getReportedConfigurationList();

        List<ReportedConfiguration> expectedReportedConfigurations = addReportedConfigurations(
                combineReportedConfigurations(createBasicReportedConfigurations(), reportQos(true)),

                new ReportedConfiguration(ReportedConfigurationType.IPV4_BOOT_PROTOCOL,
                        iface.getIpv4BootProtocol().name(),
                        /*ipv4Address*/address.getBootProtocol().name(),
                        true),

                new ReportedConfiguration(ReportedConfigurationType.DNS_CONFIGURATION,
                        addressesAsString(sampleDnsResolverConfiguration.getNameServers()),
                        addressesAsString(network.getDnsResolverConfiguration().getNameServers()),
                        false),
                new ReportedConfiguration(ReportedConfigurationType.DEFAULT_ROUTE, true, true, true)

        );

        assertThat(reportedConfigurationList.containsAll(expectedReportedConfigurations), is(true));
        assertThat(reportedConfigurationList.size(), is(expectedReportedConfigurations.size()));
    }

    @Test
    public void testReportConfigurationsOnHostWhenIfaceQosIsNull() {
        ifaceQos = null;
        iface.setQos(null);
        NetworkInSyncWithVdsNetworkInterface testedInstanceWithSameNonQosValues =
                createTestedInstanceWithSameNonQosValues();
        networkQos.setOutAverageLinkshare(1);
        networkQos.setOutAverageUpperlimit(1);
        networkQos.setOutAverageRealtime(1);

        ReportedConfigurations reportedConfigurations = testedInstanceWithSameNonQosValues.reportConfigurationsOnHost();

        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(false));
        assertThat(reportedConfigurations.isNetworkInSync(), is(false));
        List<ReportedConfiguration> reportedConfigurationList = reportedConfigurations.getReportedConfigurationList();

        List<ReportedConfiguration> expectedReportedConfigurations = addReportedConfigurations(
                createBasicReportedConfigurations(),
                defaultRouteReportedConfiguration(false),

                new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_LINK_SHARE,
                        null,
                        networkQos.getOutAverageLinkshare(),
                        false),
                new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_REAL_TIME,
                        null,
                        networkQos.getOutAverageRealtime(),
                        false),
                new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_UPPER_LIMIT,
                        null,
                        networkQos.getOutAverageUpperlimit(),
                        false)
        );

        assertThat(reportedConfigurationList.containsAll(expectedReportedConfigurations), is(true));
        assertThat(reportedConfigurationList.size(), is(expectedReportedConfigurations.size()));
    }

    @Test
    public void testIsNetworkInSyncWhenIpConfigurationIsNull() {
        this.testedNetworkAttachment.setIpConfiguration(null);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpConfigurationIsEmpty() {
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv4BootProtocolEqual() {
        initIpv4ConfigurationBootProtocol(true);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv4BootProtocolDifferent() {
        initIpv4ConfigurationBootProtocol(false);
        iface.setIpv4BootProtocol(Ipv4BootProtocol.forValue(
                (IPV4_BOOT_PROTOCOL.getValue() + 1) % Ipv4BootProtocol.values().length));
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv4StaticBootProtocolAddressEqual() {
        initIpv4ConfigurationBootProtocolAddress(IPV4_BOOT_PROTOCOL, true);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv4StaticBootProtocolAddressDifferent() {
        initIpv4ConfigurationBootProtocolAddress(IPV4_BOOT_PROTOCOL, false);
        iface.setIpv4BootProtocol(Ipv4BootProtocol.forValue(
                (IPV4_BOOT_PROTOCOL.getValue() + 1) % Ipv4BootProtocol.values().length));
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv4StaticBootProtocolNetmaskEqual() {
        initIpv4ConfigurationBootProtocolNetmask(IPV4_BOOT_PROTOCOL, true);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv4StaticBootProtocolNetmaskDifferent() {
        initIpv4ConfigurationBootProtocolNetmask(IPV4_BOOT_PROTOCOL, false);
        iface.setIpv4BootProtocol(Ipv4BootProtocol.forValue(
                (IPV4_BOOT_PROTOCOL.getValue() + 1) % Ipv4BootProtocol.values().length));
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv4BootProtocolNotStaticAddressDifferent() {
        initIpv4ConfigurationBootProtocolAddress(Ipv4BootProtocol.NONE, false);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv4BootProtocolNotStaticNetmaskDifferent() {
        initIpv4ConfigurationBootProtocolNetmask(Ipv4BootProtocol.NONE, false);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv4GatewayEqual(){
        initIpv4ConfigurationBootProtocolGateway(Ipv4BootProtocol.STATIC_IP, true);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv4GatewayBothNull() {
        initIpv4ConfigurationStaticBootProtocol(Ipv4BootProtocol.STATIC_IP);
        ipv4Address.setGateway(null);
        iface.setIpv4Gateway(null);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv4GatewayDifferent(){
        initIpv4ConfigurationBootProtocolGateway(Ipv4BootProtocol.STATIC_IP, false);
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    @Test
    public void testReportConfigurationsOnHostWhenIpv4BootProtocolNotStatic() {
        initIpv4ConfigurationBootProtocolAddress(Ipv4BootProtocol.NONE, false);
        initIpv4ConfigurationBootProtocolNetmask(Ipv4BootProtocol.NONE, false);
        initIpv4ConfigurationBootProtocolGateway(Ipv4BootProtocol.NONE, false);
        NetworkInSyncWithVdsNetworkInterface testedInstanceWithSameNonQosValues =
                createTestedInstanceWithSameNonQosValues();
        List<ReportedConfiguration> reportedConfigurationList =
                testedInstanceWithSameNonQosValues.reportConfigurationsOnHost().getReportedConfigurationList();
        IPv4Address primaryAddress = this.testedNetworkAttachment.getIpConfiguration().getIpv4PrimaryAddress();

        List<ReportedConfiguration> expectedReportedConfigurations = addReportedConfigurations(
                createBasicAndQosReportedConfigurations(),
                defaultRouteReportedConfiguration(false),

                new ReportedConfiguration(ReportedConfigurationType.IPV4_BOOT_PROTOCOL,
                        iface.getIpv4BootProtocol().name(),
                        primaryAddress.getBootProtocol().name(),
                        true)
        );

        assertThat(reportedConfigurationList.containsAll(expectedReportedConfigurations), is(true));
        assertThat(reportedConfigurationList.size(), is(expectedReportedConfigurations.size()));
    }

    @Test
    public void testReportConfigurationsOnHostWhenIpv4BootProtocolStaticAndAddressSynced() {
        testReportConfigurationsOnHostWhenIpv4BootProtocolStatic(true, false, false);
    }

    @Test
    public void testReportConfigurationsOnHostWhenIpv4BootProtocolStaticAndNetmaskSynced() {
        testReportConfigurationsOnHostWhenIpv4BootProtocolStatic(false, true, false);
    }

    @Test
    public void testReportConfigurationsOnHostWhenIpv4BootProtocolStaticAndGatewaySynced() {
        testReportConfigurationsOnHostWhenIpv4BootProtocolStatic(false, false, true);
    }

    private void testReportConfigurationsOnHostWhenIpv4BootProtocolStatic(boolean syncAddress, boolean syncNetmask, boolean syncGateway) {
        initIpv4ConfigurationBootProtocolAddress(Ipv4BootProtocol.STATIC_IP, syncAddress);
        initIpv4ConfigurationBootProtocolNetmask(Ipv4BootProtocol.STATIC_IP, syncNetmask);
        initIpv4ConfigurationBootProtocolGateway(Ipv4BootProtocol.STATIC_IP, syncGateway);
        NetworkInSyncWithVdsNetworkInterface testedInstanceWithSameNonQosValues =
                createTestedInstanceWithSameNonQosValues();
        List<ReportedConfiguration> reportedConfigurationList =
                testedInstanceWithSameNonQosValues.reportConfigurationsOnHost().getReportedConfigurationList();
        IPv4Address primaryAddress = this.testedNetworkAttachment.getIpConfiguration().getIpv4PrimaryAddress();

        List<ReportedConfiguration> expectedReportedConfigurations = addReportedConfigurations(
                createBasicAndQosReportedConfigurations(),

                defaultRouteReportedConfiguration(false),

                new ReportedConfiguration(ReportedConfigurationType.IPV4_BOOT_PROTOCOL,
                        iface.getIpv4BootProtocol().name(),
                        primaryAddress.getBootProtocol().name(),
                        true),
                new ReportedConfiguration(ReportedConfigurationType.IPV4_NETMASK,
                        iface.getIpv4Subnet(),
                        primaryAddress.getNetmask(),
                        syncNetmask),
                new ReportedConfiguration(ReportedConfigurationType.IPV4_ADDRESS,
                        iface.getIpv4Address(),
                        primaryAddress.getAddress(),
                        syncAddress),
                new ReportedConfiguration(ReportedConfigurationType.IPV4_GATEWAY,
                        iface.getIpv4Gateway(),
                        primaryAddress.getGateway(),
                        syncGateway)
        );

        for (ReportedConfiguration expectedReportedConfiguration : expectedReportedConfigurations) {
            assertThat("expected configuration not reported:" + expectedReportedConfiguration,
                    reportedConfigurationList.contains(expectedReportedConfiguration),
                    is(true));
        }
        assertThat(reportedConfigurationList.size(), is(expectedReportedConfigurations.size()));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv6BootProtocolEqual() {
        initIpv6ConfigurationBootProtocol(true);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv6StaticBootProtocolAddressEqual() {
        initIpv6ConfigurationBootProtocolAddress(IPV6_BOOT_PROTOCOL, true);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv6StaticBootProtocolNetmaskEqual() {
        initIpv6ConfigurationBootProtocolPrefix(IPV6_BOOT_PROTOCOL, true);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv6BootProtocolNotStaticAddressDifferent() {
        initIpv6ConfigurationBootProtocolAddress(Ipv6BootProtocol.NONE, false);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv6BootProtocolNotStaticNetmaskDifferent() {
        initIpv6ConfigurationBootProtocolPrefix(Ipv6BootProtocol.NONE, false);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv6GatewayEqual(){
        initIpv6ConfigurationBootProtocolGateway(Ipv6BootProtocol.STATIC_IP, true);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv6GatewayBothNull() {
        initIpv6ConfigurationStaticBootProtocol(Ipv6BootProtocol.STATIC_IP);
        ipv6Address.setGateway(null);
        iface.setIpv6Gateway(null);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

/*
    TODO: YZ - uncomment the tests after v4.0 is branched out.

    Reporting out-of-sync IPv6 configuration is disabled temporary.
    It's planned to be re-enabled after v4.0-beta is released.

    @Test
    public void testIsNetworkInSyncWhenIpv6BootProtocolDifferent() {
        initIpv6ConfigurationBootProtocol(false);
        iface.setIpv6BootProtocol(Ipv6BootProtocol.forValue(
                (IPV6_BOOT_PROTOCOL.getValue() + 1) % Ipv6BootProtocol.values().length));
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv6StaticBootProtocolAddressDifferent() throws Exception {
        initIpv6ConfigurationBootProtocolAddress(IPV6_BOOT_PROTOCOL, false);
        iface.setIpv6BootProtocol(Ipv6BootProtocol.forValue(
                (IPV6_BOOT_PROTOCOL.getValue() + 1) % Ipv6BootProtocol.values().length));
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv6StaticBootProtocolPrefixDifferent() throws Exception {
        initIpv6ConfigurationBootProtocolPrefix(IPV6_BOOT_PROTOCOL, false);
        iface.setIpv6BootProtocol(Ipv6BootProtocol.forValue(
                (IPV6_BOOT_PROTOCOL.getValue() + 1) % Ipv6BootProtocol.values().length));
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv6GatewayDifferent(){
        initIpv6ConfigurationBootProtocolGateway(Ipv6BootProtocol.STATIC_IP, false);
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    @Test
    public void testReportConfigurationsOnHostWhenIpv6BootProtocolNotStatic() {
        initIpv6ConfigurationBootProtocolAddress(Ipv6BootProtocol.NONE, false);
        initIpv6ConfigurationBootProtocolPrefix(Ipv6BootProtocol.NONE, false);
        initIpv6ConfigurationBootProtocolGateway(Ipv6BootProtocol.NONE, false);
        NetworkInSyncWithVdsNetworkInterface testedInstanceWithSameNonQosValues =
                createTestedInstanceWithSameNonQosValues();
        List<ReportedConfiguration> reportedConfigurationList =
                testedInstanceWithSameNonQosValues.reportConfigurationsOnHost().getReportedConfigurationList();

        IpV6Address primaryAddress = this.testedNetworkAttachment.getIpConfiguration().getIpv6PrimaryAddress();
        List<ReportedConfiguration> expectedReportedConfigurations = combineReportedConfigurations(
                createBasicAndQosReportedConfigurations(),
                new ReportedConfiguration(ReportedConfigurationType.IPV6_BOOT_PROTOCOL,
                        iface.getIpv6BootProtocol().name(),
                        primaryAddress.getBootProtocol().name(),
                        true));
        assertThat(reportedConfigurationList.containsAll(expectedReportedConfigurations), is(true));
        assertThat(reportedConfigurationList.size(), is(expectedReportedConfigurations.size()));
    }

    @Test
    public void testReportConfigurationsOnHostWhenIpv6BootProtocolStatic() {
        boolean syncIpv6Address = RandomUtils.instance().nextBoolean();
        boolean syncIpv6Prefix = RandomUtils.instance().nextBoolean();
        boolean syncIpv6Gateway = RandomUtils.instance().nextBoolean();
        initIpv6ConfigurationBootProtocolAddress(Ipv6BootProtocol.STATIC_IP, syncIpv6Address);
        initIpv6ConfigurationBootProtocolPrefix(Ipv6BootProtocol.STATIC_IP, syncIpv6Prefix);
        initIpv6ConfigurationBootProtocolGateway(Ipv6BootProtocol.STATIC_IP, syncIpv6Gateway);
        NetworkInSyncWithVdsNetworkInterface testedInstanceWithSameNonQosValues =
                createTestedInstanceWithSameNonQosValues();
        List<ReportedConfiguration> reportedConfigurationList =
                testedInstanceWithSameNonQosValues.reportConfigurationsOnHost().getReportedConfigurationList();
        IpV6Address primaryAddress = this.testedNetworkAttachment.getIpConfiguration().getIpv6PrimaryAddress();
        List<ReportedConfiguration> expectedReportedConfigurations = combineReportedConfigurations(
                createBasicAndQosReportedConfigurations(),

                new ReportedConfiguration(ReportedConfigurationType.IPV6_BOOT_PROTOCOL,
                        iface.getIpv6BootProtocol().name(),
                        primaryAddress.getBootProtocol().name(),
                        true),
                new ReportedConfiguration(ReportedConfigurationType.IPV6_PREFIX,
                        Objects.toString(iface.getIpv6Prefix(), null),
                        Objects.toString(primaryAddress.getPrefix(), null),
                        syncIpv6Prefix),
                new ReportedConfiguration(ReportedConfigurationType.IPV6_ADDRESS,
                        iface.getIpv6Address(),
                        primaryAddress.getAddress(),
                        syncIpv6Address),
                new ReportedConfiguration(ReportedConfigurationType.IPV6_GATEWAY,
                        iface.getIpv6Gateway(),
                        primaryAddress.getGateway(),
                        syncIpv6Gateway));

        for (ReportedConfiguration expectedReportedConfiguration : expectedReportedConfigurations) {
            assertThat(reportedConfigurationList, hasItem(expectedReportedConfiguration));
        }
        assertThat(reportedConfigurationList.size(), is(expectedReportedConfigurations.size()));
    }
*/

    @Test
    public void testDnsResolverConfigurationInSync() {
        testDnsResolverConfiguration(null, null, null, true);
    }

    @Test
    public void testDnsResolverConfigurationNetworkAttachmentInSyncWithHost() {
        testDnsResolverConfiguration(sampleDnsResolverConfiguration, null, sampleDnsResolverConfiguration, true);
    }

    @Test
    public void testDnsResolverConfigurationNetworkAttachmentOutOfSyncWithHostDueNoDataOnHost() {
        testDnsResolverConfiguration(null, null, sampleDnsResolverConfiguration, false);
    }

    @Test
    public void testDnsResolverConfigurationNetworkInSyncWithHost() {
        testDnsResolverConfiguration(sampleDnsResolverConfiguration, sampleDnsResolverConfiguration, null, true);
    }

    @Test
    public void testDnsResolverConfigurationNetworkOutOfSyncWithHostDueNoDataOnHost() {
        testDnsResolverConfiguration(null, sampleDnsResolverConfiguration, null, false);
    }

    @Test
    public void testDnsResolverConfigurationNetworkOutOfSyncWithHostDueNoDataOnNetworkOrNetworkAttachment() {
        testDnsResolverConfiguration(sampleDnsResolverConfiguration, null, null, true);
    }

    @Test
    public void testDnsResolverConfigurationNetworkAttachmentHasPrecedenceNetworkAttachmentInSyncWithHost() {
        testDnsResolverConfiguration(sampleDnsResolverConfiguration,
                sampleDnsResolverConfiguration2,
                sampleDnsResolverConfiguration,
                true);
    }

    @Test
    public void testDnsResolverConfigurationOrderOfAddressesIsNotImportant() {
        testDnsResolverConfiguration(this.sampleDnsResolverConfigurationWithReversedNameServers,
                null,
                this.sampleDnsResolverConfiguration,
                true);
    }

    private DnsResolverConfiguration reverseNameServersOrder(DnsResolverConfiguration sampleDnsResolverConfiguration) {
        List<NameServer> reversedNameServers = sampleDnsResolverConfiguration.getNameServers();
        Collections.reverse(reversedNameServers);
        DnsResolverConfiguration result = new DnsResolverConfiguration();
        result.setNameServers(reversedNameServers);
        return result;
    }

    @Test
    public void testDnsResolverConfigurationNetworkAttachmentHasPrecedenceNetworkAttachmentOutOfSyncWithHost() {
        testDnsResolverConfiguration(sampleDnsResolverConfiguration,
                sampleDnsResolverConfiguration,
                sampleDnsResolverConfiguration2,
                false);
    }

    private void testDnsResolverConfiguration(DnsResolverConfiguration vdsDnsResolver,
            DnsResolverConfiguration networkDnsResolver,
            DnsResolverConfiguration attachmentDnsResolver,
            boolean expectedInSync) {
        iface.setIpv4DefaultRoute(true);
        //cannot use initIpv4ConfigurationBootProtocol because of 'randomized tests' technique.
        iface.setIpv4BootProtocol(Ipv4BootProtocol.DHCP);
        IPv4Address address = new IPv4Address();
        address.setBootProtocol(Ipv4BootProtocol.DHCP);
        testedNetworkAttachment.getIpConfiguration().setIPv4Addresses(Collections.singletonList(address));

        network.setDnsResolverConfiguration(networkDnsResolver);
        testedNetworkAttachment.setDnsResolverConfiguration(attachmentDnsResolver);
        assertThat(createTestedInstance(true, vdsDnsResolver).isNetworkInSync(), is(expectedInSync));
    }

    @Test
    public void testDnsResolverConfigurationNonDefaultRouteNetwork() {
        iface.setIpv4DefaultRoute(true);
        network.setDnsResolverConfiguration(sampleDnsResolverConfiguration);
        testedNetworkAttachment.setDnsResolverConfiguration(sampleDnsResolverConfiguration2);
        assertThat(createTestedInstance(false, sampleDnsResolverConfiguration).isNetworkInSync(), is(false));
    }

    @Test
    public void testDefaultRouteNonDefaultRouteNetwork() {
        iface.setIpv4DefaultRoute(true);
        network.setDnsResolverConfiguration(sampleDnsResolverConfiguration);
        assertThat(createTestedInstance(false, sampleDnsResolverConfiguration).isNetworkInSync(), is(false));
    }

    @Test
    public void testDefaultRouteWhenInSync() {
        iface.setIpv4DefaultRoute(true);
        network.setDnsResolverConfiguration(sampleDnsResolverConfiguration);
        assertThat(createTestedInstance(true, sampleDnsResolverConfiguration).isNetworkInSync(), is(true));
    }

    @Test
    public void testDefaultRouteWhenOutOfSync() {
        network.setDnsResolverConfiguration(sampleDnsResolverConfiguration);
        assertThat(createTestedInstance(true, sampleDnsResolverConfiguration).isNetworkInSync(), is(false));
    }

    @Test
    public void testDefaultRouteWhenOutOfSyncOnOlderCluster() {
        cluster.setCompatibilityVersion(Version.v4_1);
        network.setDnsResolverConfiguration(sampleDnsResolverConfiguration);
        assertThat(createTestedInstance(true, sampleDnsResolverConfiguration).isNetworkInSync(), is(true));
    }

    @Test
    public void testDnsResolverConfigurationInSyncWithHostWhenDhcpIsUsed() {
        iface.setIpv4DefaultRoute(false);
        network.setDnsResolverConfiguration(sampleDnsResolverConfiguration2);
        testedNetworkAttachment.setDnsResolverConfiguration(null);


        iface.setIpv4BootProtocol(Ipv4BootProtocol.DHCP);
        IPv4Address address = new IPv4Address();
        address.setBootProtocol(Ipv4BootProtocol.DHCP);
        testedNetworkAttachment.getIpConfiguration().setIPv4Addresses(Collections.singletonList(address));
        assertThat(createTestedInstance(true, sampleDnsResolverConfiguration).isNetworkInSync(), is(false));
    }

    private void initIpv4Configuration() {
        IpConfiguration ipConfiguration = this.testedNetworkAttachment.getIpConfiguration();
        ipConfiguration.setIPv4Addresses(Collections.singletonList(ipv4Address));

    }

    private void initIpv6Configuration() {
        IpConfiguration ipConfiguration = this.testedNetworkAttachment.getIpConfiguration();
        ipConfiguration.setIpV6Addresses(Collections.singletonList(ipv6Address));
    }

    private void initIpv4ConfigurationBootProtocol(boolean sameBootProtocol) {
        initIpv4Configuration();
        ipv4Address.setBootProtocol(IPV4_BOOT_PROTOCOL);
        Ipv4BootProtocol ifaceBootProtocol =
                sameBootProtocol ? IPV4_BOOT_PROTOCOL : Ipv4BootProtocol.forValue((IPV4_BOOT_PROTOCOL.getValue() + 1)
                        % Ipv4BootProtocol.values().length);
        iface.setIpv4BootProtocol(ifaceBootProtocol);
    }

    private void initIpv4ConfigurationBootProtocolAddress(Ipv4BootProtocol networkBootProtocol, boolean syncAddress) {
        initIpv4ConfigurationStaticBootProtocol(networkBootProtocol);
        ipv4Address.setAddress(IPV4_ADDRESS);
        iface.setIpv4Address(syncAddress ? IPV4_ADDRESS : null);
    }

    private void initIpv4ConfigurationBootProtocolNetmask(Ipv4BootProtocol networkBootProtocol, boolean syncNetmask) {
        initIpv4ConfigurationStaticBootProtocol(networkBootProtocol);
        ipv4Address.setNetmask(IPV4_NETMASK);
        iface.setIpv4Subnet(syncNetmask ? IPV4_NETMASK : null);
    }

    private void initIpv4ConfigurationBootProtocolGateway(Ipv4BootProtocol networkBootProtocol, boolean syncGateway) {
        initIpv4ConfigurationStaticBootProtocol(networkBootProtocol);
        ipv4Address.setGateway(IPV4_GATEWAY);
        iface.setIpv4Gateway(syncGateway ? IPV4_GATEWAY : null);
    }

    private void initIpv4ConfigurationStaticBootProtocol(Ipv4BootProtocol networkBootProtocol) {
        initIpv4Configuration();
        ipv4Address.setBootProtocol(networkBootProtocol);
        iface.setIpv4BootProtocol(networkBootProtocol);
    }

    private void initIpv6ConfigurationBootProtocol(boolean sameBootProtocol) {
        initIpv6Configuration();
        ipv6Address.setBootProtocol(IPV6_BOOT_PROTOCOL);
        Ipv6BootProtocol ifaceBootProtocol =
                sameBootProtocol ? IPV6_BOOT_PROTOCOL : Ipv6BootProtocol.forValue((IPV4_BOOT_PROTOCOL.getValue() + 1)
                        % Ipv4BootProtocol.values().length);
        iface.setIpv6BootProtocol(ifaceBootProtocol);
    }

    private void initIpv6ConfigurationBootProtocolAddress(Ipv6BootProtocol networkBootProtocol, boolean syncAddress) {
        initIpv6ConfigurationStaticBootProtocol(networkBootProtocol);
        ipv6Address.setAddress(IPV6_ADDRESS);
        iface.setIpv6Address(syncAddress ? IPV6_ADDRESS : null);
    }

    private void initIpv6ConfigurationBootProtocolPrefix(Ipv6BootProtocol networkBootProtocol, boolean syncPrefix) {
        initIpv6ConfigurationStaticBootProtocol(networkBootProtocol);
        ipv6Address.setPrefix(IPV6_PREFIX);
        iface.setIpv6Prefix(syncPrefix ? IPV6_PREFIX : null);
    }

    private void initIpv6ConfigurationBootProtocolGateway(Ipv6BootProtocol networkBootProtocol, boolean syncGateway) {
        initIpv6ConfigurationStaticBootProtocol(networkBootProtocol);
        ipv6Address.setGateway(IPV6_GATEWAY);
        iface.setIpv6Gateway(syncGateway ? IPV6_GATEWAY : null);
    }

    private void initIpv6ConfigurationStaticBootProtocol(Ipv6BootProtocol networkBootProtocol) {
        initIpv6Configuration();
        ipv6Address.setBootProtocol(networkBootProtocol);
        iface.setIpv6BootProtocol(networkBootProtocol);
    }

    private List<ReportedConfiguration> addReportedConfigurations(Stream<ReportedConfiguration> configurations, ReportedConfiguration ... toAdd) {
        return addReportedConfigurations(configurations.collect(Collectors.toList()), toAdd);
    }

    private List<ReportedConfiguration> addReportedConfigurations(List<ReportedConfiguration> configurations, ReportedConfiguration ... toAdd) {
        List<ReportedConfiguration> result = new ArrayList<>(configurations);
        result.addAll(Arrays.asList(toAdd));
        return result;
    }

    @SafeVarargs
    private final Stream<ReportedConfiguration> combineReportedConfigurations(List<ReportedConfiguration>... configurations) {
        Stream<List<ReportedConfiguration>> basicReportedConfigurations = Stream.of(configurations);
        return basicReportedConfigurations.flatMap(Collection::stream);
    }

    private List<ReportedConfiguration> createBasicReportedConfigurations() {
        return Arrays.asList(
                reportedEqualMtu(),
                reporteEqualBridged(),
                reportEqualVLAN(),
                reportEqualSwitchType()
        );
    }

    private List<ReportedConfiguration> createBasicAndQosReportedConfigurations() {
        return addReportedConfigurations(createBasicReportedConfigurations(),

                new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_LINK_SHARE),
                new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_REAL_TIME),
                new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_UPPER_LIMIT)
        );
    }

    private ReportedConfiguration reportEqualVLAN() {
        return new ReportedConfiguration(ReportedConfigurationType.VLAN,
                iface.getVlanId(),
                network.getVlanId(),
                true);
    }

    private ReportedConfiguration reportedEqualMtu() {
        return new ReportedConfiguration(
                ReportedConfigurationType.MTU,
                iface.getMtu(),
                network.getMtu(),
                true);
    }

    private ReportedConfiguration reportEqualSwitchType() {
        return new ReportedConfiguration(ReportedConfigurationType.SWITCH_TYPE,
                SwitchType.LEGACY,
                SwitchType.LEGACY,
                true);
    }

    private List<ReportedConfiguration> reportQos(boolean allInSync) {
        return Arrays.asList(new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_LINK_SHARE,
                        ifaceQos.getOutAverageLinkshare(),
                        null,
                        allInSync),
                new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_UPPER_LIMIT,
                        ifaceQos.getOutAverageUpperlimit(),
                        null,
                        allInSync),
                new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_REAL_TIME,
                        ifaceQos.getOutAverageRealtime(),
                        null,
                        allInSync));
    }

    private String addressesAsString(List<NameServer> nameServers) {
        if (nameServers == null) {
            return null;
        }
        return nameServers.stream().map(NameServer::getAddress).sorted().collect(Collectors.joining(","));
    }

    private ReportedConfiguration reporteEqualBridged() {
        return new ReportedConfiguration(ReportedConfigurationType.BRIDGED,
                iface.isBridged(),
                network.isVmNetwork(),
                true);
    }

}

package org.ovirt.engine.core.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.IpV6Address;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfiguration;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfigurationType;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfigurations;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.network.SwitchType;
import org.ovirt.engine.core.common.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
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

    @ClassRule
    public static final MockConfigRule mcr = new MockConfigRule(mockConfig(ConfigValues.DefaultMTU, 1500));

    @Before
    public void setUp() throws Exception {
        ifaceQos = new HostNetworkQos();
        networkQos = new HostNetworkQos();
        iface = new VdsNetworkInterface();
        //needed because network is vm network by default
        iface.setBridged(true);
        iface.setQos(ifaceQos);
        iface.setReportedSwitchType(SwitchType.LEGACY);

        network = new Network();

        testedNetworkAttachment = new NetworkAttachment();
        testedNetworkAttachment.setIpConfiguration(new IpConfiguration());

        cluster = new Cluster();
        cluster.setRequiredSwitchTypeForCluster(SwitchType.LEGACY);
    }

    @Test
    public void testIsNetworkInSyncWhenMtuDifferent() throws Exception {
        iface.setMtu(1);
        network.setMtu(2);
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenMtuSameViaDefault() throws Exception {
        NetworkInSyncWithVdsNetworkInterface testedInstanceWithSameNonQosValues =
                createTestedInstanceWithSameNonQosValues();

        iface.setMtu(DEFAULT_MTU_VALUE);
        network.setMtu(VALUE_DENOTING_THAT_MTU_SHOULD_BE_SET_TO_DEFAULT_VALUE);

        assertThat(testedInstanceWithSameNonQosValues.isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenVlanIdDifferent() throws Exception {
        iface.setMtu(1);
        network.setMtu(1);

        iface.setVlanId(1);
        network.setVlanId(2);
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenBridgedFlagDifferent() throws Exception {
        iface.setMtu(1);
        network.setMtu(1);

        iface.setVlanId(1);
        network.setVlanId(1);

        iface.setBridged(true);
        network.setVmNetwork(false);
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenIfaceQosEqual() throws Exception {
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIfaceQosIsNull() throws Exception {
        iface.setQos(null);
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenNetworkQosIsNull() throws Exception {
        networkQos = null;
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenBothQosIsNull() throws Exception {
        iface.setQos(null);
        networkQos = null;
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIfaceQosIsNullIfaceQosOverridden() throws Exception {
        iface.setQos(null);
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenNetworkQosIsNullIfaceQosOverridden() throws Exception {
        networkQos = null;
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenAverageLinkShareDifferent() throws Exception {
        ifaceQos.setOutAverageLinkshare(1);
        networkQos.setOutAverageLinkshare(2);
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenAverageUpperLimitDifferent() throws Exception {
        ifaceQos.setOutAverageUpperlimit(1);
        networkQos.setOutAverageUpperlimit(2);
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenAverageRealTimeDifferent() throws Exception {
        ifaceQos.setOutAverageRealtime(1);
        networkQos.setOutAverageRealtime(2);
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(false));
    }

    public NetworkInSyncWithVdsNetworkInterface createTestedInstanceWithSameNonQosValues() {
        iface.setMtu(1);
        network.setMtu(1);

        iface.setVlanId(1);
        network.setVlanId(1);

        iface.setBridged(true);
        network.setVmNetwork(true);
        return createTestedInstance();
    }

    public NetworkInSyncWithVdsNetworkInterface createTestedInstance() {
        return new NetworkInSyncWithVdsNetworkInterface(iface, network, networkQos, testedNetworkAttachment, cluster);
    }

    @Test
    public void testReportConfigurationsOnHost() throws Exception {
        NetworkInSyncWithVdsNetworkInterface testedInstanceWithSameNonQosValues =
                createTestedInstanceWithSameNonQosValues();
        ifaceQos.setOutAverageLinkshare(1);
        ifaceQos.setOutAverageUpperlimit(1);
        ifaceQos.setOutAverageRealtime(1);

        ReportedConfigurations reportedConfigurations = testedInstanceWithSameNonQosValues.reportConfigurationsOnHost();


        assertThat(reportedConfigurations.isNetworkInSync(), is(false));
        List<ReportedConfiguration> reportedConfigurationList = reportedConfigurations.getReportedConfigurationList();

        List<ReportedConfiguration> expectedReportedConfigurations = combineReportedConfigurations(
                createBasicReportedConfigurations(),

                new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_LINK_SHARE,
                        ifaceQos.getOutAverageLinkshare(),
                        null,
                        false),
                new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_UPPER_LIMIT,
                        ifaceQos.getOutAverageUpperlimit(),
                        null,
                        false),
                new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_REAL_TIME,
                        ifaceQos.getOutAverageRealtime(),
                        null,
                        false)
        );

        assertThat(reportedConfigurationList.containsAll(expectedReportedConfigurations), is(true));
        assertThat(reportedConfigurationList.size(), is(expectedReportedConfigurations.size()));
    }

    @Test
    public void testReportConfigurationsOnHostWhenSwitchTypeIsOutOfSync() throws Exception {
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
    public void testReportConfigurationsOnHostWhenIfaceQosIsNull() throws Exception {
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

        List<ReportedConfiguration> expectedReportedConfigurations = combineReportedConfigurations(
                createBasicReportedConfigurations(),

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
    public void testIsNetworkInSyncWhenIpConfigurationIsNull() throws Exception {
        this.testedNetworkAttachment.setIpConfiguration(null);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpConfigurationIsEmpty() throws Exception {
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv4BootProtocolEqual() throws Exception {
        initIpv4ConfigurationBootProtocol(true);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv4BootProtocolDifferent() throws Exception {
        initIpv4ConfigurationBootProtocol(false);
        iface.setIpv4BootProtocol(Ipv4BootProtocol.forValue(
                (IPV4_BOOT_PROTOCOL.getValue() + 1) % Ipv4BootProtocol.values().length));
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }


    @Test
    public void testIsNetworkInSyncWhenIpv4StaticBootProtocolAddressEqual() throws Exception {
        initIpv4ConfigurationBootProtocolAddress(IPV4_BOOT_PROTOCOL, true);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv4StaticBootProtocolAddressDifferent() throws Exception {
        initIpv4ConfigurationBootProtocolAddress(IPV4_BOOT_PROTOCOL, false);
        iface.setIpv4BootProtocol(Ipv4BootProtocol.forValue(
                (IPV4_BOOT_PROTOCOL.getValue() + 1) % Ipv4BootProtocol.values().length));
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv4StaticBootProtocolNetmaskEqual() throws Exception {
        initIpv4ConfigurationBootProtocolNetmask(IPV4_BOOT_PROTOCOL, true);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv4StaticBootProtocolNetmaskDifferent() throws Exception {
        initIpv4ConfigurationBootProtocolNetmask(IPV4_BOOT_PROTOCOL, false);
        iface.setIpv4BootProtocol(Ipv4BootProtocol.forValue(
                (IPV4_BOOT_PROTOCOL.getValue() + 1) % Ipv4BootProtocol.values().length));
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv4BootProtocolNotStaticAddressDifferent() throws Exception {
        initIpv4ConfigurationBootProtocolAddress(Ipv4BootProtocol.NONE, false);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv4BootProtocolNotStaticNetmaskDifferent() throws Exception {
        initIpv4ConfigurationBootProtocolNetmask(Ipv4BootProtocol.NONE, false);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv4GatewayEqual(){
        initIpv4ConfigurationBootProtocolGateway(Ipv4BootProtocol.STATIC_IP, true);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv4GatewayBothBlank() {
        List<String> blankValues = Arrays.asList(null, "");
        initIpv4ConfigurationStaticBootProtocol(Ipv4BootProtocol.STATIC_IP);
        int blankIndex = RandomUtils.instance().nextInt(2);
        ipv4Address.setGateway(blankValues.get(blankIndex));
        iface.setIpv4Gateway(blankValues.get(blankIndex ^ 1));
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

        List<ReportedConfiguration> expectedReportedConfigurations = combineReportedConfigurations(
                createBasicAndQosReportedConfigurations(),

                new ReportedConfiguration(ReportedConfigurationType.IPV4_BOOT_PROTOCOL,
                        iface.getIpv4BootProtocol().name(),
                        primaryAddress.getBootProtocol().name(),
                        true)
        );

        assertThat(reportedConfigurationList.containsAll(expectedReportedConfigurations), is(true));
        assertThat(reportedConfigurationList.size(), is(expectedReportedConfigurations.size()));
    }

    @Test
    public void testReportConfigurationsOnHostWhenIpv4BootProtocolStatic() {
        boolean syncAddress = RandomUtils.instance().nextBoolean();
        boolean syncNetmask = RandomUtils.instance().nextBoolean();
        boolean syncGateway = RandomUtils.instance().nextBoolean();
        initIpv4ConfigurationBootProtocolAddress(Ipv4BootProtocol.STATIC_IP, syncAddress);
        initIpv4ConfigurationBootProtocolNetmask(Ipv4BootProtocol.STATIC_IP, syncNetmask);
        initIpv4ConfigurationBootProtocolGateway(Ipv4BootProtocol.STATIC_IP, syncGateway);
        NetworkInSyncWithVdsNetworkInterface testedInstanceWithSameNonQosValues =
                createTestedInstanceWithSameNonQosValues();
        List<ReportedConfiguration> reportedConfigurationList =
                testedInstanceWithSameNonQosValues.reportConfigurationsOnHost().getReportedConfigurationList();
        IPv4Address primaryAddress = this.testedNetworkAttachment.getIpConfiguration().getIpv4PrimaryAddress();

        List<ReportedConfiguration> expectedReportedConfigurations = combineReportedConfigurations(
                createBasicAndQosReportedConfigurations(),

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
        assertThat(reportedConfigurationList.containsAll(expectedReportedConfigurations), is(true));
        assertThat(reportedConfigurationList.size(), is(expectedReportedConfigurations.size()));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv6BootProtocolEqual() {
        initIpv6ConfigurationBootProtocol(true);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv6StaticBootProtocolAddressEqual() throws Exception {
        initIpv6ConfigurationBootProtocolAddress(IPV6_BOOT_PROTOCOL, true);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv6StaticBootProtocolNetmaskEqual() throws Exception {
        initIpv6ConfigurationBootProtocolPrefix(IPV6_BOOT_PROTOCOL, true);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv6BootProtocolNotStaticAddressDifferent() throws Exception {
        initIpv6ConfigurationBootProtocolAddress(Ipv6BootProtocol.NONE, false);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv6BootProtocolNotStaticNetmaskDifferent() throws Exception {
        initIpv6ConfigurationBootProtocolPrefix(Ipv6BootProtocol.NONE, false);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv6GatewayEqual(){
        initIpv6ConfigurationBootProtocolGateway(Ipv6BootProtocol.STATIC_IP, true);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpv6GatewayBothBlank() {
        List<String> blankValues = Arrays.asList(null, "");
        initIpv6ConfigurationStaticBootProtocol(Ipv6BootProtocol.STATIC_IP);
        int blankIndex = RandomUtils.instance().nextInt(2);
        ipv6Address.setGateway(blankValues.get(blankIndex));
        iface.setIpv6Gateway(blankValues.get(blankIndex ^ 1));
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

    private List<ReportedConfiguration> combineReportedConfigurations(List<ReportedConfiguration> configurations, ReportedConfiguration ... toAdd) {
        List<ReportedConfiguration> result = new ArrayList<>(configurations);
        result.addAll(Arrays.asList(toAdd));
        return result;
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
        return combineReportedConfigurations(createBasicReportedConfigurations(),

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

    private ReportedConfiguration reporteEqualBridged() {
        return new ReportedConfiguration(ReportedConfigurationType.BRIDGED,
                iface.isBridged(),
                network.isVmNetwork(),
                true);
    }

}

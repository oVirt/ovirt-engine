package org.ovirt.engine.core.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfiguration;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfigurationType;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfigurations;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;

@RunWith(MockitoJUnitRunner.class)
public class NetworkInSyncWithVdsNetworkInterfaceTest {

    private static final int DEFAULT_MTU_VALUE = 1500;
    private static final int VALUE_DENOTING_THAT_MTU_SHOULD_BE_SET_TO_DEFAULT_VALUE = 0;
    private static final NetworkBootProtocol BOOT_PROTOCOL = NetworkBootProtocol.forValue(RandomUtils.instance().nextInt(NetworkBootProtocol.values().length));
    private static final String ADDRESS = "ADDRESS";
    private static final String NETMASK = "NETMASK";
    private VdsNetworkInterface iface;
    private Network network;
    private HostNetworkQos ifaceQos;
    private HostNetworkQos networkQos;
    @Mock
    private IpConfiguration mockedIpConfiguration;
    @Mock
    private IPv4Address mockedIPv4Address;

    @ClassRule
    public static final MockConfigRule mcr = new MockConfigRule(mockConfig(ConfigValues.DefaultMTU, 1500));

    @Before
    public void setUp() throws Exception {
        ifaceQos = new HostNetworkQos();
        networkQos = new HostNetworkQos();
        iface = new VdsNetworkInterface();
        //needed because network is vm network by default
        iface.setBridged(true);
        network = new Network();

        iface.setQos(ifaceQos);
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
        return new NetworkInSyncWithVdsNetworkInterface(iface, network, networkQos, mockedIpConfiguration);
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

        List<ReportedConfiguration> expectedReportedConfigurations = Arrays.asList(
                        new ReportedConfiguration(ReportedConfigurationType.MTU,
                                Integer.toString(iface.getMtu()),
                                Integer.toString(network.getMtu()),
                                true),
                        new ReportedConfiguration(ReportedConfigurationType.BRIDGED,
                                Boolean.toString(iface.isBridged()),
                                Boolean.toString(network.isVmNetwork()),
                                true),
                        new ReportedConfiguration(ReportedConfigurationType.VLAN,
                                Integer.toString(iface.getVlanId()),
                                Integer.toString(network.getVlanId()),
                                true),

                        new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_LINK_SHARE,
                                ifaceQos.getOutAverageLinkshare().toString(),
                                null,
                                false),
                        new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_UPPER_LIMIT,
                                ifaceQos.getOutAverageUpperlimit().toString(),
                                null,
                                false),
                        new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_REAL_TIME,
                                ifaceQos.getOutAverageRealtime().toString(),
                                null,
                                false)
        );

        assertThat(reportedConfigurationList.containsAll(expectedReportedConfigurations), is(true));
        assertThat(reportedConfigurationList.size(), is(6));
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

        List<ReportedConfiguration> expectedReportedConfigurations = Arrays.asList(
            new ReportedConfiguration(ReportedConfigurationType.MTU,
                Integer.toString(iface.getMtu()),
                Integer.toString(network.getMtu()),
                true),
            new ReportedConfiguration(ReportedConfigurationType.BRIDGED,
                Boolean.toString(iface.isBridged()),
                Boolean.toString(network.isVmNetwork()),
                true),
            new ReportedConfiguration(ReportedConfigurationType.VLAN,
                Integer.toString(iface.getVlanId()),
                Integer.toString(network.getVlanId()),
                true),
            new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_LINK_SHARE,
                null,
                networkQos.getOutAverageLinkshare().toString(), false),
            new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_REAL_TIME,
                null,
                networkQos.getOutAverageRealtime().toString(), false),
            new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_UPPER_LIMIT,
                null,
                networkQos.getOutAverageUpperlimit().toString(), false)
        );

        assertThat(reportedConfigurationList.containsAll(expectedReportedConfigurations), is(true));
        assertThat(reportedConfigurationList.size(), is(expectedReportedConfigurations.size()));
    }

    @Test
    public void testIsNetworkInSyncWhenIpConfigurationIsNull() throws Exception {
        mockedIpConfiguration = null;
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpConfigurationIsEmpty() throws Exception {
        when(mockedIpConfiguration.hasPrimaryAddressSet()).thenReturn(false);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenBootProtocolEqual() throws Exception {
        initIpConfigurationBootProtocol(true);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenBootProtocolDifferent() throws Exception {
        initIpConfigurationBootProtocol(false);
        iface.setBootProtocol(NetworkBootProtocol.forValue((BOOT_PROTOCOL.getValue() + 1)
                % NetworkBootProtocol.values().length));
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    private void initIpConfigurationBootProtocol(boolean sameBootProtocol) {
        initIpConfiguration();
        when(mockedIPv4Address.getBootProtocol()).thenReturn(BOOT_PROTOCOL);
        NetworkBootProtocol ifaceBootProtocol =
                sameBootProtocol ? BOOT_PROTOCOL : NetworkBootProtocol.forValue((BOOT_PROTOCOL.getValue() + 1)
                        % NetworkBootProtocol.values().length);
        iface.setBootProtocol(ifaceBootProtocol);
    }

    private void initIpConfiguration() {
        when(mockedIpConfiguration.hasPrimaryAddressSet()).thenReturn(true);
        when(mockedIpConfiguration.getPrimaryAddress()).thenReturn(mockedIPv4Address);
    }

    @Test
    public void testIsNetworkInSyncWhenStaticBootProtocolAddressEqual() throws Exception {
        initIpConfigurationBootProtocolAddress(BOOT_PROTOCOL, true);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenStaticBootProtocolAddressDifferent() throws Exception {
        initIpConfigurationBootProtocolAddress(BOOT_PROTOCOL, false);
        iface.setBootProtocol(NetworkBootProtocol.forValue((BOOT_PROTOCOL.getValue() + 1)
                % NetworkBootProtocol.values().length));
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenStaticBootProtocolNetmaskEqual() throws Exception {
        initIpConfigurationBootProtocolNetmask(BOOT_PROTOCOL, true);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenStaticBootProtocolNetmaskDifferent() throws Exception {
        initIpConfigurationBootProtocolNetmask(BOOT_PROTOCOL, false);
        iface.setBootProtocol(NetworkBootProtocol.forValue((BOOT_PROTOCOL.getValue() + 1)
                % NetworkBootProtocol.values().length));
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenBootProtocolNotStaticAddressDifferent() throws Exception {
        initIpConfigurationBootProtocolAddress(NetworkBootProtocol.NONE, false);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenBootProtocolNotStaticNetmaskDifferent() throws Exception {
        initIpConfigurationBootProtocolNetmask(NetworkBootProtocol.NONE, false);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testReportConfigurationsOnHostWhenBootProtocolNotStatic() {
        initIpConfigurationBootProtocolAddress(NetworkBootProtocol.NONE, false);
        initIpConfigurationBootProtocolNetmask(NetworkBootProtocol.NONE, false);
        NetworkInSyncWithVdsNetworkInterface testedInstanceWithSameNonQosValues =
                createTestedInstanceWithSameNonQosValues();
        List<ReportedConfiguration> reportedConfigurationList =
                testedInstanceWithSameNonQosValues.reportConfigurationsOnHost().getReportedConfigurationList();
        List<ReportedConfiguration> expectedReportedConfigurations = createDefaultExpectedReportedConfigurations();
        expectedReportedConfigurations.add(new ReportedConfiguration(ReportedConfigurationType.BOOT_PROTOCOL,
                iface.getBootProtocol().name(),
                mockedIpConfiguration.getPrimaryAddress().getBootProtocol().name(),
                true));
        assertThat(reportedConfigurationList.containsAll(expectedReportedConfigurations), is(true));
        assertThat(reportedConfigurationList.size(), is(expectedReportedConfigurations.size()));
    }

    @Test
    public void testReportConfigurationsOnHostWhenBootProtocolStatic() {
        boolean syncAddress = RandomUtils.instance().nextBoolean();
        boolean syncNetmask = RandomUtils.instance().nextBoolean();
        initIpConfigurationBootProtocolAddress(NetworkBootProtocol.STATIC_IP, syncAddress);
        initIpConfigurationBootProtocolNetmask(NetworkBootProtocol.STATIC_IP, syncNetmask);
        NetworkInSyncWithVdsNetworkInterface testedInstanceWithSameNonQosValues =
                createTestedInstanceWithSameNonQosValues();
        List<ReportedConfiguration> reportedConfigurationList =
                testedInstanceWithSameNonQosValues.reportConfigurationsOnHost().getReportedConfigurationList();
        List<ReportedConfiguration> expectedReportedConfigurations = createDefaultExpectedReportedConfigurations();
        expectedReportedConfigurations.add(new ReportedConfiguration(ReportedConfigurationType.BOOT_PROTOCOL,
                iface.getBootProtocol().name(),
                mockedIpConfiguration.getPrimaryAddress().getBootProtocol().name(),
                true));
        expectedReportedConfigurations.add(new ReportedConfiguration(ReportedConfigurationType.NETMASK,
                iface.getSubnet(),
                mockedIpConfiguration.getPrimaryAddress().getNetmask(),
                syncNetmask));
        expectedReportedConfigurations.add(new ReportedConfiguration(ReportedConfigurationType.IP_ADDRESS,
                iface.getAddress(),
                mockedIpConfiguration.getPrimaryAddress().getAddress(),
                syncAddress));
        assertThat(reportedConfigurationList.containsAll(expectedReportedConfigurations), is(true));
        assertThat(reportedConfigurationList.size(), is(expectedReportedConfigurations.size()));
    }

    private void initIpConfigurationBootProtocolAddress(NetworkBootProtocol networkBootProtocol, boolean syncAddress) {
        initIpConfigurationStaticBootProtocol(networkBootProtocol);
        when(mockedIPv4Address.getAddress()).thenReturn(ADDRESS);
        iface.setAddress(syncAddress ? ADDRESS : null);
    }

    private void initIpConfigurationBootProtocolNetmask(NetworkBootProtocol networkBootProtocol, boolean syncNetmask) {
        initIpConfigurationStaticBootProtocol(networkBootProtocol);
        when(mockedIPv4Address.getNetmask()).thenReturn(NETMASK);
        iface.setSubnet(syncNetmask ? NETMASK : null);
    }

    private void initIpConfigurationStaticBootProtocol(NetworkBootProtocol networkBootProtocol) {
        initIpConfiguration();
        when(mockedIPv4Address.getBootProtocol()).thenReturn(networkBootProtocol);
        iface.setBootProtocol(networkBootProtocol);

    }

    private List<ReportedConfiguration> createDefaultExpectedReportedConfigurations() {
        List<ReportedConfiguration> defaultExpectedReportedConfigurations = Arrays.asList(new ReportedConfiguration(
                ReportedConfigurationType.MTU,
                Integer.toString(iface.getMtu()),
                Integer.toString(network.getMtu()),
                true),
                new ReportedConfiguration(ReportedConfigurationType.BRIDGED,
                        Boolean.toString(iface.isBridged()),
                        Boolean.toString(network.isVmNetwork()),
                        true),
                new ReportedConfiguration(ReportedConfigurationType.VLAN,
                        Integer.toString(iface.getVlanId()),
                        Integer.toString(network.getVlanId()),
                        true),
                new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_LINK_SHARE, null, null, true),
                new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_REAL_TIME, null, null, true),
                new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_UPPER_LIMIT, null, null, true));
        return new LinkedList<>(defaultExpectedReportedConfigurations);
    }
}

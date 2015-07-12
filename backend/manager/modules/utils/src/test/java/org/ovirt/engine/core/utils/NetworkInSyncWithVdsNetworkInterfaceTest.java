package org.ovirt.engine.core.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfiguration;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfigurationType;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfigurations;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;

public class NetworkInSyncWithVdsNetworkInterfaceTest {

    private static final int DEFAULT_MTU_VALUE = 1500;
    private static final int VALUE_DENOTING_THAT_MTU_SHOULD_BE_SET_TO_DEFAULT_VALUE = 0;
    private VdsNetworkInterface iface;
    private Network network;
    private HostNetworkQos ifaceQos;
    private HostNetworkQos networkQos;

    @ClassRule
    public static final MockConfigRule mcr = new MockConfigRule(mockConfig(ConfigValues.DefaultMTU, 1500));

    @Before
    public void setUp() throws Exception {
        ifaceQos = new HostNetworkQos();
        networkQos = new HostNetworkQos();
        iface = new VdsNetworkInterface();
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
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenNetworkQosIsNull() throws Exception {
        networkQos = null;
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(false));
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
        iface.setQosOverridden(true);
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenNetworkQosIsNullIfaceQosOverridden() throws Exception {
        networkQos = null;
        iface.setQosOverridden(true);
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenAverageLinkShareDifferent() throws Exception {
        ifaceQos.setOutAverageLinkshare(1);
        networkQos.setOutAverageLinkshare(2);
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenAverageLinkShareDifferentIfaceQosOverridden() throws Exception {
        ifaceQos.setOutAverageLinkshare(1);
        networkQos.setOutAverageLinkshare(2);
        iface.setQosOverridden(true);
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenAverageUpperLimitDifferent() throws Exception {
        ifaceQos.setOutAverageUpperlimit(1);
        networkQos.setOutAverageUpperlimit(2);
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenAverageUpperLimitDifferentIfaceQosOverridden() throws Exception {
        ifaceQos.setOutAverageUpperlimit(1);
        networkQos.setOutAverageUpperlimit(2);
        iface.setQosOverridden(true);
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenAverageRealTimeDifferent() throws Exception {
        ifaceQos.setOutAverageRealtime(1);
        networkQos.setOutAverageRealtime(2);
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenAverageRealTimeDifferentIfaceQosOverridden() throws Exception {
        ifaceQos.setOutAverageRealtime(1);
        networkQos.setOutAverageRealtime(2);
        iface.setQosOverridden(true);
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(true));
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
        return new NetworkInSyncWithVdsNetworkInterface(iface, network, networkQos);
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
                new ReportedConfiguration(ReportedConfigurationType.MTU, Integer.toString(iface.getMtu()), true),
                new ReportedConfiguration(ReportedConfigurationType.BRIDGED, Boolean.toString(iface.isBridged()), true),
                new ReportedConfiguration(ReportedConfigurationType.VLAN, Integer.toString(iface.getVlanId()), true),

                new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_LINK_SHARE, ifaceQos.getOutAverageLinkshare().toString(), false),
                new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_UPPER_LIMIT, ifaceQos.getOutAverageUpperlimit().toString(), false),
                new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_REAL_TIME, ifaceQos.getOutAverageRealtime().toString(), false)
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
                new ReportedConfiguration(ReportedConfigurationType.MTU, Integer.toString(iface.getMtu()), true),
                new ReportedConfiguration(ReportedConfigurationType.BRIDGED, Boolean.toString(iface.isBridged()), true),
                new ReportedConfiguration(ReportedConfigurationType.VLAN, Integer.toString(iface.getVlanId()), true)
        );

        assertThat(reportedConfigurationList.containsAll(expectedReportedConfigurations), is(true));
        assertThat(reportedConfigurationList.size(), is(3));
    }
}

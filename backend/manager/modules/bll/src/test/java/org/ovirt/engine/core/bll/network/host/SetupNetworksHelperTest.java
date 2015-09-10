package org.ovirt.engine.core.bll.network.host;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.InjectorRule;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.bll.validator.network.NetworkExclusivenessValidator;
import org.ovirt.engine.core.bll.validator.network.NetworkExclusivenessValidatorResolver;
import org.ovirt.engine.core.bll.validator.network.NetworkType;
import org.ovirt.engine.core.common.action.CustomPropertiesForVdsNetworkInterface;
import org.ovirt.engine.core.common.action.SetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.vdsbroker.CalculateBaseNic;
import org.ovirt.engine.core.vdsbroker.EffectiveHostNetworkQos;
import org.ovirt.engine.core.vdsbroker.NetworkImplementationDetailsUtils;

@RunWith(MockitoJUnitRunner.class)
public class SetupNetworksHelperTest {

    private static final String BOND_NAME = "bond0";
    private static final String MANAGEMENT_NETWORK_NAME = "management";
    private static final String BOND_MODE_4 = "mode=4 miimon=100";
    private static final int DEFAULT_MTU = 1500;
    private static final int DEFAULT_SPEED = 1000;
    private static final String LIST_SUFFIX = "_LIST";
    private static final EngineMessage NETWORK_INTERFACES_NOT_EXCLUSIVELY_USED_BY_NETWORK_MSG =
            EngineMessage.NETWORK_INTERFACES_NOT_EXCLUSIVELY_USED_BY_NETWORK;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.MultipleGatewaysSupported, Version.v3_3.toString(), true),
            mockConfig(ConfigValues.MultipleGatewaysSupported, Version.v3_2.toString(), false),
            mockConfig(ConfigValues.HostNetworkQosSupported, Version.v3_2.toString(), false),
            mockConfig(ConfigValues.HostNetworkQosSupported, Version.v3_3.toString(), false),
            mockConfig(ConfigValues.HostNetworkQosSupported, Version.v3_4.toString(), true),
            mockConfig(ConfigValues.HostNetworkQosSupported, Version.v3_5.toString(), true),
            mockConfig(ConfigValues.NetworkCustomPropertiesSupported, Version.v3_3.toString(), false),
            mockConfig(ConfigValues.NetworkCustomPropertiesSupported, Version.v3_4.toString(), false),
            mockConfig(ConfigValues.NetworkCustomPropertiesSupported, Version.v3_5.toString(), true),
            mockConfig(ConfigValues.PreDefinedNetworkCustomProperties, Version.v3_2.toString(), ""),
            mockConfig(ConfigValues.PreDefinedNetworkCustomProperties, Version.v3_3.toString(), ""),
            mockConfig(ConfigValues.PreDefinedNetworkCustomProperties, Version.v3_4.toString(), ""),
            mockConfig(ConfigValues.PreDefinedNetworkCustomProperties, Version.v3_5.toString(), ""),
            mockConfig(ConfigValues.UserDefinedNetworkCustomProperties, Version.v3_2.toString(), ""),
            mockConfig(ConfigValues.UserDefinedNetworkCustomProperties, Version.v3_3.toString(), ""),
            mockConfig(ConfigValues.UserDefinedNetworkCustomProperties, Version.v3_4.toString(), ""),
            mockConfig(ConfigValues.UserDefinedNetworkCustomProperties,
                    Version.v3_5.toString(),
                    "bridge_opts=^[^\\s=]+=[^\\s=]+(\\s+[^\\s=]+=[^\\s=]+)*$"),
            mockConfig(ConfigValues.DefaultMTU, DEFAULT_MTU));
    private final CustomPropertiesForVdsNetworkInterface customProperties = new CustomPropertiesForVdsNetworkInterface();

    @Rule
    public InjectorRule injectorRule = new InjectorRule();

    @Mock
    private NetworkDao networkDao;

    @Mock
    private VmInterfaceManager vmInterfaceManager;

    @Mock
    private InterfaceDao interfaceDao;

    @Mock
    private HostNetworkQosDao qosDao;

    @Mock
    private GlusterBrickDao brickDao;

    @Mock
    private ManagementNetworkUtil managementNetworkUtil;

    @Mock
    private NetworkExclusivenessValidatorResolver networkExclusivenessValidatorResolver;

    @Mock
    private NetworkExclusivenessValidator networkExclusivenessValidator;

    @Mock
    private NetworkAttachmentDao networkAttachmentDao;

    private EffectiveHostNetworkQos effectiveHostNetworkQos;

    @Mock
    private CalculateBaseNic calculateBaseNic;

    @Spy
    private NetworkImplementationDetailsUtils networkImplementationDetailsUtilsSpy =
        new NetworkImplementationDetailsUtils(effectiveHostNetworkQos, networkAttachmentDao, calculateBaseNic);

    /* --- Tests for networks functionality --- */

    @Before
    public void setUp() throws Exception {
        effectiveHostNetworkQos = new EffectiveHostNetworkQos(qosDao);
        injectorRule.bind(NetworkDao.class, networkDao);
        injectorRule.bind(NetworkAttachmentDao.class, networkAttachmentDao);
        injectorRule.bind(InterfaceDao.class, interfaceDao);
        injectorRule.bind(GlusterBrickDao.class, brickDao);
        injectorRule.bind(EffectiveHostNetworkQos.class, effectiveHostNetworkQos);
        injectorRule.bind(NetworkImplementationDetailsUtils.class,
            new NetworkImplementationDetailsUtils(effectiveHostNetworkQos, networkAttachmentDao, calculateBaseNic));
        injectorRule.bind(CalculateBaseNic.class, calculateBaseNic);
    }

    @Test
    public void networkDidntChange() {
        Network network = createNetwork("network");
        VdsNetworkInterface nic = createNic("nic0", "network");
        mockExistingIfaces(nic);
        mockExistingNetworks(network);

        SetupNetworksHelper helper = createHelper(createParametersForNics(nic));

        validateAndAssertNoChanges(helper);
    }

    @Test
    public void unmanagedNetworkAddedToNic() {
        VdsNetworkInterface nic = createNic("nic0", null);
        mockExistingIfaces(nic);
        nic.setNetworkName("net");

        SetupNetworksHelper helper = createHelper(createParametersForNics(nic));

        validateAndExpectViolation(helper, EngineMessage.NETWORKS_DONT_EXIST_IN_CLUSTER, nic.getNetworkName());
    }

    @Test
    public void managedNetworkAddedToNic() {
        Network net = createNetwork("net");
        VdsNetworkInterface nic = createNic("nic0", null);

        mockExistingNetworks(net);
        mockExistingIfaces(nic);

        nic.setNetworkName(net.getName());

        SetupNetworksHelper helper = createHelper(createParametersForNics(nic));

        validateAndAssertNetworkModified(helper, net);
    }

    @Test
    public void networkRemovedFromNic() {
        String networkName = "net";
        VdsNetworkInterface nic = createNic("nic0", networkName);
        mockExistingIfaces(nic);
        nic.setNetworkName(null);

        SetupNetworksHelper helper = createHelper(createParametersForNics(nic));

        validateAndExpectNoViolations(helper);
        assertNoBondsModified(helper);
        assertNoNetworksModified(helper);
        assertNetworkRemoved(helper, networkName);
        assertNoBondsRemoved(helper);
    }

    @Test
    public void networkMovedFromNicToNic() {
        Network net = createNetwork("net");
        VdsNetworkInterface nic1 = createNicSyncedWithNetwork("nic0", net);
        VdsNetworkInterface nic2 = createNic("nic1", null);

        mockExistingNetworks(net);
        mockExistingIfaces(nic1, nic2);

        nic2.setNetworkName(net.getName());
        nic1.setNetworkName(null);
        SetupNetworksHelper helper = createHelper(createParametersForNics(nic1, nic2));

        validateAndExpectNoViolations(helper);
        assertNoBondsModified(helper);
        assertNetworkModified(helper, net);
        assertNoNetworksRemoved(helper);
        assertNoBondsRemoved(helper);
    }

    @Test
    public void networkReplacedOnNic() {
        Network net = createNetwork("net");
        Network newNet = createNetwork("net2");
        mockExistingNetworks(net, newNet);
        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net);
        mockExistingIfaces(nic);
        nic.setNetworkName(newNet.getName());

        SetupNetworksHelper helper = createHelper(createParametersForNics(nic));

        validateAndExpectNoViolations(helper);
        assertNoBondsModified(helper);
        assertNetworkModified(helper, newNet);
        assertNetworkRemoved(helper, net.getName());
        assertNoBondsRemoved(helper);
    }

    @Test
    public void bootProtocolChanged() {
        Network net = createNetwork("net");
        mockExistingNetworks(net);
        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net);
        nic.setBootProtocol(NetworkBootProtocol.NONE);
        mockExistingIfaces(nic);
        nic.setBootProtocol(NetworkBootProtocol.DHCP);

        SetupNetworksHelper helper = createHelper(createParametersForNics(nic));

        validateAndAssertNetworkModified(helper, net);
    }

    @Test
    public void gatewayChanged() {
        Network net = createNetwork("otherThenMgmtNetwork");
        mockExistingNetworks(net);
        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net);
        nic.setBootProtocol(NetworkBootProtocol.STATIC_IP);
        nic.setGateway(RandomUtils.instance().nextString(10));
        mockExistingIfaces(nic);
        nic.setGateway(RandomUtils.instance().nextString(10));

        VDS vds = mock(VDS.class);
        when(vds.getId()).thenReturn(Guid.Empty);
        when(vds.getVdsGroupCompatibilityVersion()).thenReturn(Version.v3_3);
        when(vds.getHostName()).thenReturn(RandomUtils.instance().nextString(10));

        SetupNetworksHelper helper = createHelper(createParametersForNics(nic), vds);

        validateAndAssertNetworkModified(helper, net);
    }

    @Test
    public void unsupportedGatewayChanged() {
        Network net = createNetwork("otherThenMgmtNetwork");
        mockExistingNetworks(net);
        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net);
        nic.setBootProtocol(NetworkBootProtocol.STATIC_IP);
        nic.setGateway(RandomUtils.instance().nextString(10));
        mockExistingIfaces(nic);
        nic.setGateway(RandomUtils.instance().nextString(10));

        VDS vds = mock(VDS.class);
        when(vds.getId()).thenReturn(Guid.Empty);

        SetupNetworksHelper helper = createHelper(createParametersForNics(nic), vds);
        when(vds.getVdsGroupCompatibilityVersion()).thenReturn(Version.v3_2);

        validateAndExpectViolation(helper, EngineMessage.NETWORK_ATTACH_ILLEGAL_GATEWAY, nic.getNetworkName());
    }

    @Test
    public void subnetChanged() {
        Network net = createNetwork("net");
        mockExistingNetworks(net);
        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net);
        nic.setBootProtocol(NetworkBootProtocol.STATIC_IP);
        nic.setSubnet(RandomUtils.instance().nextString(10));
        mockExistingIfaces(nic);
        nic.setSubnet(RandomUtils.instance().nextString(10));

        VDS vds = mock(VDS.class);
        when(vds.getHostName()).thenReturn(RandomUtils.instance().nextString(10));

        SetupNetworksHelper helper = createHelper(createParametersForNics(nic), vds);

        validateAndAssertNetworkModified(helper, net);
    }

    @Test
    public void ipChanged() {
        Network net = createNetwork("net");
        mockExistingNetworks(net);
        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net);
        nic.setBootProtocol(NetworkBootProtocol.STATIC_IP);
        nic.setAddress(RandomUtils.instance().nextString(10));
        mockExistingIfaces(nic);
        nic.setAddress(RandomUtils.instance().nextString(10));

        VDS vds = mock(VDS.class);
        when(vds.getHostName()).thenReturn(RandomUtils.instance().nextString(10));

        SetupNetworksHelper helper = createHelper(createParametersForNics(nic), vds);

        validateAndAssertNetworkModified(helper, net);
    }

    @Test
    public void ipChangedWhenEqualToHostname() {
        String hostName = "1.1.1.1";

        Network net = createNetwork("net");
        mockExistingNetworks(net);
        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net);
        nic.setBootProtocol(NetworkBootProtocol.STATIC_IP);
        nic.setAddress(hostName);
        mockExistingIfaces(nic);
        nic.setAddress(RandomUtils.instance().nextString(10));

        VDS vds = mock(VDS.class);
        when(vds.getHostName()).thenReturn(hostName);

        SetupNetworksHelper helper = createHelper(createParametersForNics(nic), vds);

        validateAndExpectViolation(helper,
            EngineMessage.ACTION_TYPE_FAILED_NETWORK_ADDRESS_CANNOT_BE_CHANGED);
    }

    @Test
    public void managementNetworkChangedCorrectly() {
        Network net = createManagementNetwork();
        mockExistingNetworks(net);
        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net);
        nic.setBootProtocol(NetworkBootProtocol.STATIC_IP);
        nic.setAddress(RandomUtils.instance().nextString(10));
        mockExistingIfaces(nic);
        nic.setAddress(RandomUtils.instance().nextString(10));

        VDS vds = mock(VDS.class);
        when(vds.getId()).thenReturn(Guid.Empty);
        when(vds.getHostName()).thenReturn(RandomUtils.instance().nextString(10));
        SetupNetworksHelper helper = createHelper(createParametersForNics(nic), vds);

        validateAndAssertNetworkModified(helper, net);
    }

    @Test
    public void managementNetworkChangedCorrectlyWhenDhcpSet() {
        Network net = createManagementNetwork();
        mockExistingNetworks(net);
        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net);
        nic.setBootProtocol(NetworkBootProtocol.STATIC_IP);
        nic.setAddress(RandomUtils.instance().nextString(10));
        mockExistingIfaces(nic);
        nic.setBootProtocol(NetworkBootProtocol.DHCP);
        nic.setAddress(RandomUtils.instance().nextString(10));

        VDS vds = mock(VDS.class);
        when(vds.getId()).thenReturn(Guid.Empty);
        when(vds.getHostName()).thenReturn(RandomUtils.instance().nextString(10));
        SetupNetworksHelper helper = createHelper(createParametersForNics(nic), vds);

        validateAndAssertNetworkModified(helper, net);
    }

    public void managementNetworkChangedCorrectlyWithIpHostname() {
        Network net = createManagementNetwork();
        mockExistingNetworks(net);
        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net);
        nic.setBootProtocol(NetworkBootProtocol.STATIC_IP);
        nic.setAddress(RandomUtils.instance().nextString(10));
        mockExistingIfaces(nic);
        nic.setAddress(RandomUtils.instance().nextString(10));

        VDS vds = mock(VDS.class);
        when(vds.getId()).thenReturn(Guid.Empty);
        when(vds.getHostName()).thenReturn("1.1.1.1");
        SetupNetworksHelper helper = createHelper(createParametersForNics(nic), vds);

        validateAndAssertNetworkModified(helper, net);
    }

    @Test
    public void managementNetworkChangedIncorrectly() {
        String hostName = "1.1.1.1";

        Network net = createManagementNetwork();
        mockExistingNetworks(net);
        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net);
        nic.setBootProtocol(NetworkBootProtocol.STATIC_IP);
        nic.setAddress(hostName);
        mockExistingIfaces(nic);
        nic.setAddress(RandomUtils.instance().nextString(10));

        VDS vds = mock(VDS.class);
        when(vds.getId()).thenReturn(Guid.Empty);
        when(vds.getHostName()).thenReturn(hostName);
        SetupNetworksHelper helper = createHelper(createParametersForNics(nic), vds);

        validateAndExpectViolation(helper,
            EngineMessage.ACTION_TYPE_FAILED_NETWORK_ADDRESS_CANNOT_BE_CHANGED);
    }

    private SetupNetworksHelper qosValuesTest(Network network, HostNetworkQos qos) {
        mockExistingNetworks(network);
        VdsNetworkInterface iface = createNicSyncedWithNetwork("eth0", network);
        mockExistingIfaces(iface);

        iface.setQos(qos);

        return createHelper(createParametersForNics(iface), Version.v3_4);
    }

    @Test
    public void qosValuesModified() {
        Network network = createNetwork(MANAGEMENT_NETWORK_NAME);
        SetupNetworksHelper helper = qosValuesTest(network, createQos());
        validateAndAssertNetworkModified(helper, network);
    }

    private SetupNetworksHelper setupCompositeQosConfiguration(boolean constructBond,
            boolean qosOnAll,
            HostNetworkQos qos,
            Integer slaveSpeed,
            Integer masterSpeed,
            String bondOptions) {

        Network net1 = createNetwork("net1");
        net1.setVlanId(100);
        Network net2 = createNetwork("net2");
        net2.setVlanId(200);
        Network net3 = createNetwork("net3");
        net3.setVmNetwork(false);

        VdsNetworkInterface master = createNic(BOND_NAME, net3.getName());
        master.setSpeed(masterSpeed);
        VdsNetworkInterface vlan1 = createVlan(master, net1.getVlanId(), net1.getName());
        VdsNetworkInterface vlan2 = createVlan(master, net2.getVlanId(), net2.getName());

        vlan1.setQos(qos);

        Guid qosId = Guid.newGuid();
        when(qosDao.get(qosId)).thenReturn(qos);

        NetworkAttachment networkAttachment = createNetworkAttachment(net1, master.getId(), vlan1);

        when(networkAttachmentDao
                .getNetworkAttachmentByNicIdAndNetworkId(master.getId(), net1.getId()))
                .thenReturn(networkAttachment);

        net2.setQosId(qosId);
        net3.setQosId(qosOnAll ? qosId : null);

        mockExistingNetworks(net1, net2, net3);

        if (constructBond) {
            master.setBonded(true);
            VdsNetworkInterface slave1 = createNic("nic1", null);
            VdsNetworkInterface slave2 = createNic("nic2", null);
            slave2.setSpeed(slaveSpeed);
            master.setBondOptions(bondOptions);
            mockExistingIfaces(master, slave1, slave2, vlan1);
            slave1.setBondName(master.getName());
            slave2.setBondName(master.getName());
            return createHelper(createParametersForNics(master, slave1, slave2, vlan1, vlan2), Version.v3_4);
        } else {
            mockExistingIfaces(master, vlan1);
            return createHelper(createParametersForNics(master, vlan1, vlan2), Version.v3_4);
        }
    }

    private SetupNetworksHelper setupCompositeQosConfiguration(boolean constructBond,
            boolean qosOnAll,
            HostNetworkQos qos,
            Integer nicSpeed) {
        return setupCompositeQosConfiguration(constructBond, qosOnAll, qos, null, nicSpeed, null);
    }

    private NetworkAttachment createNetworkAttachment(Network network,
            Guid baseNicId,
            VdsNetworkInterface nic) {
        NetworkAttachment networkAttachment = new NetworkAttachment();

        networkAttachment.setNicId(baseNicId);
        networkAttachment.setNetworkId(network.getId());

        networkAttachment.setHostNetworkQos(nic.getQos());

        IpConfiguration ipConfiguration = new IpConfiguration();
        networkAttachment.setIpConfiguration(ipConfiguration);
        IPv4Address ipv4Address = new IPv4Address();
        ipConfiguration.setIPv4Addresses(Collections.singletonList(ipv4Address));
        ipv4Address.setAddress(nic.getAddress());
        ipv4Address.setBootProtocol(nic.getBootProtocol());
        ipv4Address.setGateway(nic.getGateway());
        ipv4Address.setNetmask(nic.getSubnet());

        return networkAttachment;
    }

    private SetupNetworksHelper setupCompositeQosConfiguration(boolean qosOnAll,
            HostNetworkQos qos,
            Integer slaveSpeed,
            Integer bondSpeed,
            String bondOptions) {
        return setupCompositeQosConfiguration(true, qosOnAll, qos, slaveSpeed, bondSpeed, bondOptions);
    }

    private SetupNetworksHelper setupCompositeQosConfiguration(boolean qosOnAll) {
        return setupCompositeQosConfiguration(qosOnAll, createQos(), null, DEFAULT_SPEED, BOND_MODE_4);
    }

    @Test
    public void qosConfiguredOnAllNetworks() {
        SetupNetworksHelper helper = setupCompositeQosConfiguration(true);
        validateAndExpectNoViolations(helper);
    }

    @Test
    public void qosNotConfiguredOnAllNetworks() {
        SetupNetworksHelper helper = setupCompositeQosConfiguration(false);
        validateAndExpectViolation(helper,
            EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_INTERFACES_WITHOUT_QOS,
            BOND_NAME);
    }

    private SetupNetworksHelper qosCommitmentSetup(boolean constructBond,
            int totalCommitment,
            Integer slaveSpeed,
            Integer masterSpeed,
            String bondOptions) {

        HostNetworkQos qos = new HostNetworkQos();
        qos.setOutAverageLinkshare(10);
        qos.setOutAverageRealtime(totalCommitment / 3);
        return constructBond ? setupCompositeQosConfiguration(true, qos, slaveSpeed, masterSpeed, bondOptions)
                : setupCompositeQosConfiguration(false, true, qos, masterSpeed);
    }

    @Test
    public void customPropertiesNotSupported() {
        Network network = createManagementNetwork();
        mockExistingNetworks(network);
        VdsNetworkInterface iface = createNicSyncedWithNetwork("eth0", network);
        mockExistingIfaces(iface);
        customProperties.add(iface, createCustomProperties());
        customProperties.add(iface, createCustomProperties());

        SetupNetworksHelper helper = createHelper(createParametersForNics(iface));

        validateAndExpectViolation(helper,
            EngineMessage.ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_NOT_SUPPORTED,
            network.getName());
    }

    @Test
    public void customPropertiesNoNetwork() {
        mockExistingNetworks();
        VdsNetworkInterface iface = createNic("eth0", null);
        mockExistingIfaces(iface);
        customProperties.add(iface, createCustomProperties());

        SetupNetworksHelper helper = createHelper(createParametersForNics(iface), Version.v3_5);

        validateAndAssertNoChanges(helper);
    }

    @Test
    public void customPropertiesNetworkRemoved() {
        mockExistingNetworks();
        VdsNetworkInterface iface = createManagementNetworkNic("eth0");
        customProperties.add(iface, createCustomProperties());
        mockExistingIfaces(iface);
        iface.setNetworkName(null);

        SetupNetworksHelper helper = createHelper(createParametersForNics(iface), Version.v3_5);

        validateAndAssertNetworkRemoved(helper, MANAGEMENT_NETWORK_NAME);
    }

    @Test
    public void customPropertiesBadInput() {
        Network network = createManagementNetwork();
        mockExistingNetworks(network);
        VdsNetworkInterface iface = createNicSyncedWithNetwork("eth0", network);
        mockExistingIfaces(iface);
        Map<String, String> customProperties = new HashMap<>();
        customProperties.put("foo", "b@r");
        this.customProperties.add(iface, customProperties);

        SetupNetworksHelper helper = createHelper(createParametersForNics(iface), Version.v3_5);

        validateAndExpectViolation(helper,
            EngineMessage.ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_BAD_INPUT,
            network.getName());
    }

    @Test
    public void customPropertiesModified() {
        Network network = createManagementNetwork();
        mockExistingNetworks(network);
        VdsNetworkInterface iface = createNicSyncedWithNetwork("eth0", network);
        mockExistingIfaces(iface);
        customProperties.add(iface, createCustomProperties());

        SetupNetworksHelper helper = createHelper(createParametersForNics(iface), Version.v3_5);

        validateAndExpectNoViolations(helper);
        assertNoBondsModified(helper);
        assertNetworkModified(helper, network);
        assertNoNetworksRemoved(helper);
        assertNoBondsRemoved(helper);
        assertNoInterfacesModified(helper);
    }

    @Test
    public void bridgePropertiesNonVm() {
        Network network = createManagementNetwork();
        network.setVmNetwork(false);
        mockExistingNetworks(network);
        VdsNetworkInterface iface = createNicSyncedWithNetwork("eth0", network);
        mockExistingIfaces(iface);
        customProperties.add(iface, createCustomProperties());

        SetupNetworksHelper helper = createHelper(createParametersForNics(iface), Version.v3_5);

        validateAndExpectViolation(helper,
            EngineMessage.ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_BAD_INPUT,
            MANAGEMENT_NETWORK_NAME);
    }

    /* --- Tests for external networks --- */

    @Test
    public void externalNetworkAttached() {
        Network net = createNetwork("net");
        net.setProvidedBy(new ProviderNetwork());
        mockExistingNetworks(net);
        VdsNetworkInterface nic = createNic("nic0", null);
        mockExistingIfaces(nic);

        nic.setNetworkName(net.getName());
        SetupNetworksHelper helper = createHelper(createParametersForNics(nic));

        validateAndExpectViolation(helper,
            EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_NETWORKS_CANNOT_BE_PROVISIONED);
    }

    /* --- Tests for sync network functionality --- */

    @Test
    public void dontSyncNetworkAlreadyInSync() {
        Network net = createNetwork("net");
        mockExistingNetworks(net);
        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net);
        mockExistingIfaces(nic);

        SetupNetworksHelper helper = createHelper(createParametersForSync(nic));

        validateAndAssertNoChanges(helper);
    }

    @Test
    public void unmanagedNetworkNotSynced() {
        VdsNetworkInterface nic = createNic("nic0", "net");
        mockExistingIfaces(nic);

        SetupNetworksHelper helper = createHelper(createParametersForSync(nic));

        validateAndExpectNoViolations(helper);
        assertTrue(helper.getNetworks().isEmpty());
    }

    @Test
    public void unsyncedNetworkRemoved() {
        Network net = createNetwork("net");
        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net);
        nic.setBridged(!net.isVmNetwork());

        mockExistingNetworks(net);
        mockExistingIfaces(nic);

        nic.setNetworkName(null);
        SetupNetworksHelper helper = createHelper(createParametersForNics(nic));

        validateAndExpectNoViolations(helper);
        assertNoBondsModified(helper);
        assertNoNetworksModified(helper);
        assertNetworkRemoved(helper, net.getName());
        assertNoBondsRemoved(helper);
    }

    @Test
    public void unsyncedNetworkModified() {
        Network net = createNetwork("net");
        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net);
        nic.setBridged(!net.isVmNetwork());
        nic.setBootProtocol(NetworkBootProtocol.NONE);

        mockExistingNetworks(net);
        mockExistingIfaces(nic);

        nic.setBootProtocol(NetworkBootProtocol.DHCP);
        SetupNetworksHelper helper = createHelper(createParametersForNics(nic));

        validateAndExpectViolation(helper, EngineMessage.NETWORKS_NOT_IN_SYNC, net.getName());
    }

    @Test
    public void unsyncedNetworkNotModified() {
        Network net = createNetwork("net");
        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net);
        nic.setBridged(!net.isVmNetwork());

        mockExistingNetworks(net);
        mockExistingIfaces(nic);

        SetupNetworksHelper helper = createHelper(createParametersForNics(nic));

        validateAndAssertNoChanges(helper);
    }

    @Test
    public void unsyncedNetworkMovedToAnotherNic() {
        Network net = createNetwork("net");
        VdsNetworkInterface nic1 = createNicSyncedWithNetwork("nic0", net);
        nic1.setBridged(!net.isVmNetwork());
        VdsNetworkInterface nic2 = createNic("nic1", null);

        mockExistingNetworks(net);
        mockExistingIfaces(nic1, nic2);

        nic2.setNetworkName(nic1.getNetworkName());
        nic1.setNetworkName(null);
        SetupNetworksHelper helper = createHelper(createParametersForNics(nic1, nic2));

        validateAndExpectViolation(helper, EngineMessage.NETWORKS_NOT_IN_SYNC, net.getName());
    }

    @Test
    public void syncNetworkQosNotSupported() {
        Network network = createManagementNetwork();
        mockExistingNetworks(network);
        VdsNetworkInterface iface = createNicSyncedWithNetwork("eth0", network);
        mockExistingIfaces(iface);

        Guid qosId = Guid.newGuid();
        HostNetworkQos qos = createQos();
        when(qosDao.get(qosId)).thenReturn(qos);
        network.setQosId(qosId);

        SetupNetworksHelper helper = createHelper(createParametersForSync(iface));

        validateAndExpectViolation(helper,
            EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_NOT_SUPPORTED,
            MANAGEMENT_NETWORK_NAME);
    }

    @Test
    public void networkToSyncMovedToAnotherNic() {
        Network net = createNetwork("net");
        VdsNetworkInterface nic1 = createNicSyncedWithNetwork("nic0", net);
        nic1.setBridged(!net.isVmNetwork());
        VdsNetworkInterface nic2 = createNic("nic1", null);

        mockExistingNetworks(net);
        mockExistingIfaces(nic1, nic2);

        nic2.setNetworkName(nic1.getNetworkName());
        nic1.setNetworkName(null);
        SetupNetworksHelper helper = createHelper(createParametersForSync(nic2.getNetworkName(), nic1, nic2));

        validateAndAssertNetworkModified(helper, net);
    }

    @Test
    public void syncNetworkOnVmNetwork() {
        Network net = createNetwork("net");
        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net);
        nic.setBridged(!net.isVmNetwork());

        mockExistingNetworks(net);
        mockExistingIfaces(nic);

        SetupNetworksHelper helper = createHelper(createParametersForSync(nic));

        validateAndAssertNetworkModified(helper, net);
    }

    @Test
    public void dontSyncNetworkOnDefaultMtu() {
        Network net = createNetwork("net");
        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net);
        nic.setMtu(RandomUtils.instance().nextInt());

        mockExistingNetworks(net);
        mockExistingIfaces(nic);

        SetupNetworksHelper helper = createHelper(createParametersForSync(nic));

        validateAndAssertNoChanges(helper);
    }

    @Test
    public void syncNetworkOnMtu() {
        Network net = createNetwork("net");
        net.setMtu(RandomUtils.instance().nextInt());
        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net);
        nic.setMtu(RandomUtils.instance().nextInt());

        mockExistingNetworks(net);
        mockExistingIfaces(nic);

        SetupNetworksHelper helper = createHelper(createParametersForSync(nic));

        validateAndAssertNetworkModified(helper, net);
    }

    @Test
    public void syncNetworkOnVlan() {
        Network net = createNetwork("net");
        VdsNetworkInterface baseNic = createNic("baseNic", null);
        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net);
        nic.setBaseInterface(baseNic.getName());
        nic.setVlanId(RandomUtils.instance().nextInt());

        mockExistingNetworks(net);
        mockExistingIfaces(nic, baseNic);

        SetupNetworksHelper helper = createHelper(createParametersForSync(nic));

        validateAndAssertNetworkModified(helper, net);
    }

    @Test
    public void vlanNetworkWithVmNetworkDenied() {
        Network net1 = createNetwork("net1");
        Network net2 = createNetwork("net2");
        net2.setVlanId(100);
        mockExistingNetworks(net1, net2);

        VdsNetworkInterface nic = createNic("nic0", null);
        VdsNetworkInterface vlanNic = createVlan(nic, net2.getVlanId(), net2.getName());
        mockExistingIfaces(nic, vlanNic);

        nic.setNetworkName(net1.getName());

        SetupNetworksHelper helper = createHelper(createParametersForNics(nic, vlanNic));

        final List<NetworkType> networksOnIface = Arrays.asList(NetworkType.VM, NetworkType.VLAN);
        when(networkExclusivenessValidator.isNetworkExclusive(networksOnIface)).thenReturn(false);

        validateAndExpectViolation(helper,
            EngineMessage.NETWORK_INTERFACES_NOT_EXCLUSIVELY_USED_BY_NETWORK,
            nic.getName());
    }

    @Test
    public void vmNetworkWithVlanNetworkDenied() {
        Network net1 = createNetwork("net1");
        Network net2 = createNetwork("net2");
        net2.setVlanId(100);
        mockExistingNetworks(net1, net2);

        VdsNetworkInterface nic = createNic("nic0", null);
        VdsNetworkInterface vlanNic = createVlan(nic, net2.getVlanId(), net2.getName());
        mockExistingIfaces(nic, vlanNic);

        nic.setNetworkName(net1.getName());

        SetupNetworksHelper helper = createHelper(createParametersForNics(vlanNic, nic));

        final List<NetworkType> networksOnIface = Arrays.asList(NetworkType.VLAN, NetworkType.VM);
        when(networkExclusivenessValidator.isNetworkExclusive(networksOnIface)).thenReturn(false);

        validateAndExpectViolation(helper,
            EngineMessage.NETWORK_INTERFACES_NOT_EXCLUSIVELY_USED_BY_NETWORK,
            nic.getName());
    }

    @Test
    public void unmanagedVlanNetworkWithVmNetworkDenied() {
        Network net1 = createNetwork("net1");
        Network net2 = createNetwork("net2");
        net2.setVlanId(100);
        mockExistingNetworks(net1);

        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net1);
        mockExistingIfaces(nic);

        VdsNetworkInterface vlanNic = createVlan(nic, net2.getVlanId(), net2.getName());

        SetupNetworksHelper helper = createHelper(createParametersForNics(nic, vlanNic));

        final List<NetworkType> networksOnIface = Arrays.asList(NetworkType.VM, NetworkType.VLAN);
        when(networkExclusivenessValidator.isNetworkExclusive(networksOnIface)).thenReturn(false);

        validateAndExpectViolation(helper,
            EngineMessage.NETWORK_INTERFACES_NOT_EXCLUSIVELY_USED_BY_NETWORK,
            nic.getName());
    }

    @Test
    public void fakeVlanNicWithVmNetworkDenied() {
        Network net1 = createNetwork("net1");
        Network net2 = createNetwork("net2");
        net2.setVlanId(100);
        mockExistingNetworks(net1, net2);

        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net1);
        mockExistingIfaces(nic);

        VdsNetworkInterface fakeVlanNic = createVlan(nic, 100, net2.getName());

        SetupNetworksHelper helper = createHelper(createParametersForNics(nic, fakeVlanNic));

        final List<NetworkType> networksOnIface = Arrays.asList(NetworkType.VM, NetworkType.VLAN);
        when(networkExclusivenessValidator.isNetworkExclusive(networksOnIface)).thenReturn(false);

        validateAndExpectViolation(helper,
            EngineMessage.NETWORK_INTERFACES_NOT_EXCLUSIVELY_USED_BY_NETWORK,
            nic.getName());
    }

    @Test
    public void nonVmNetworkWithVlanVmNetwork() {
        Network net1 = createNetwork("net1");
        net1.setVmNetwork(false);
        Network net2 = createNetwork("net2");
        net2.setVlanId(200);
        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net1);
        VdsNetworkInterface vlan = createVlan(nic, net2.getVlanId(), net2.getName());

        mockExistingNetworks(net1, net2);
        mockExistingIfaces(nic);

        SetupNetworksHelper helper = createHelper(createParametersForNics(nic, vlan));

        validateAndExpectNoViolations(helper);
    }

    @Test
    public void twoVlanVmNetworks() {
        Network net1 = createNetwork("net1");
        net1.setVlanId(100);
        Network net2 = createNetwork("net2");
        net2.setVlanId(200);
        VdsNetworkInterface nic = createNic("nic0", null);
        VdsNetworkInterface vlan1 = createVlan(nic, net1.getVlanId(), net1.getName());
        VdsNetworkInterface vlan2 = createVlan(nic, net2.getVlanId(), net2.getName());

        mockExistingNetworks(net1, net2);
        mockExistingIfaces(nic);

        SetupNetworksHelper helper = createHelper(createParametersForNics(nic, vlan1, vlan2));

        validateAndExpectNoViolations(helper);
    }

    /* --- Tests for bonds functionality --- */

    @Test
    public void bondWithNoSlaves() {
        VdsNetworkInterface bond = createBond(BOND_NAME, null);

        mockExistingIfaces(bond);

        SetupNetworksHelper helper = createHelper(createParametersForNics(bond));

        validateAndExpectViolation(helper, EngineMessage.NETWORK_BONDS_INVALID_SLAVE_COUNT, bond.getName());
    }

    @Test
    public void onlyOneSlaveForBonding() {
        VdsNetworkInterface bond = createBond(BOND_NAME, null);
        List<VdsNetworkInterface> slaves = Arrays.asList(createNic("nic0", null));

        mockExistingIfacesWithBond(bond, slaves);

        SetupNetworksHelper helper = createHelper(createParametersForBond(bond, slaves));

        validateAndExpectViolation(helper, EngineMessage.NETWORK_BONDS_INVALID_SLAVE_COUNT, bond.getName());
    }

    @Test
    public void sameBondNameSentTwice() {
        VdsNetworkInterface bond = createBond(BOND_NAME, null);

        mockExistingIfaces(bond);
        SetupNetworksHelper helper = createHelper(createParametersForNics(bond, bond));

        validateAndExpectViolation(helper, EngineMessage.NETWORK_INTERFACES_ALREADY_SPECIFIED, bond.getName());
    }

    @Test
    public void bondGrew() {
        Network network = createNetwork("network");

        VdsNetworkInterface bond = createBond(BOND_NAME, "network");
        List<VdsNetworkInterface> slaves = createNics(bond.getName(), RandomUtils.instance().nextInt(3, 100));
        slaves.get(0).setBondName(null);

        mockExistingIfacesWithBond(bond, slaves);
        mockExistingNetworks(network);
        slaves.get(0).setBondName(bond.getName());
        SetupNetworksParameters parameters = createParametersForBond(bond, slaves);

        SetupNetworksHelper helper = createHelper(parameters);

        validateAndExpectNoViolations(helper);
        assertBondModified(helper, bond);
        assertNoNetworksModified(helper);
        assertNoNetworksRemoved(helper);
        assertNoBondsRemoved(helper);
    }

    @Test
    public void bondShrank() {
        VdsNetworkInterface bond = createBond(BOND_NAME, "net");
        List<VdsNetworkInterface> slaves = createNics(bond.getName(), RandomUtils.instance().nextInt(3, 100));

        mockExistingIfacesWithBond(bond, slaves);
        slaves.get(0).setBondName(null);
        SetupNetworksParameters parameters = new SetupNetworksParameters();
        parameters.setInterfaces(slaves);
        parameters.getInterfaces().add(bond);

        SetupNetworksHelper helper = createHelper(parameters);

        validateAndExpectNoViolations(helper);
        assertBondModified(helper, bond);
        assertNoNetworksModified(helper);
        assertNoNetworksRemoved(helper);
        assertNoBondsRemoved(helper);
    }

    @Test
    public void bondWithNetworkDidntChange() {
        Network network = createNetwork("network");

        VdsNetworkInterface bond = createBond(BOND_NAME, network.getName());
        List<VdsNetworkInterface> ifaces = createNics(bond.getName());

        mockExistingIfacesWithBond(bond, ifaces);
        mockExistingNetworks(network);

        SetupNetworksParameters parameters = new SetupNetworksParameters();
        ifaces.add(bond);
        parameters.setInterfaces(ifaces);
        SetupNetworksHelper helper = createHelper(parameters);

        validateAndExpectNoViolations(helper);
        assertNoBondsModified(helper);
        assertNoNetworksModified(helper);
        assertNoBondsRemoved(helper);
        assertNoNetworksRemoved(helper);
    }

    @Test
    public void bondWithNoNetworkDidntChange() {
        VdsNetworkInterface bond = createBond(BOND_NAME, null);
        List<VdsNetworkInterface> ifaces = createNics(bond.getName());

        mockExistingIfacesWithBond(bond, ifaces);
        SetupNetworksParameters parameters = new SetupNetworksParameters();
        ifaces.add(bond);
        parameters.setInterfaces(ifaces);
        SetupNetworksHelper helper = createHelper(parameters);

        validateAndExpectNoViolations(helper);
        assertNoBondsModified(helper);
        assertNoNetworksModified(helper);
        assertNoBondsRemoved(helper);
        assertNoNetworksRemoved(helper);
    }

    @Test
    public void bondWithNetworkAttached() {
        Network network = createNetwork("net");
        VdsNetworkInterface bond = createBond(BOND_NAME, null);
        List<VdsNetworkInterface> ifaces = createNics(null);

        mockExistingNetworks(network);
        mockExistingIfacesWithBond(bond, ifaces);

        bond.setNetworkName(network.getName());
        SetupNetworksParameters parameters = createParametersForBond(bond, ifaces);
        SetupNetworksHelper helper = createHelper(parameters);

        validateAndExpectNoViolations(helper);

        // The expected network name is null, since the bond didn't change from the one in the DB.
        bond.setNetworkName(null);
        assertBondModified(helper, bond);
        assertNetworkModified(helper, network);
        assertNoBondsRemoved(helper);
        assertNoNetworksRemoved(helper);
    }

    @Test
    public void bondWithNoNetowrkAttached() {
        VdsNetworkInterface bond = createBond(BOND_NAME, null);
        List<VdsNetworkInterface> ifacesToBond = createNics(null);
        SetupNetworksParameters parameters = createParametersForBond(bond, ifacesToBond);

        SetupNetworksHelper helper = createHelper(parameters);
        mockExistingIfacesWithBond(bond, ifacesToBond);

        validateAndExpectNoViolations(helper);
        assertBondModified(helper, bond);
        assertNoNetworksModified(helper);
        assertNoBondsRemoved(helper);
        assertNoNetworksRemoved(helper);
    }

    @Test
    public void bondWithNetworkRemoved() {
        VdsNetworkInterface bond = createBond(BOND_NAME, "net");
        List<VdsNetworkInterface> slaves = createNics(bond.getName());

        mockExistingIfacesWithBond(bond, slaves);
        SetupNetworksParameters parameters = new SetupNetworksParameters();
        for (VdsNetworkInterface slave : slaves) {
            parameters.getInterfaces().add(enslaveOrReleaseNIC(slave, null));
        }

        SetupNetworksHelper helper = createHelper(parameters);

        validateAndExpectNoViolations(helper);
        assertNoBondsModified(helper);
        assertNoNetworksModified(helper);
        assertNetworkRemoved(helper, bond.getNetworkName());
        assertBondRemoved(helper, bond.getName());
    }

    @Test
    public void networkRemovedFromBond() {
        String networkName = "net";
        VdsNetworkInterface bond = createBond(BOND_NAME, networkName);
        List<VdsNetworkInterface> slaves = createNics(bond.getName());

        mockExistingIfacesWithBond(bond, slaves);
        bond.setNetworkName(null);

        SetupNetworksParameters parameters = createParametersForBond(bond, slaves);
        SetupNetworksHelper helper = createHelper(parameters);

        validateAndExpectNoViolations(helper);
        assertNoBondsModified(helper);
        assertNoNetworksModified(helper);
        assertNetworkRemoved(helper, networkName);
        assertNoBondsRemoved(helper);
    }

    @Test
    public void networkReplacedOnBond() {
        Network net = createNetwork("net");
        Network newNet = createNetwork("net2");
        VdsNetworkInterface bond = createBond(BOND_NAME, net.getName());
        List<VdsNetworkInterface> slaves = createNics(bond.getName());

        mockExistingNetworks(net, newNet);
        mockExistingIfacesWithBond(bond, slaves);

        bond.setNetworkName(newNet.getName());
        SetupNetworksHelper helper = createHelper(createParametersForBond(bond, slaves));

        validateAndExpectNoViolations(helper);
        assertNetworkModified(helper, newNet);
        assertNetworkRemoved(helper, net.getName());
        assertNoBondsModified(helper);
        assertNoBondsRemoved(helper);
    }

    @Test
    public void bondOptionsChanged() {
        VdsNetworkInterface bond = createBond(BOND_NAME, null);
        bond.setBondOptions(RandomUtils.instance().nextString(10));
        List<VdsNetworkInterface> slaves = createNics(bond.getName());

        mockExistingIfacesWithBond(bond, slaves);
        bond.setBondOptions(RandomUtils.instance().nextString(10));

        SetupNetworksHelper helper = createHelper(createParametersForBond(bond, slaves));

        validateAndExpectNoViolations(helper);
        assertBondModified(helper, bond);
        assertNoNetworksModified(helper);
        assertNoNetworksRemoved(helper);
        assertNoBondsRemoved(helper);
    }

    @Test
    public void bootProtocolChangedOverBond() {
        Network net = createNetwork("net");
        mockExistingNetworks(net);

        VdsNetworkInterface bond = createBond(BOND_NAME, net.getName());
        bond.setBootProtocol(NetworkBootProtocol.NONE);
        List<VdsNetworkInterface> slaves = createNics(bond.getName());
        mockExistingIfacesWithBond(bond, slaves);
        bond.setBootProtocol(NetworkBootProtocol.DHCP);

        SetupNetworksHelper helper = createHelper(createParametersForBond(bond, slaves));

        validateAndAssertNetworkModified(helper, net);
    }

    /* --- Tests for VLANs functionality --- */

    @Test
    public void vlanOverBond() {
        Network network = createNetwork("net");
        network.setVlanId(100);
        VdsNetworkInterface bond = createBond(BOND_NAME, null);
        List<VdsNetworkInterface> ifacesToBond = createNics(null);

        mockExistingNetworks(network);
        mockExistingIfacesWithBond(bond, ifacesToBond);

        SetupNetworksParameters parameters = createParametersForBond(bond, ifacesToBond);
        parameters.getInterfaces().add(createVlan(bond, 100, network.getName()));

        SetupNetworksHelper helper = createHelper(parameters);

        validateAndExpectNoViolations(helper);
        assertBondModified(helper, bond);
        assertNetworkModified(helper, network);
        assertNoBondsRemoved(helper);
        assertNoNetworksRemoved(helper);
    }

    @Test
    public void vlanBondNameMismatch() {
        VdsNetworkInterface bond = createBond(BOND_NAME, null);
        VdsNetworkInterface anotherBond = createBond(BOND_NAME + "1", null);
        List<VdsNetworkInterface> ifacesToBond = createNics(null);
        SetupNetworksParameters parameters = createParametersForBond(bond, ifacesToBond);

        parameters.getInterfaces().add(createVlan(anotherBond, 100, "net"));
        mockExistingIfacesWithBond(bond, ifacesToBond);

        SetupNetworksHelper helper = createHelper(parameters);

        validateAndExpectViolation(helper, EngineMessage.NETWORK_INTERFACES_DONT_EXIST, anotherBond.getName());
    }

    @Test
    public void unmanagedVlanAddedToNic() {
        VdsNetworkInterface nic = createNic("nic0", null);
        mockExistingIfaces(nic);

        String networkName = "net";
        SetupNetworksHelper helper = createHelper(
            createParametersForNics(nic, createVlan(nic, 100, networkName)));

        validateAndExpectViolation(helper, EngineMessage.NETWORKS_DONT_EXIST_IN_CLUSTER, networkName);
    }

    /**
     * Existing:
     *
     * <pre>
     *     nic0 --------- nonVmMtu9000
     * </pre>
     *
     * </p> Modified:
     *
     * <pre>
     *                   ---- nonVmMtu9000
     *     nic0 ---------|
     *                   ---- vlanVmMtu9000
     * </pre>
     */
    @Test
    public void networkWithTheSameMTUAddedToNic() {
        Network net = createNetwork("nonVmMtu9000");
        net.setVmNetwork(false);
        net.setMtu(9000);
        Network newNet = createNetwork("vlanVmMtu9000");
        newNet.setMtu(9000);
        newNet.setVlanId(100);
        mockExistingNetworks(net, newNet);

        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net);
        mockExistingIfaces(nic);

        SetupNetworksHelper helper = createHelper(
                createParametersForNics(nic, createVlan(nic, newNet.getVlanId(), newNet.getName())));

        validateAndExpectNoViolations(helper);
    }

    /**
     * Existing:
     *
     * <pre>
     *     nic0 --------- vlanVmMtu9000
     * </pre>
     *
     * </p> Modified:
     *
     * <pre>
     *                   ---- vlanVmMtu9000
     *     nic0 ---------|
     *                   ---- nonVmMtu9000
     * </pre>
     */
    @Test
    public void nonVmWithSameMTUAddedToNic() {
        Network net = createNetwork("vlanVmMtu9000");
        net.setVlanId(100);
        net.setMtu(9000);
        Network newNet = createNetwork("nonVmMtu9000");
        newNet.setVmNetwork(false);
        newNet.setMtu(9000);
        mockExistingNetworks(net, newNet);

        VdsNetworkInterface nic = createNic("nic0", null);
        VdsNetworkInterface nicWithVlan = createVlanSyncedWithNetwork(nic, net);
        mockExistingIfaces(nic, nicWithVlan);

        nic.setNetworkName(newNet.getName());
        SetupNetworksHelper helper = createHelper(createParametersForNics(nic, nicWithVlan));

        validateAndExpectNoViolations(helper);
    }

    /**
     * Existing:
     *
     * <pre>
     *     nic0 --------- nonVm (mtu=default)
     * </pre>
     *
     * </p> Modified:
     *
     * <pre>
     *                   ---- nonVm (mtu=default)
     *     nic0 ---------|
     *                   ---- vlanVm (mtu=default)
     * </pre>
     */
    @Test
    public void networkWithTheSameDefaultMTUAddedToNic() {
        Network net = createNetwork("nonVm");
        net.setVmNetwork(false);
        Network newNet = createNetwork("vlanVm");
        newNet.setVlanId(100);
        mockExistingNetworks(net, newNet);

        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net);
        mockExistingIfaces(nic);

        SetupNetworksHelper helper = createHelper(
                createParametersForNics(nic, createVlan(nic, newNet.getVlanId(), newNet.getName())));

        validateAndExpectNoViolations(helper);
    }

    /**
     * Existing:
     *
     * <pre>
     *     nic0 --------- nonVmMtu5000
     * </pre>
     *
     * </p> Modified:
     *
     * <pre>
     *                   ---- nonVmMtu5000
     *     nic0 ---------|
     *                   ---- vlanVmMtu9000
     * </pre>
     */
    @Test
    public void vlanWithDifferentMTUAddedToNic() {
        Network net = createNetwork("nonVmMtu5000");
        net.setVmNetwork(false);
        net.setMtu(5000);
        Network newNet = createNetwork("vlanVmMtu9000");
        newNet.setVlanId(100);
        newNet.setMtu(9000);
        mockExistingNetworks(net, newNet);

        VdsNetworkInterface nic = createNicSyncedWithNetwork("nic0", net);
        mockExistingIfaces(nic);

        SetupNetworksHelper helper = createHelper(
                createParametersForNics(nic, createVlan(nic, newNet.getVlanId(), newNet.getName())));

        validateAndExpectMtuValidation(helper, net, newNet);
    }

    /**
     * Existing:
     *
     * <pre>
     *     nic0 --------- vlanVmMtu9000
     * </pre>
     *
     * </p> Modified:
     *
     * <pre>
     *                   ---- vlanVmMtu9000
     *     nic0 ---------|
     *                   ---- nonVmMtu5000
     * </pre>
     */
    @Test
    public void nonVmWithDifferentMTUAddedToNic() {
        Network net = createNetwork("vlanVmMtu9000");
        net.setVlanId(100);
        net.setMtu(9000);
        Network newNet = createNetwork("nonVmMtu5000");
        newNet.setVmNetwork(false);
        newNet.setMtu(5000);
        mockExistingNetworks(net, newNet);

        VdsNetworkInterface nic = createNic("nic0", null);
        VdsNetworkInterface nicWithVlan = createVlanSyncedWithNetwork(nic, net);
        mockExistingIfaces(nic, nicWithVlan);

        nic.setNetworkName(newNet.getName());
        SetupNetworksHelper helper = createHelper(createParametersForNics(nic, nicWithVlan));

        validateAndExpectMtuValidation(helper, newNet, net);
    }

    /**
     * Existing:
     *
     * <pre>
     *     nic0 --------- vlanVmMtu9000
     * </pre>
     *
     * </p> Modified:
     *
     * <pre>
     *                   ---- vlanVmMtu9000
     *     nic0 ---------|
     *                   ---- nonVm (mtu=default)
     * </pre>
     */
    @Test
    public void nonVmWithDefaultMTUAddedToNic() {
        Network net = createNetwork("vlanVmMtu9000");
        net.setVlanId(100);
        net.setMtu(9000);
        Network newNet = createNetwork("nonVm");
        newNet.setVmNetwork(false);
        mockExistingNetworks(net, newNet);

        VdsNetworkInterface nic = createNic("nic0", null);
        VdsNetworkInterface nicWithVlan = createVlanSyncedWithNetwork(nic, net);
        mockExistingIfaces(nic, nicWithVlan);

        nic.setNetworkName(newNet.getName());
        SetupNetworksHelper helper = createHelper(createParametersForNics(nic, nicWithVlan));

        validateAndExpectMtuValidation(helper, newNet, net);
    }

    /**
     * Existing:
     *
     * <pre>
     *     nic0 --------- vlanVmMtu9000
     * </pre>
     *
     * </p> Modified:
     *
     * <pre>
     *                   ---- vlanVmMtu9000
     *     nic0 ---------|
     *                   ---- vlanNonVmMtu5000
     * </pre>
     */
    @Test
    public void nonVmVlanWithDifferentMTUAddedToNic() {
        Network net = createNetwork("vlanVmMtu9000");
        net.setVlanId(100);
        net.setMtu(9000);
        Network newNet = createNetwork("vlanNonVmMtu5000");
        newNet.setVlanId(200);
        newNet.setMtu(5000);
        newNet.setVmNetwork(false);
        mockExistingNetworks(net, newNet);

        VdsNetworkInterface nic = createNic("nic0", null);
        VdsNetworkInterface nicWithVlan = createVlanSyncedWithNetwork(nic, net);
        mockExistingIfaces(nic, nicWithVlan);

        VdsNetworkInterface nicWithNonVmVlan = createVlan(nic, newNet.getVlanId(), newNet.getName());
        nicWithNonVmVlan.setMtu(newNet.getMtu());

        SetupNetworksHelper helper = createHelper(createParametersForNics(nic, nicWithVlan, nicWithNonVmVlan));

        validateAndExpectNoViolations(helper);
    }

    /**
     * Existing:
     *
     * <pre>
     * nic0
     * </pre>
     *
     * </p> Modified:
     *
     * <pre>
     *                   ---- nonVmMtu5000
     *     nic0 ---------|
     *                   ---- vlanVmMtu9000
     * </pre>
     */
    @Test
    public void networksWithDifferentMTUAddedToNic() {
        Network net = createNetwork("nonVmMtu5000");
        net.setVmNetwork(false);
        net.setMtu(5000);
        Network newNet = createNetwork("vlanVmMtu9000");
        newNet.setVlanId(100);
        newNet.setMtu(9000);
        mockExistingNetworks(net, newNet);

        VdsNetworkInterface nic = createNic("nic0", null);
        mockExistingIfaces(nic);

        nic.setNetworkName(net.getName());
        SetupNetworksHelper helper = createHelper(
                createParametersForNics(nic, createVlan(nic, newNet.getVlanId(), newNet.getName())));

        validateAndExpectMtuValidation(helper, net, newNet);
    }

    private void validateAndExpectMtuValidation(SetupNetworksHelper helper, Network net1, Network net2) {
        validateAndExpectViolation(helper, EngineMessage.NETWORK_MTU_DIFFERENCES,
                String.format("[%s(%s), %s(%d)]",
                    net1.getName(),
                    net1.getMtu() == 0 ? "default" : net1.getMtu(),
                    net2.getName(),
                    net2.getMtu()));
    }

    /* --- Tests for General Violations --- */

    @Test
    public void violationAppearsTwice() {
        VdsNetworkInterface nic1 = createNic("nic0", null);
        VdsNetworkInterface nic2 = createNic("nic1", null);
        mockExistingIfaces(nic1, nic2);

        SetupNetworksHelper helper = createHelper(createParametersForNics(nic1, nic1, nic2, nic2));

        validateAndExpectViolation(helper,
            EngineMessage.NETWORK_INTERFACES_ALREADY_SPECIFIED,
            nic1.getName(),
            nic2.getName());
    }

    @Test
    public void nicDoesntExist() {
        VdsNetworkInterface nic = createNic("eth0", null);
        SetupNetworksHelper helper = createHelper(createParametersForNics(nic));
        validateAndExpectViolation(helper, EngineMessage.NETWORK_INTERFACES_DONT_EXIST, nic.getName());
    }

    @Test
    public void bondDoesntExist() {
        VdsNetworkInterface bond = createBond(BOND_NAME, null);
        List<VdsNetworkInterface> ifacesToBond = createNics(null);
        mockExistingIfaces(ifacesToBond.toArray(new VdsNetworkInterface[ifacesToBond.size()]));
        SetupNetworksHelper helper = createHelper(createParametersForBond(bond, ifacesToBond));
        validateAndExpectNoViolations(helper);
    }

    @Test
    public void unlabeledNetworkRemovedFromLabeledNic() {
        String networkName = "net";
        VdsNetworkInterface nic = createLabeledNic("nic0", networkName, "lbl1");
        mockExistingIfaces(nic);
        nic.setNetworkName(null);
        mockExistingNetworks(createNetwork(networkName));
        SetupNetworksHelper helper = createHelper(createParametersForNics(nic));

        validateAndExpectNoViolations(helper);
        assertNoNetworksModified(helper);
        assertNetworkRemoved(helper, networkName);
    }

    @Test
    public void labeledNetworkRemovedFromUnlabeledNic() {
        String networkName = "net";
        VdsNetworkInterface nic = createNic("nic0", networkName);
        mockExistingIfaces(nic);
        nic.setNetworkName(null);
        mockExistingNetworks(createLabeledNetwork(networkName, "lbl1"));
        SetupNetworksHelper helper = createHelper(createParametersForNics(nic));

        validateAndExpectNoViolations(helper);
        assertNoNetworksModified(helper);
        assertNetworkRemoved(helper, networkName);
    }

    @Test
    public void labeledNetworkRemovedFromNic() {
        String networkName = "net";
        String label = "lbl1";
        VdsNetworkInterface nic = createLabeledNic("nic0", networkName, label);
        mockExistingIfaces(nic);
        nic.setNetworkName(null);
        mockExistingNetworks(createLabeledNetwork(networkName, label));

        SetupNetworksHelper helper = createHelper(createParametersForNics(nic));

        validateAndExpectViolation(helper,
            EngineMessage.ACTION_TYPE_FAILED_CANNOT_REMOVE_LABELED_NETWORK_FROM_NIC,
            networkName);
    }

    /* --- Helper methods for tests --- */

    private void validateAndExpectNoViolations(SetupNetworksHelper helper) {
        List<String> violations = helper.validate();
        assertTrue("Expected no violations, but got: " + violations, violations.isEmpty());
    }

    private void validateAndExpectViolation(SetupNetworksHelper helper,
            EngineMessage violation,
            String... violatingEntities) {
        List<String> violations = helper.validate();
        assertTrue(MessageFormat.format("Expected violation {0} but only got {1}.", violation, violations),
            violations.contains(violation.name()));

        for(String violationDetail : ReplacementUtils.replaceWith(violation + LIST_SUFFIX, Arrays.asList(violatingEntities))) {
            assertThat("Missing violation entity", violations, hasItems(violationDetail));
        }
    }

    private void validateAndExpectViolation(SetupNetworksHelper helper, EngineMessage violation) {
        List<String> violations = helper.validate();
        assertTrue(MessageFormat.format("Expected violation {0} but only got {1}.", violation, violations),
            violations.contains(violation.name()));
    }

    private void validateAndAssertNoChanges(SetupNetworksHelper helper) {
        validateAndExpectNoViolations(helper);
        assertNoBondsModified(helper);
        assertNoNetworksModified(helper);
        assertNoNetworksRemoved(helper);
        assertNoBondsRemoved(helper);
        assertNoInterfacesModified(helper);
    }

    private void validateAndAssertNetworkModified(SetupNetworksHelper helper, Network net) {
        validateAndExpectNoViolations(helper);
        assertNoBondsModified(helper);
        assertNetworkModified(helper, net);
        assertNoNetworksRemoved(helper);
        assertNoBondsRemoved(helper);
        assertNoInterfacesModified(helper);
    }

    private void validateAndAssertNetworkRemoved(SetupNetworksHelper helper, String networkName) {
        validateAndExpectNoViolations(helper);
        assertNoBondsModified(helper);
        assertNoNetworksModified(helper);
        assertNetworkRemoved(helper, networkName);
        assertNoBondsRemoved(helper);
        assertNoInterfacesModified(helper);
    }

    private void assertBondRemoved(SetupNetworksHelper helper, String expectedBondName) {
        assertTrue(MessageFormat.format("Expected bond ''{0}'' to be removed but it wasn''t. Removed bonds: {1}",
                expectedBondName, helper.getRemovedBonds()),
            helper.getRemovedBonds().containsKey(expectedBondName));
    }

    private void assertNetworkRemoved(SetupNetworksHelper helper, String expectedNetworkName) {
        assertTrue(MessageFormat.format("Expected network ''{0}'' to be removed but it wasn''t. Removed networks: {1}",
                expectedNetworkName, helper.getRemoveNetworks()),
            helper.getRemoveNetworks().contains(expectedNetworkName));
    }

    private void assertNoNetworksRemoved(SetupNetworksHelper helper) {
        assertTrue(MessageFormat.format(
                "Expected no networks to be removed but some were removed. Removed networks: {0}",
                helper.getRemoveNetworks()),
            helper.getRemoveNetworks().isEmpty());
    }

    private void assertNoBondsRemoved(SetupNetworksHelper helper) {
        assertTrue(MessageFormat.format("Expected no bonds to be removed but some were removed. Removed bonds: {0}",
                helper.getRemovedBonds()),
            helper.getRemovedBonds().isEmpty());
    }

    private void assertNetworkModified(SetupNetworksHelper helper, Network expectedNetwork) {
        assertEquals("Expected a modified network.", 1, helper.getNetworks().size());
        assertEquals(MessageFormat.format(
                "Expected network ''{0}'' to be modified but it wasn''t. Modified networks: {1}",
                expectedNetwork,
                helper.getNetworks()),
            expectedNetwork,
            helper.getNetworks().get(0));
    }

    private void assertBondModified(SetupNetworksHelper helper, VdsNetworkInterface expectedBond) {
        assertEquals(1, helper.getBonds().size());
        assertEquals(MessageFormat.format("Expected bond ''{0}'' to be modified but it wasn''t. Modified bonds: {1}",
                expectedBond, helper.getBonds()),
            expectedBond, helper.getBonds().get(0));
    }

    private void assertNoNetworksModified(SetupNetworksHelper helper) {
        assertEquals(MessageFormat.format(
                "Expected no networks to be modified but some were modified. Modified networks: {0}",
                helper.getNetworks()),
            0, helper.getNetworks().size());
    }

    private void assertNoBondsModified(SetupNetworksHelper helper) {
        assertEquals(MessageFormat.format(
                "Expected no bonds to be modified but some were modified. Modified bonds: {0}",
                helper.getBonds()),
            0, helper.getBonds().size());
    }

    private void assertInterfaceModified(SetupNetworksHelper helper, VdsNetworkInterface iface) {
        Set<String> modifiedNames = new HashSet<String>();
        for (VdsNetworkInterface modifiedIface : helper.getModifiedInterfaces()) {
            modifiedNames.add(modifiedIface.getName());
        }
        assertTrue(MessageFormat.format(
                "Expected interface ''{0}'' to be modified but it wasn''t. Modified interfaces: {1}",
                iface,
                helper.getModifiedInterfaces()),
            modifiedNames.contains(iface.getName()));
    }

    private void assertNoInterfacesModified(SetupNetworksHelper helper) {
        assertEquals(MessageFormat.format(
                "Expected no interfaces to be modified, but the following interfaces were: {0}",
                helper.getModifiedInterfaces()),
            0,
            helper.getModifiedInterfaces().size());
    }

    /**
     * @param networkName
     *            The network's name.
     * @return A network with some defaults and the given name,
     */
    private Network createNetwork(String networkName) {
        Network net = new Network("", "", Guid.newGuid(), networkName, "", "", 0, null, false, 0, true);
        net.setCluster(new NetworkCluster());
        return net;
    }

    private Network createManagementNetwork() {
        when(managementNetworkUtil.isManagementNetwork(eq(MANAGEMENT_NETWORK_NAME), any(Guid.class))).thenReturn(true);
        return createNetwork(MANAGEMENT_NETWORK_NAME);
    }

    /**
     * @param networkName
     *            The network's name.
     * @param label
     *            The label to be set on the network
     * @return A network with some defaults, the given name and the given label
     */
    private Network createLabeledNetwork(String networkName, String label) {
        Network network = createNetwork(networkName);
        network.setLabel(label);
        return network;
    }

    /**
     * Base method to create any sort of network interface with the given parameters.
     *
     * @param id
     * @param name
     * @param bonded
     * @param bondName
     * @param baseInterfaceName
     * @param vlanId
     * @param networkName
     * @param bridged
     * @param address
     * @return A network interface.
     */
    private VdsNetworkInterface createVdsInterface(Guid id,
            String name,
            Boolean bonded,
            String bondName,
            String baseInterfaceName,
            Integer vlanId,
            String networkName,
            boolean bridged,
            String address,
            Set<String> labels,
            Integer speed) {
        VdsNetworkInterface iface = new VdsNetworkInterface();
        iface.setId(id);
        iface.setName(name);
        iface.setBonded(bonded);
        iface.setBondName(bondName);
        iface.setBaseInterface(baseInterfaceName);
        iface.setVlanId(vlanId);
        iface.setNetworkName(networkName);
        iface.setBridged(bridged);
        iface.setAddress(address);
        iface.setLabels(labels);
        iface.setSpeed(speed);
        return iface;
    }

    /**
     * @param nicName
     *            The name of the NIC.
     * @param networkName
     *            The network that is on the NIC. Can be <code>null</code>.
     * @return {@link VdsNetworkInterface} representing a regular NIC with the given parameters.
     */
    private VdsNetworkInterface createNic(String nicName, String networkName) {
        VdsNetworkInterface nic = createVdsInterface(Guid.newGuid(),
            nicName,
            false,
            null,
            null,
            null,
            networkName,
            true,
            null,
            null,
            DEFAULT_SPEED);

        mockCalculateBaseNicWhenBaseNicIsPassed(nic);
        return nic;
    }

    private void mockCalculateBaseNicWhenBaseNicIsPassed(VdsNetworkInterface nic) {
        when(calculateBaseNic.getBaseNic(eq(nic))).thenReturn(nic);
        when(calculateBaseNic.getBaseNic(eq(nic), any(Map.class))).thenReturn(nic);
    }

    private VdsNetworkInterface createManagementNetworkNic(String nicName) {
        when(managementNetworkUtil.isManagementNetwork(eq(MANAGEMENT_NETWORK_NAME), any(Guid.class))).thenReturn(true);
        return createNic(nicName, MANAGEMENT_NETWORK_NAME);
    }

    /**
     * @param nicName
     *            The name of the NIC.
     * @param networkName
     *            The network that is on the NIC. Can be <code>null</code>.
     * @param labels
     *            The labels to be set for the nic
     * @return {@link VdsNetworkInterface} representing a regular labeled NIC with the given parameters.
     */
    private VdsNetworkInterface createLabeledNic(String nicName, String networkName, String ... labels) {
        VdsNetworkInterface nic = createNic(nicName, networkName);
        nic.setLabels(new HashSet<>(Arrays.asList(labels)));
        return nic;
    }

    /**
     * @param nicName
     *            The name of the NIC.
     * @param network
     *            The network that the NIC is synced to. Can't be <code>null</code>.
     * @return {@link VdsNetworkInterface} representing a regular NIC with the given parameters.
     */
    private VdsNetworkInterface createNicSyncedWithNetwork(String nicName, Network network) {
        VdsNetworkInterface nic = createVdsInterface(Guid.newGuid(),
            nicName,
            false,
            null,
            null,
            network.getVlanId(),
            network.getName(),
            network.isVmNetwork(),
            network.getAddr(),
            null,
            DEFAULT_SPEED);
        mockCalculateBaseNicWhenBaseNicIsPassed(nic);

        return nic;
    }

    /**
     * @param baseNic baseNic
     * @param network
     *            The network that the NIC is in sync with. Can't be <code>null</code>.
     * @return {@link VdsNetworkInterface} representing a vlan NIC with the given parameters.
     */
    private VdsNetworkInterface createVlanSyncedWithNetwork(VdsNetworkInterface baseNic, Network network) {
        VdsNetworkInterface nic = createVlan(baseNic, network.getVlanId(), network.getName());
        nic.setBridged(network.isVmNetwork());
        nic.setMtu(network.getMtu());
        return nic;
    }

    /**
     * @param name
     *            The name of the bond.
     * @param networkName
     *            The network that is on the bond. Can be <code>null</code>.
     * @return Bond with the given parameters.
     */
    private VdsNetworkInterface createBond(String name, String networkName) {
        VdsNetworkInterface bond = createVdsInterface(Guid.newGuid(),
            name,
            true,
            null,
            null,
            null,
            networkName,
            true,
            null,
            null,
            DEFAULT_SPEED);
        mockCalculateBaseNicWhenBaseNicIsPassed(bond);
        return bond;
    }

    /**
     * @param baseNic
     *            The iface that the VLAN is sitting on.
     * @param vlanId
     *            The VLAN id.
     * @param networkName
     *            The network that is on the VLAN. Can be <code>null</code>.
     * @return VLAN over the given interface, with the given ID and optional network name.
     */
    private VdsNetworkInterface createVlan(VdsNetworkInterface baseNic, int vlanId, String networkName) {
        String baseIfaceName = baseNic.getName();
        VdsNetworkInterface nic = createVdsInterface(Guid.newGuid(),
            baseIfaceName + "." + vlanId,
            false,
            null,
            baseIfaceName,
            vlanId,
            networkName,
            true,
            null,
            null,
            DEFAULT_SPEED);
        mockCalculateBaseNicWhenVlanNicIsPassed(baseNic, nic);
        return nic;

    }

    private void mockCalculateBaseNicWhenVlanNicIsPassed(VdsNetworkInterface baseNic, VdsNetworkInterface vlanNic) {
        when(calculateBaseNic.getBaseNic(eq(vlanNic))).thenReturn(baseNic);
        when(calculateBaseNic.getBaseNic(eq(vlanNic), any(Map.class))).thenReturn(baseNic);
    }

    /**
     * If a bond name is specified then enslave the given NIC to the bond, otherwise free the given NIC.
     *
     * @param iface
     *            The NIC to enslave (or free from bond).
     * @param bondName
     *            The bond the slave is part of. Can be <code>null</code> to indicate it was enslaved but now is free.
     * @return NIC from given NIC which is either enslaved or freed.
     */
    private VdsNetworkInterface enslaveOrReleaseNIC(VdsNetworkInterface iface, String bondName) {
        return createVdsInterface(iface.getId(),
            iface.getName(),
            false,
            bondName,
            null,
            null,
            null,
            true,
            null,
            null,
            DEFAULT_SPEED);
    }

    /**
     * @param bondName
     *            Optional (Can be <code>null</code>) bond name that these NICs are already enslaved to.
     * @return List of interfaces which optionally are slaves of the given bond.
     */
    private List<VdsNetworkInterface> createNics(String bondName) {
        int slaveCount = RandomUtils.instance().nextInt(2, 100);
        return createNics(bondName, slaveCount);
    }

    /**
     * @param bondName
     *            Optional (Can be <code>null</code>) bond name that these NICs are already enslaved to.
     * @param count
     *            How many NICs to create.
     * @return List of interfaces which optionally are slaves of the given bond.
     */
    private List<VdsNetworkInterface> createNics(String bondName, int count) {
        List<VdsNetworkInterface> ifaces = new ArrayList<VdsNetworkInterface>(count);
        for (int i = 0; i < count; i++) {
            VdsNetworkInterface nic = createNic("eth" + i, null);

            if (bondName != null) {
                nic = enslaveOrReleaseNIC(nic, bondName);
            }

            ifaces.add(nic);
        }

        return ifaces;
    }

    /**
     * Create parameters for the given NICs.
     *
     * @param nics
     *            The NICs to use in parameters.
     * @return Parameters with the NIC.
     */
    private SetupNetworksParameters createParametersForNics(VdsNetworkInterface... nics) {
        SetupNetworksParameters parameters = new SetupNetworksParameters();
        parameters.setInterfaces(Arrays.asList(nics));
        parameters.setCustomProperties(customProperties);
        return parameters;
    }

    /**
     * Create parameters for the given bond over the given interfaces.
     *
     * @param bond
     *            The bond to use in parameters.
     * @param bondedIfaces
     *            The interfaces to use as bond slaves.
     * @return Parameters that define a bond over 2 interfaces.
     */
    private SetupNetworksParameters createParametersForBond(VdsNetworkInterface bond,
            List<VdsNetworkInterface> bondedIfaces) {
        SetupNetworksParameters parameters = new SetupNetworksParameters();
        parameters.getInterfaces().add(bond);

        for (VdsNetworkInterface iface : bondedIfaces) {
            parameters.getInterfaces().add(enslaveOrReleaseNIC(iface, bond.getName()));
        }

        return parameters;
    }

    /**
     * Create parameters for the given NIC, and the nic's network name set to be synchronized.
     *
     * @param nic
     *            The NIC to use in parameters.
     * @return Parameters with the NIC.
     */
    private SetupNetworksParameters createParametersForSync(VdsNetworkInterface nic) {
        return createParametersForSync(nic.getNetworkName(), nic);
    }

    /**
     * Create parameters for the given NICs, and the given network name to be synchronized.
     *
     * @param nics
     *            The NICs to use in parameters.
     * @param network
     *            The name of network to be synchronized.
     * @return Parameters with the NIC.
     */
    private SetupNetworksParameters createParametersForSync(String network, VdsNetworkInterface... nics) {
        SetupNetworksParameters params = createParametersForNics(nics);
        params.setNetworksToSync(Collections.singletonList(network));
        return params;
    }

    /**
     * Create QoS with some non-empty values.
     *
     * @return the QoS entity.
     */
    private HostNetworkQos createQos() {
        HostNetworkQos qos = new HostNetworkQos();
        qos.setOutAverageLinkshare(30);
        qos.setOutAverageUpperlimit(30);
        qos.setOutAverageRealtime(30);
        return qos;
    }

    private Map<String, String> createCustomProperties() {
        Map<String, String> customProperties = new HashMap<String, String>();
        customProperties.put("bridge_opts", "forward_delay=1500");
        return customProperties;
    }

    private void mockExistingNetworks(Network... networks) {
        when(networkDao.getAllForCluster(any(Guid.class))).thenReturn(Arrays.asList(networks));
    }

    private void mockExistingIfacesWithBond(VdsNetworkInterface bond, List<VdsNetworkInterface> ifacesToBond) {
        VdsNetworkInterface[] ifaces = new VdsNetworkInterface[ifacesToBond.size() + 1];
        ifacesToBond.toArray(ifaces);
        ifaces[ifaces.length - 1] = bond;
        mockExistingIfaces(ifaces);
    }

    /*
    READ! This brilliant method does shallow copies of your nics which were potentially used in mocking
    rendering it useless. So if you used 'eq' and it miraculously
    does not work, it means, that newly created instance does not equal to original one. Notice, that
    Mockito.eq argument matches uses == operator, so … So you can't use eq. Use Mockito.any.
    If you need to mock multiple instnaces behavior, you probably have to use
     when(<method call>(any(…)).thenReturn(<first call result>,<second call result>, <so on.>)

    purpose of this method was to separate original, input nics, so that copies can be altered and compared to originals
    after operation, however this is very inappropriate.
    * */
    private void mockExistingIfaces(VdsNetworkInterface... nics) {
        List<VdsNetworkInterface> existingIfaces = new ArrayList<>();

        for (VdsNetworkInterface original : nics) {
            VdsNetworkInterface vdsInterface = createVdsInterface(original.getId(),
                original.getName(),
                original.getBonded(),
                original.getBondName(),
                original.getBaseInterface(),
                original.getVlanId(),
                original.getNetworkName(),
                original.isBridged(),
                original.getAddress(),
                original.getLabels(),
                original.getSpeed());

            vdsInterface.setBootProtocol(original.getBootProtocol());
            vdsInterface.setQos(original.getQos());

            existingIfaces.add(vdsInterface);

        }

        mockCalculateBaseNic(existingIfaces);
        when(interfaceDao.getAllInterfacesForVds(any(Guid.class))).thenReturn(existingIfaces);
    }

    private void mockCalculateBaseNic(List<VdsNetworkInterface> existingIfaces) {
        Map<String, VdsNetworkInterface> interfacesByName = new HashMap<>();
        for (VdsNetworkInterface existingIface : existingIfaces) {
            interfacesByName.put(existingIface.getName(), existingIface);
        }

        for (VdsNetworkInterface existingIface : existingIfaces) {
            boolean vlan = existingIface.getBaseInterface() != null;
            if (vlan) {
                mockCalculateBaseNicWhenVlanNicIsPassed(interfacesByName.get(existingIface.getBaseInterface()),
                    existingIface);
            } else {
                mockCalculateBaseNicWhenBaseNicIsPassed(existingIface);
            }
        }
    }

    private SetupNetworksHelper createHelper(SetupNetworksParameters params) {
        return createHelper(params, Version.v3_3);
    }

    private SetupNetworksHelper createHelper(SetupNetworksParameters params, Version compatibilityVersion) {
        VDS vds = mock(VDS.class);
        when(vds.getId()).thenReturn(Guid.Empty);
        return createHelper(params, vds, compatibilityVersion);
    }

    private SetupNetworksHelper createHelper(SetupNetworksParameters params, VDS vds) {
        return createHelper(params, vds, Version.v3_3);
    }

    private SetupNetworksHelper createHelper(SetupNetworksParameters params, VDS vds, Version compatibilityVersion) {
        when(vds.getVdsGroupCompatibilityVersion()).thenReturn(compatibilityVersion);
        final HashSet<Version> supportedClusterVersions = new HashSet<>();
        when(vds.getSupportedClusterVersionsSet()).thenReturn(supportedClusterVersions);
        when(networkExclusivenessValidatorResolver.resolveNetworkExclusivenessValidator(same(supportedClusterVersions)))
                .thenReturn(networkExclusivenessValidator);
        when(networkExclusivenessValidator.isNetworkExclusive(Mockito.anyListOf(NetworkType.class))).thenReturn(true);
        when(networkExclusivenessValidator.getViolationMessage()).thenReturn(
                NETWORK_INTERFACES_NOT_EXCLUSIVELY_USED_BY_NETWORK_MSG);

        SetupNetworksHelper helper = spy(
                new SetupNetworksHelper(params, vds, managementNetworkUtil, networkExclusivenessValidatorResolver));

        when(helper.getVmInterfaceManager()).thenReturn(vmInterfaceManager);
        doReturn(null).when(helper).translateErrorMessages(Matchers.<List<String>> any());

        return helper;
    }
}

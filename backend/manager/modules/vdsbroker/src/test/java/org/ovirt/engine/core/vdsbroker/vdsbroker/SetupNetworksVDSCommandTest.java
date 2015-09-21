package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.SetupNetworksVdsCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.NetworkInSyncWithVdsNetworkInterface;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.vdsbroker.CalculateBaseNic;
import org.ovirt.engine.core.vdsbroker.EffectiveHostNetworkQos;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
public class SetupNetworksVDSCommandTest {

    @Mock
    private IVdsServer server;

    @Mock
    private HostNetworkQosDao qosDao;

    @Mock
    private VDS host;

    @Mock
    private Version version;

    @Mock
    private CalculateBaseNic calculateBaseNic;

    @Mock
    private NetworkAttachmentDao networkAttachmentDao;

    @Mock
    private EffectiveHostNetworkQos effectiveHostNetworkQos;

    @Rule
    public MockConfigRule configRule = new MockConfigRule();

    @Captor
    private ArgumentCaptor<Map> bondingCaptor;

    @Captor
    private ArgumentCaptor<Map> networksCaptor;

    @Before
    public void mockConfig() {
        HashSet<Version> supportedClusters = new HashSet<>();
        supportedClusters.add(version);
        when(host.getSupportedClusterVersionsSet()).thenReturn(supportedClusters);
        when(host.getVdsGroupCompatibilityVersion()).thenReturn(version);
        configRule.mockConfigValue(ConfigValues.DefaultRouteSupported, version, Boolean.FALSE);
        configRule.mockConfigValue(ConfigValues.DefaultMTU, 1500);
        configRule.mockConfigValue(ConfigValues.HostNetworkQosSupported, version, false);
    }

    @Test
    public void vlanOverNic() {
        Network net = createNetwork(RandomUtils.instance().nextInt(0, 4000));
        VdsNetworkInterface nic = createNic("eth0", null, NetworkBootProtocol.DHCP, null);
        VdsNetworkInterface vlan = createVlan(nic, net);

        SetupNetworksVdsCommandParameters parameters =
                new SetupNetworksVdsCommandParameters(host,
                        Collections.singletonList(net),
                        Collections.<String> emptyList(),
                        Collections.singletonList(nic),
                        Collections.<String> emptySet(),
                        Arrays.asList(nic, vlan));

        createCommand(parameters).execute();
        verifyMethodPassedToHost();

        Map<String, Object> networkStruct = assertNeworkWasSent(net);
        assertEquals(nic.getName(), networkStruct.get("nic"));
    }

    @Test
    public void vlanOverBond() {
        VdsNetworkInterface bond = createBond();
        List<VdsNetworkInterface> slaves = createSlaves(bond);
        Network net = createNetwork(RandomUtils.instance().nextInt(0, 4000));
        VdsNetworkInterface vlan = createVlan(bond, net);

        List<VdsNetworkInterface> ifaces = new ArrayList<>(slaves);
        ifaces.add(bond);
        ifaces.add(vlan);

        SetupNetworksVdsCommandParameters parameters =
                new SetupNetworksVdsCommandParameters(host,
                        Collections.singletonList(net),
                        Collections.<String> emptyList(),
                        Collections.singletonList(bond),
                        Collections.<String> emptySet(),
                        ifaces);

        createCommand(parameters).execute();
        verifyMethodPassedToHost();

        assertBondWasSent(bond, slaves);
        Map<String, Object> networkStruct = assertNeworkWasSent(net);
        assertEquals(bond.getName(), networkStruct.get("bonding"));
    }

    @Test
    public void networkWithDhcp() {
        Network net = createNetwork(null);
        VdsNetworkInterface nic = createNic("eth0", null, NetworkBootProtocol.DHCP, net.getName());

        SetupNetworksVdsCommandParameters parameters =
                new SetupNetworksVdsCommandParameters(host,
                        Collections.singletonList(net),
                        Collections.<String> emptyList(),
                        Collections.<VdsNetworkInterface> emptyList(),
                        Collections.<String> emptySet(),
                        Collections.singletonList(nic));

        createCommand(parameters).execute();
        verifyMethodPassedToHost();

        Map<String, Object> networkStruct = assertNeworkWasSent(net);
        assertEquals(nic.getName(), networkStruct.get("nic"));
        assertEquals(SetupNetworksVDSCommand.DHCP_BOOT_PROTOCOL,
                networkStruct.get(SetupNetworksVDSCommand.BOOT_PROTOCOL));
    }

    @Test
    public void bondModified() {
        VdsNetworkInterface bond = createBond();
        List<VdsNetworkInterface> slaves = createSlaves(bond);
        List<VdsNetworkInterface> ifaces = new ArrayList<>(slaves);
        ifaces.add(bond);

        SetupNetworksVdsCommandParameters parameters =
                new SetupNetworksVdsCommandParameters(host,
                        Collections.<Network> emptyList(),
                        Collections.<String> emptyList(),
                        Collections.singletonList(bond),
                        Collections.<String> emptySet(),
                        ifaces);

        createCommand(parameters).execute();
        verifyMethodPassedToHost();

        Map<String, Object> bondMap = assertBondWasSent(bond, slaves);
        assertEquals(bond.getBondOptions(), bondMap.get(SetupNetworksVDSCommand.BONDING_OPTIONS));
    }

    private void qos(Network network,
            VdsNetworkInterface iface,
            HostNetworkQos expectedQos,
            boolean hostNetworkQosSupported) {
        configRule.mockConfigValue(ConfigValues.HostNetworkQosSupported, version, hostNetworkQosSupported);

        SetupNetworksVdsCommandParameters parameters =
                new SetupNetworksVdsCommandParameters(host,
                        Collections.singletonList(network),
                        Collections.<String> emptyList(),
                        Collections.<VdsNetworkInterface> emptyList(),
                        Collections.<String> emptySet(),
                        Collections.singletonList(iface));

        createCommand(parameters).execute();

        verifyMethodPassedToHost();
        Map<String, Object> networkStruct = assertNeworkWasSent(network);
        HostNetworkQos result = new HostNetworkQosMapper(networkStruct).deserialize();
        iface.setQos(result);
        assertTrue(new NetworkInSyncWithVdsNetworkInterface(iface, network, expectedQos, null).qosParametersEqual());
    }

    private void qos(Network network, VdsNetworkInterface iface, HostNetworkQos expectedQos) {
        qos(network, iface, expectedQos, false);
    }

    @Test
    public void qosNotSupported() {
        Network network = createNetwork(null);
        VdsNetworkInterface iface = createNic("eth0", null, null, network.getName());

        when(qosDao.get(any(Guid.class))).thenReturn(createQos());

        qos(network, iface, null);
    }

    @Test
    public void qosOnNetwork() {
        Network network = createNetwork(null);
        VdsNetworkInterface iface = createNic("eth0", null, null, network.getName());

        Guid qosId = Guid.newGuid();
        network.setQosId(qosId);
        HostNetworkQos qos = createQos();
        when(qosDao.get(qosId)).thenReturn(qos);

        NetworkAttachment networkAttachment = createNetworkAttachment(iface, null);

        when(effectiveHostNetworkQos.getQos(any(NetworkAttachment.class), any(Network.class))).thenReturn(qos);
        when(networkAttachmentDao
            .getNetworkAttachmentByNicIdAndNetworkId(eq(networkAttachment.getNicId()), eq(network.getId())))
            .thenReturn(networkAttachment);

        qos(network, iface, qos, true);
    }

    @Test
    public void qosOnAttachment() {
        Network network = createNetwork(null);
        VdsNetworkInterface iface = createNic("eth0", null, null, network.getName());

        HostNetworkQos qos = createQos();

        NetworkAttachment networkAttachment = createNetworkAttachment(iface, qos);

        when(effectiveHostNetworkQos.getQos(any(NetworkAttachment.class), any(Network.class))).thenReturn(qos);
        when(networkAttachmentDao
            .getNetworkAttachmentByNicIdAndNetworkId(eq(networkAttachment.getNicId()), eq(network.getId())))
            .thenReturn(networkAttachment);

        qos(network, iface, qos, true);
    }

    @Test
    public void qosNeitherOnAttachmentOrNetwork() {
        Network network = createNetwork(null);
        VdsNetworkInterface iface = createNic("eth0", null, null, network.getName());

        NetworkAttachment networkAttachment = createNetworkAttachment(iface, null);

        when(networkAttachmentDao
            .getNetworkAttachmentByNicIdAndNetworkId(eq(networkAttachment.getNicId()), eq(network.getId())))
            .thenReturn(networkAttachment);

        qos(network, iface, null, true);
    }

    private NetworkAttachment createNetworkAttachment(VdsNetworkInterface iface, HostNetworkQos qos) {
        NetworkAttachment networkAttachment = new NetworkAttachment();
        networkAttachment.setId(Guid.newGuid());
        networkAttachment.setNicId(iface.getId());
        networkAttachment.setNicName(iface.getName());
        networkAttachment.setHostNetworkQos(qos);
        return networkAttachment;
    }

    /**
     * Verify that the method on the host was called, capturing the sent arguments for tests done later.
     */
    private void verifyMethodPassedToHost() {
        verify(server).setupNetworks(networksCaptor.capture(), bondingCaptor.capture(), any(HashMap.class));
    }

    /**
     * Make sure that the given bond (together with slaves) was sent to the Host.
     *
     * @param bond
     *            The bond expected to be sent.
     * @param slaves
     *            The slaves that are expected to be in the bond.
     * @return The bond's Map (which is what we send to Host) for further testing.
     */
    private Map<String, Object> assertBondWasSent(VdsNetworkInterface bond, List<VdsNetworkInterface> slaves) {
        Map bondingStruct = bondingCaptor.getValue();
        Map<String, Object> bondMap = (Map<String, Object>) bondingStruct.get(bond.getName());
        assertNotNull("Bond " + bond.getName() + " should've been sent but wasn't.", bondMap);

        List<String> nicsInStruct = (List<String>) bondMap.get(SetupNetworksVDSCommand.SLAVES);
        for (VdsNetworkInterface slave : slaves) {
            assertThat("Slave " + slave.getName() + " should've been sent but wasn't.",
                    nicsInStruct, hasItem(slave.getName()));
        }

        return bondMap;
    }

    /**
     * Make sure that the given network was sent to the host in the networks list.
     *
     * @param net
     *            The network expected to be sent.
     * @return The network's XML/RPC struct for further testing.
     */
    private Map<String, Object> assertNeworkWasSent(Network net) {
        Map networksStruct = networksCaptor.getValue();
        Map<String, Object> networkStruct = (Map<String, Object>) networksStruct.get(net.getName());
        assertNotNull("Network " + net.getName() + " should've been sent but wasn't.", networkStruct);
        return networkStruct;
    }

    private List<VdsNetworkInterface> createSlaves(VdsNetworkInterface bond) {
        int slaveCount = RandomUtils.instance().nextInt(2, 100);
        List<VdsNetworkInterface> slaves = new ArrayList<>(slaveCount);
        for (int i = 0; i < slaveCount; i++) {
            slaves.add(createNic("eth" + i, bond.getName(), null, null));
        }

        return slaves;
    }

    private SetupNetworksVDSCommand<SetupNetworksVdsCommandParameters> createCommand(
            SetupNetworksVdsCommandParameters parameters) {
        final DbFacade dbFacade = mock(DbFacade.class);
        final VdsStaticDao vdsStaticDao = mock(VdsStaticDao.class);
        final VdsDao vdsDao = mock(VdsDao.class);

        when(dbFacade.getVdsStaticDao()).thenReturn(vdsStaticDao);

        when(vdsDao.get(any(Guid.class))).thenReturn(host);

        // No way to avoid these calls by regular mocking, so must implement anonymously.
        SetupNetworksVDSCommand<SetupNetworksVdsCommandParameters> result =
            new SetupNetworksVDSCommand<SetupNetworksVdsCommandParameters>(
            parameters) {

            @Override
            protected IVdsServer initializeVdsBroker(Guid vdsId) {
                return server;
            }

            @Override
            protected DbFacade getDbFacade() {
                return dbFacade;
            }
        };


        result.networkAttachmentDao = networkAttachmentDao;
        result.effectiveHostNetworkQos = effectiveHostNetworkQos;
        result.calculateBaseNic = calculateBaseNic;

        return result;
    }

    private VdsNetworkInterface createVdsInterface(String name,
            Boolean bonded,
            String bondName,
            String bondOptions,
            NetworkBootProtocol bootProtocol,
            String networkName,
            String baseInterfaceName,
            Integer vlanId) {
        VdsNetworkInterface iface = new VdsNetworkInterface();
        iface.setId(Guid.newGuid());
        iface.setName(name);
        iface.setBonded(bonded);
        iface.setBondName(bondName);
        iface.setBondOptions(bondOptions);
        iface.setBootProtocol(bootProtocol);
        iface.setNetworkName(networkName);
        iface.setBaseInterface(baseInterfaceName);
        iface.setVlanId(vlanId);
        return iface;
    }

    private VdsNetworkInterface createBond() {
        VdsNetworkInterface bond =
            createVdsInterface("bond0", true, null, RandomUtils.instance().nextString(100), null, null, null, null);
        when(calculateBaseNic.getBaseNic(bond)).thenReturn(bond);

        return bond;
    }

    private VdsNetworkInterface createNic(String name,
            String bondName,
            NetworkBootProtocol bootProtocol,
            String network) {
        VdsNetworkInterface nic =
            createVdsInterface(name, false, bondName, null, bootProtocol, network, null, null);

        when(calculateBaseNic.getBaseNic(nic)).thenReturn(nic);

        return nic;
    }

    private VdsNetworkInterface createVlan(VdsNetworkInterface baseNic, Network net) {
        VdsNetworkInterface vlanNic = createVdsInterface(baseNic.getName() + "." + net.getVlanId(),
            false,
            null,
            null,
            NetworkBootProtocol.NONE,
            net.getName(),
            baseNic.getName(),
            net.getVlanId());

        when(calculateBaseNic.getBaseNic(vlanNic)).thenReturn(baseNic);

        return vlanNic;
    }

    private Network createNetwork(Integer vlanId) {
        return new Network("",
                "",
                Guid.newGuid(),
                RandomUtils.instance().nextString(10),
                "",
                "",
                0,
                vlanId,
                false,
                0,
                true);
    }

    private HostNetworkQos createQos() {
        HostNetworkQos qos = new HostNetworkQos();
        qos.setId(Guid.newGuid());
        qos.setOutAverageLinkshare(RandomUtils.instance().nextInt(0, 100));
        qos.setOutAverageUpperlimit(RandomUtils.instance().nextInt(0, 2000));
        qos.setOutAverageRealtime(RandomUtils.instance().nextInt(0, 2000));
        return qos;
    }
}

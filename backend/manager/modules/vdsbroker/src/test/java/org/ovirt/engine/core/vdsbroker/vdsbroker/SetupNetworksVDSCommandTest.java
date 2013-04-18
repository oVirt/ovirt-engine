package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.vdscommands.SetupNetworksVdsCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.utils.RandomUtils;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({"unchecked" , "rawtypes"})
public class SetupNetworksVDSCommandTest {

    @Mock
    private IVdsServer server;

    @Captor
    private ArgumentCaptor<Map> bondingCaptor;

    @Captor
    private ArgumentCaptor<Map> networksCaptor;

    @Test
    public void vlanOverNic() {
        Network net = createNetwork(RandomUtils.instance().nextInt(0, 4000));
        VdsNetworkInterface nic = createNic("eth0", null, NetworkBootProtocol.DHCP, null);
        VdsNetworkInterface vlan = createVlan(nic, net);

        SetupNetworksVdsCommandParameters parameters =
                new SetupNetworksVdsCommandParameters(Guid.NewGuid(),
                        Collections.singletonList(net),
                        Collections.<String> emptyList(),
                        Collections.singletonList(nic),
                        Collections.<String> emptySet(),
                        Arrays.asList(nic, vlan));

        createCommand(parameters).execute();
        verifyMethodPassedToHost();

        Map<String, String> networkStruct = assertNeworkWasSent(net);
        assertEquals(nic.getName(), networkStruct.get("nic"));
    }

    @Test
    public void vlanOverBond() {
        VdsNetworkInterface bond = createBond();
        List<VdsNetworkInterface> slaves = createSlaves(bond);
        Network net = createNetwork(RandomUtils.instance().nextInt(0, 4000));
        VdsNetworkInterface vlan = createVlan(bond, net);

        List<VdsNetworkInterface> ifaces = new ArrayList<VdsNetworkInterface>(slaves);
        ifaces.add(bond);
        ifaces.add(vlan);

        SetupNetworksVdsCommandParameters parameters =
                new SetupNetworksVdsCommandParameters(Guid.NewGuid(),
                        Collections.singletonList(net),
                        Collections.<String> emptyList(),
                        Collections.singletonList(bond),
                        Collections.<String> emptySet(),
                        ifaces);

        createCommand(parameters).execute();
        verifyMethodPassedToHost();

        assertBondWasSent(bond, slaves);
        Map<String, String> networkStruct = assertNeworkWasSent(net);
        assertEquals(bond.getName(), networkStruct.get("bonding"));
    }

    @Test
    public void networkWithDhcp() {
        Network net = createNetwork(null);
        VdsNetworkInterface nic = createNic("eth0", null, NetworkBootProtocol.DHCP, net.getName());

        SetupNetworksVdsCommandParameters parameters =
                new SetupNetworksVdsCommandParameters(Guid.NewGuid(),
                        Collections.singletonList(net),
                        Collections.<String> emptyList(),
                        Collections.<VdsNetworkInterface> emptyList(),
                        Collections.<String> emptySet(),
                        Arrays.asList(nic));

        createCommand(parameters).execute();
        verifyMethodPassedToHost();

        Map<String, String> networkStruct = assertNeworkWasSent(net);
        assertEquals(nic.getName(), networkStruct.get("nic"));
        assertEquals(SetupNetworksVDSCommand.DHCP_BOOT_PROTOCOL,
                networkStruct.get(SetupNetworksVDSCommand.BOOT_PROTOCOL));
    }

    @Test
    public void bondModified() {
        VdsNetworkInterface bond = createBond();
        List<VdsNetworkInterface> slaves = createSlaves(bond);
        List<VdsNetworkInterface> ifaces = new ArrayList<VdsNetworkInterface>(slaves);
        ifaces.add(bond);

        SetupNetworksVdsCommandParameters parameters =
                new SetupNetworksVdsCommandParameters(Guid.NewGuid(),
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
            assertTrue("Slave " + slave.getName() + " should've been sent but wasn't.",
                    nicsInStruct.contains(slave.getName()));
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
    private Map<String, String> assertNeworkWasSent(Network net) {
        Map networksStruct = networksCaptor.getValue();
        Map<String, String> networkStruct = (Map<String, String>) networksStruct.get(net.getName());
        assertNotNull("Network " + net.getName() + " should've been sent but wasn't.", networkStruct);
        return networkStruct;
    }

    private List<VdsNetworkInterface> createSlaves(VdsNetworkInterface bond) {
        int slaveCount = RandomUtils.instance().nextInt(2, 100);
        List<VdsNetworkInterface> slaves = new ArrayList<VdsNetworkInterface>(slaveCount);
        for (int i = 0; i < slaveCount; i++) {
            slaves.add(createNic("eth" + i, bond.getName(), null, null));
        }

        return slaves;
    }

    private SetupNetworksVDSCommand<SetupNetworksVdsCommandParameters> createCommand(
            SetupNetworksVdsCommandParameters parameters) {
        final DbFacade dbFacade = mock(DbFacade.class);
        final VdsDAO vdsDao = mock(VdsDAO.class);

        when(dbFacade.getVdsDao()).thenReturn(vdsDao);

        // No way to avoid these calls by regular mocking, so must implement anonymously.
        return new SetupNetworksVDSCommand<SetupNetworksVdsCommandParameters>(parameters) {

            @Override
            protected IVdsServer initializeVdsBroker(Guid vdsId) {
                return server;
            }

            @Override
            protected DbFacade getDbFacade() {
                return dbFacade;
            }
        };
    }

    private VdsNetworkInterface createVdsInterface(String name,
            Boolean bonded,
            String bondName,
            String bondOptions,
            NetworkBootProtocol bootProtocol,
            String networkName,
            Integer vlanId) {
        VdsNetworkInterface iface = new VdsNetworkInterface();
        iface.setId(Guid.NewGuid());
        iface.setName(name);
        iface.setBonded(bonded);
        iface.setBondName(bondName);
        iface.setBondOptions(bondOptions);
        iface.setBootProtocol(bootProtocol);
        iface.setNetworkName(networkName);
        iface.setVlanId(vlanId);
        return iface;
    }

    private VdsNetworkInterface createBond() {
        return createVdsInterface("bond0", true, null, RandomUtils.instance().nextString(100), null, null, null);
    }

    private VdsNetworkInterface createNic(String name,
            String bondName,
            NetworkBootProtocol bootProtocol,
            String network) {
        return createVdsInterface(name, false, bondName, null, bootProtocol, network, null);
    }

    private VdsNetworkInterface createVlan(VdsNetworkInterface iface, Network net) {
        return createVdsInterface(iface.getName() + "." + net.getVlanId(),
                false,
                null,
                null,
                NetworkBootProtocol.NONE,
                net.getName(),
                net.getVlanId());
    }

    private Network createNetwork(Integer vlanId) {
        return new Network("",
                "",
                Guid.NewGuid(),
                RandomUtils.instance().nextString(10),
                "",
                "",
                0,
                vlanId,
                false,
                0,
                true);
    }
}

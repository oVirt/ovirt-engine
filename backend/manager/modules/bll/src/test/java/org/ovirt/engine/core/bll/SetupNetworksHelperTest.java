package org.ovirt.engine.core.bll;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.dal.VdcBllMessages.NETWORK_BOND_PARAMETERS_INVALID;
import static org.ovirt.engine.core.dal.VdcBllMessages.NETWORK_INTERFACE_NAME_ALREAY_IN_USE;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.SetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.InterfaceDAO;
import org.ovirt.engine.core.dao.NetworkDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbFacade.class, Backend.class })
public class SetupNetworksHelperTest {

    @Mock
    BackendInternal backend;

    @Before
    public void testBackendSetup() {
        when(Backend.getInstance()).thenReturn(backend);
    }

    public SetupNetworksHelperTest() {
        MockitoAnnotations.initMocks(this);
        mockStatic(DbFacade.class);
        mockStatic(Backend.class);
    }


    @Test
    public void validateBonds() {
        List<VdsNetworkInterface> bonds = new ArrayList<VdsNetworkInterface>();
        Map<String, VdsNetworkInterface> bondsMap = new HashMap<String, VdsNetworkInterface>();
        List<VdsNetworkInterface> slaves = new ArrayList<VdsNetworkInterface>();
        Map<String, List<VdsNetworkInterface>> slavesMap =
                new HashMap<String, List<VdsNetworkInterface>>();
        VdsNetworkInterface bond0 = newBond("bond0");
        VdsNetworkInterface slave = new VdsNetworkInterface();
        slave.setBonded(true);
        slave.setBondName("bond0");

        bonds.add(bond0);
        slaves.add(slave);
        bondsMap.put("bond0", bond0);
        slavesMap.put("bond0", asList(slave));
        initMocks(bonds, null);

        SetupNetworksHelper validator = createHelper(mock(SetupNetworksParameters.class));

        assertFalse(validator.validateBonds(bondsMap, slavesMap));
        assertTrue(validator.getViolations().contains(VdcBllMessages.NETWORK_BOND_PARAMETERS_INVALID));
    }


    @Test
    public void validateAddingNonExistingNetwork() {
        List<VdsNetworkInterface> vdsNics = new ArrayList<VdsNetworkInterface>();
        VdsNetworkInterface nic1 = new VdsNetworkInterface();
        nic1.setNetworkName("vmnet");
        vdsNics.add(nic1);
        VdsNetworkInterface nic2 = new VdsNetworkInterface();
        nic2.setNetworkName("mgmtnet");
        vdsNics.add(nic2);

        List<network> clusterNetworks = new ArrayList<network>();
        network net1 = new network();
        net1.setname("vmnet");
        network net2 = new network();
        net2.setname("mgmtnet");
        clusterNetworks.add(net1);
        clusterNetworks.add(net2);

        List<VdsNetworkInterface> nics = new ArrayList<VdsNetworkInterface>();
        VdsNetworkInterface nicWithUknownNetowrk = new VdsNetworkInterface();
        nicWithUknownNetowrk.setNetworkName("nonExisitigNetworkName");
        nics.add(nicWithUknownNetowrk);

        initMocks(vdsNics, clusterNetworks);
        SetupNetworksParameters params = new SetupNetworksParameters();
        params.setInterfaces(nics);

        SetupNetworksHelper validator = createHelper(params);
        assertTrue(validator.validate().contains(VdcBllMessages.NETWORK_NOT_EXISTS_IN_CURRENT_CLUSTER));
    }

    @Test
    /**
     * test setup: existing vds nics: 2 slaves for bond0 and 1 master to network vmnet
     * cluster networks are "vmnet" and "ovirtmgmt"
     * new interfaces list; 2 bonds names bond0 with no slaves
     * expected: failure with message saying the interface name is already in use
     */
    public void validateUsingUniqeBondName() {
        List<VdsNetworkInterface> vdsNics = new ArrayList<VdsNetworkInterface>();
        VdsNetworkInterface nic1 = new VdsNetworkInterface();
        nic1.setNetworkName("vmnet");
        nic1.setBondName("bond0");
        nic1.setName("etho");
        vdsNics.add(nic1);
        VdsNetworkInterface nic2 = new VdsNetworkInterface();
        nic2.setNetworkName("vmnet");
        nic2.setBondName("bond0");
        nic2.setName("eth1");
        vdsNics.add(nic2);
        VdsNetworkInterface nic3 = newBond("bond0");
        nic3.setNetworkName("vmnet");
        vdsNics.add(nic3);

        List<network> clusterNetworks = new ArrayList<network>();
        network net1 = new network();
        net1.setname("vmnet");
        network net2 = new network();
        net2.setname("ovirtmgmt");
        clusterNetworks.add(net1);
        clusterNetworks.add(net2);

        List<VdsNetworkInterface> nics = new ArrayList<VdsNetworkInterface>();
        VdsNetworkInterface bond0 = newBond("bond0");
        bond0.setNetworkName("vmnet");
        nics.add(bond0);
        VdsNetworkInterface bond1 = newBond("bond0");
        bond1.setNetworkName("vmnet");
        nics.add(bond1);

        initMocks(vdsNics, clusterNetworks);
        SetupNetworksParameters params = new SetupNetworksParameters();
        params.setInterfaces(nics);

        SetupNetworksHelper helper = createHelper(params);
        List<VdcBllMessages> validate = helper.validate();
        assertTrue(validate.contains(NETWORK_INTERFACE_NAME_ALREAY_IN_USE));
    }

    @Test
    /**
     * test setup: 2 networks:  ovirtmgmt on eth0 and vmnet on eth1
     *
     * first test case: try to bond eth0 on ovirtmgmt network.
     * expected: failure on attempting to bond with 1 interface.
     *
     * second test case: add the missing interface with bonded to bond 0 and to ovirtmgmt
     * expected result: successful validation and removed network vmnet
     *
     */
    public void validateAttachigBondToNetwork() {
        List<VdsNetworkInterface> vdsNics = new ArrayList<VdsNetworkInterface>();
        VdsNetworkInterface nic1 = new VdsNetworkInterface();
        nic1.setNetworkName("ovirtmgmt");
        nic1.setName("eth0");
        vdsNics.add(nic1);
        VdsNetworkInterface nic2 = new VdsNetworkInterface();
        nic2.setNetworkName("vmnet");
        vdsNics.add(nic2);
        nic2.setName("eth1");

        List<network> clusterNetworks = new ArrayList<network>();
        network net1 = new network();
        net1.setname("ovirtmgmt");
        network net2 = new network();
        net2.setname("vmnet");
        clusterNetworks.add(net1);
        clusterNetworks.add(net2);

        List<VdsNetworkInterface> nics = new ArrayList<VdsNetworkInterface>();
        VdsNetworkInterface bond0 = newBond("bond0");
        bond0.setNetworkName("ovirtmgmt");
        bond0.setGateway("1.1.1.1");
        nics.add(bond0);
        VdsNetworkInterface slave1 = new VdsNetworkInterface();
        slave1.setName("eth0");
        slave1.setBondName("bond0");
        slave1.setGateway("1.1.1.1");
        slave1.setNetworkName("ovirtmgmt");
        nics.add(slave1);

        initMocks(vdsNics, clusterNetworks);
        SetupNetworksParameters params = new SetupNetworksParameters();
        params.setInterfaces(nics);

        SetupNetworksHelper helper = createHelper(params);
        List<VdcBllMessages> validate = helper.validate();
        assertTrue(validate.contains(NETWORK_BOND_PARAMETERS_INVALID));

        VdsNetworkInterface slave2 = new VdsNetworkInterface();
        slave2.setBondName("bond0");
        slave2.setNetworkName("ovirtmgmt");
        slave2.setName("eth1");

        nics.add(slave2);
        helper = createHelper(params);
        validate = helper.validate();
        assertTrue(validate.isEmpty());
        assertTrue(helper.getRemoveNetworks().get(0).getname().equals("vmnet"));

    }

    @Test
    /**
     * test setup: 2 existing cluster networks, red and blue
     * test case: sending "red" as the network to add
     * expected: network blue should return from the function
     */
    public void extractRemovedNetwork() {
        initMocks(null, null);
        SetupNetworksHelper helper = createHelper(new SetupNetworksParameters());
        String[] networkNames = { "red" };
        Map<String, network> clusterNetworksMap = new HashMap<String, network>();
        clusterNetworksMap.put("red", new network(null, null, null, "red", null, null, 1, 100, false));
        clusterNetworksMap.put("blue", new network(null, null, null, "blue", null, null, 1, 100, false));

        List<network> removeNetworks =
                helper.extractRemoveNetworks(new HashSet<String>(asList(networkNames)),
                        clusterNetworksMap,
                        Arrays.asList("nonAttachedNetwork"));
        assertTrue(removeNetworks.get(0).getname().equals("blue"));
    }

    @Test
    /**
     * test setup: 1 bond with no network name and empty bond list
     * test case 1 : try to add the current bond to the bonds map
     * expected: bond is not added because it doesn't have a networkName
     *
     * test case 2: try to add a bond with network "pink"
     * expected: bonds added to map
     *
     * test case 3: try to add a bond with the same name
     * expected: bond is not added to map
     */
    public void extractBond() {
        initMocks(null, null);
        SetupNetworksHelper helper = createHelper(new SetupNetworksParameters());

        Map<String, VdsNetworkInterface> bonds = new HashMap<String, VdsNetworkInterface>();
        String name = "bond5";
        VdsNetworkInterface iface = newBond(name);
        iface.setNetworkName("");

        helper.extractBond(bonds, iface, name);
        assertTrue(bonds.isEmpty());

        iface.setNetworkName("pink");
        helper.extractBond(bonds, iface, name);
        assertTrue(bonds.containsKey(name));

        helper.extractBond(bonds, iface, name);
        assertTrue(bonds.size() == 1);
    }

    @Test
    /**
     * test case 1: try to add slave of bond7 to the map
     * expected: slaves map size is now 2
     *
     * test case2: try to add another slave of bond2
     * expected: the size of list of nics under bond2 key is now 2
     */
    public void extractBondSlave() {
        initMocks(null, null);
        SetupNetworksHelper helper = createHelper(new SetupNetworksParameters());

        VdsNetworkInterface iface = new VdsNetworkInterface();
        iface.setBondName("bond7");
        Map<String, List<VdsNetworkInterface>> bondSlaves = new HashMap<String, List<VdsNetworkInterface>>();
        bondSlaves.put("bond2", asList(new VdsNetworkInterface()));

        helper.extractBondSlave(bondSlaves, iface, "bond7");
        assertTrue(bondSlaves.size() == 2);

        iface.setBondName("bond2");
        helper.extractBondSlave(bondSlaves, iface, "bond2");
        assertTrue(bondSlaves.get("bond2").size() == 2);
    }


    @Test
    /**
     * test setup: 2 existing bonds, "bond3" and "bond4"
     * test case: send bond "bond3"
     * expected: bond4 is extracted to the removeBonds list
     */
    public void extractRemovedBonds() {
        initMocks(null, null);
        SetupNetworksHelper helper = createHelper(new SetupNetworksParameters());

        Map<String, VdsNetworkInterface> bonds = new HashMap<String, VdsNetworkInterface>();
        Map<String, VdsNetworkInterface> existingIfaces = new HashMap<String, VdsNetworkInterface>();

        VdsNetworkInterface bond3 = newBond("bond3");
        bonds.put(bond3.getName(), bond3);

        VdsNetworkInterface existingBond1 = newBond("bond3");
        VdsNetworkInterface existingBond2 = newBond("bond4");
        existingIfaces.put(existingBond1.getName(), existingBond1);
        existingIfaces.put(existingBond2.getName(), existingBond2);

        List<VdsNetworkInterface> removedBonds = helper.extractRemovedBonds(bonds, existingIfaces);
        assertTrue(removedBonds.get(0).getName().equals("bond4"));
    }


    private VdsNetworkInterface newBond(String name) {
        VdsNetworkInterface bond = new VdsNetworkInterface();
        bond.setBonded(Boolean.TRUE);
        bond.setBondOptions("options");
        bond.setName(name);
        return bond;
    }


    private SetupNetworksHelper createHelper(SetupNetworksParameters params) {
        SetupNetworksHelper validator = new SetupNetworksHelper(params, Guid.Empty);
        return spy(validator);
    }

    private void initMocks(final List<VdsNetworkInterface> nics, final List<network> networks) {
        final Backend backendMock = new Backend() {
            @Override
            public VDSBrokerFrontend getResourceManager() {
                return null;
            }
        };

        final DbFacade facadeMock = new DbFacade() {

            private final VDS vds = new VDS();

            @Override
            public VdsDAO getVdsDAO() {

                final VdsDAO vdsDao = Mockito.mock(VdsDAO.class);
                Mockito.when(vdsDao.get(any(Guid.class))).thenReturn(vds);
                return vdsDao;
            }

            @Override
            public NetworkDAO getNetworkDAO() {

                final NetworkDAO networkDao = Mockito.mock(NetworkDAO.class);
                Mockito.when(networkDao.getAllForCluster(any(Guid.class))).thenReturn(networks);
                return networkDao;
            }

            @Override
            public InterfaceDAO getInterfaceDAO() {
                final InterfaceDAO interfaceDao = Mockito.mock(InterfaceDAO.class);
                Mockito.when(interfaceDao.getAllInterfacesForVds(any(Guid.class))).thenReturn(nics);
                return interfaceDao;
            }

        };

        PowerMockito.mockStatic(Backend.class);
        Mockito.when(Backend.getInstance()).thenReturn(backendMock);

        PowerMockito.mockStatic(DbFacade.class);
        Mockito.when(DbFacade.getInstance()).thenReturn(facadeMock);
    }
}

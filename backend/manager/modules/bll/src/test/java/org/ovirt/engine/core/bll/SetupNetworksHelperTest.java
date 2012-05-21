package org.ovirt.engine.core.bll;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.SetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.InterfaceDAO;
import org.ovirt.engine.core.dao.NetworkDAO;
import org.ovirt.engine.core.dao.VdsDAO;

@RunWith(MockitoJUnitRunner.class)
public class SetupNetworksHelperTest {

    @Mock
    private VdsDAO vdsDAO;

    @Mock
    private NetworkDAO networkDAO;

    @Mock
    private InterfaceDAO interfaceDAO;

    @Mock
    private DbFacade facadeMock;

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
        slavesMap.put("bond0", Arrays.asList(slave));

        SetupNetworksHelper validator = createHelper(mock(SetupNetworksParameters.class));
        initDaoMocks(bonds, null, validator);

        assertFalse(validator.validateBonds(bondsMap, slavesMap));
        assertTrue(validator.getViolations().contains(VdcBllMessages.NETWORK_BOND_PARAMETERS_INVALID));
    }

    @Test
    public void validateAddingNonExistingNetwork() {
        List<VdsNetworkInterface> vdsNics = new ArrayList<VdsNetworkInterface>();
        VdsNetworkInterface nic1 = new VdsNetworkInterface();
        nic1.setNetworkName("vmnet");
        nic1.setName("nic1");
        vdsNics.add(nic1);
        VdsNetworkInterface nic2 = new VdsNetworkInterface();
        nic2.setNetworkName("mgmtnet");
        nic2.setName("nic2");
        vdsNics.add(nic2);
        VdsNetworkInterface nic3 = new VdsNetworkInterface();
        nic3.setName("nic3");
        vdsNics.add(nic3);

        List<network> clusterNetworks = new ArrayList<network>();
        network net1 = new network();
        net1.setname("vmnet");
        network net2 = new network();
        net2.setname("mgmtnet");
        clusterNetworks.add(net1);
        clusterNetworks.add(net2);

        List<VdsNetworkInterface> nics = new ArrayList<VdsNetworkInterface>();
        VdsNetworkInterface nicWithUnknownNetwork = new VdsNetworkInterface();
        nicWithUnknownNetwork.setNetworkName("nonExisitigNetworkName");
        nicWithUnknownNetwork.setName("nic3");
        nics.add(nicWithUnknownNetwork);

        SetupNetworksParameters params = new SetupNetworksParameters();
        params.setInterfaces(nics);

        SetupNetworksHelper validator = createHelper(params);
        initDaoMocks(vdsNics, clusterNetworks, validator);
        assertTrue(validator.validate().contains(VdcBllMessages.NETWORK_NOT_EXISTS_IN_CURRENT_CLUSTER));
    }

    /**
     * test setup: existing vds nics: 2 slaves for bond0 and 1 master to network vmnet<br>
     * cluster networks are "vmnet" and "ovirtmgmt"<br>
     * new interfaces list; 2 bonds names bond0 with no slaves<br>
     * expected: failure with message saying the interface name is already in use
     */
    @Test
    public void validateUsingUniqueBondName() {
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

        SetupNetworksParameters params = new SetupNetworksParameters();
        params.setInterfaces(nics);

        SetupNetworksHelper helper = createHelper(params);
        initDaoMocks(vdsNics, clusterNetworks, helper);
        List<VdcBllMessages> validate = helper.validate();
        assertTrue(validate.contains(VdcBllMessages.NETWORK_INTERFACE_NAME_ALREADY_IN_USE));
    }

    /**
     * test setup: 2 networks: ovirtmgmt on eth0 and vmnet on eth1<br>
     * <br>
     * first test case: try to bond eth0 on ovirtmgmt network.<br>
     * expected: failure on attempting to bond with 1 interface.<br>
     * <br>
     * second test case: add the missing interface with bonded to bond 0 and to ovirtmgmt<br>
     * expected result: successful validation and removed network vmnet
     *
     */
    @Test
    public void validateAttachingBondToNetwork() {
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

        SetupNetworksParameters params = new SetupNetworksParameters();
        params.setInterfaces(nics);

        SetupNetworksHelper helper = createHelper(params);
        initDaoMocks(vdsNics, clusterNetworks, helper);
        List<VdcBllMessages> validate = helper.validate();
        assertTrue(validate.contains(VdcBllMessages.NETWORK_BOND_PARAMETERS_INVALID));

        VdsNetworkInterface slave2 = new VdsNetworkInterface();
        slave2.setBondName("bond0");
        slave2.setNetworkName("ovirtmgmt");
        slave2.setName("eth1");

        nics.add(slave2);
        helper = createHelper(params);
        initDaoMocks(vdsNics, clusterNetworks, helper);
        validate = helper.validate();
        assertTrue(validate.isEmpty());
        assertTrue(helper.getRemoveNetworks().get(0).equals("vmnet"));

    }

    /**
     * test setup: 2 existing cluster networks, red and blue, only blue is currently attached to the host<br>
     * test case: sending "red" as the network to add<br>
     * expected: network blue should return from the function
     */
    @Test
    public void extractRemovedNetwork() {
        SetupNetworksHelper helper = createHelper(new SetupNetworksParameters());

        helper.extractRemoveNetworks(new HashSet<String>(Arrays.asList("red")), Arrays.asList("blue"));
        assertTrue(helper.getRemoveNetworks().get(0).equals("blue"));
    }

    /**
     * test setup: 1 bond with no network name and empty bond list<br>
     * test case 1 : try to add the current bond to the bonds map<br>
     * expected: bond is not added because it doesn't have a networkName<br>
     * <br>
     * test case 2: try to add a bond with network "pink"<br>
     * expected: bonds added to map<br>
     * <br>
     * test case 3: try to add a bond with the same name<br>
     * expected: bond is not added to map
     */
    @Test
    public void extractBond() {
        SetupNetworksHelper helper = createHelper(new SetupNetworksParameters());
        initDaoMocks(null, null, helper);

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

    /**
     * test case 1: try to add slave of bond7 to the map<br>
     * expected: slaves map size is now 2<br>
     * <br>
     * test case2: try to add another slave of bond2<br>
     * expected: the size of list of nics under bond2 key is now 2
     */
    @Test
    public void extractBondSlave() {
        SetupNetworksHelper helper = createHelper(new SetupNetworksParameters());
        initDaoMocks(null, null, helper);

        VdsNetworkInterface iface = new VdsNetworkInterface();
        iface.setBondName("bond7");
        Map<String, List<VdsNetworkInterface>> bondSlaves = new HashMap<String, List<VdsNetworkInterface>>();
        bondSlaves.put("bond2", Arrays.asList(new VdsNetworkInterface()));

        helper.extractBondSlave(bondSlaves, iface, "bond7");
        assertTrue(bondSlaves.size() == 2);

        iface.setBondName("bond2");
        helper.extractBondSlave(bondSlaves, iface, "bond2");
        assertTrue(bondSlaves.get("bond2").size() == 2);
    }

    /**
     * test setup: 2 existing bonds, "bond3" and "bond4"<br>
     * test case: send bond "bond3"<br>
     * expected: bond4 is extracted to the removeBonds list
     */
    @Test
    public void extractRemovedBonds() {
        SetupNetworksHelper helper = createHelper(new SetupNetworksParameters());

        Map<String, VdsNetworkInterface> bonds = new HashMap<String, VdsNetworkInterface>();
        List<VdsNetworkInterface> existingIfaces = new ArrayList<VdsNetworkInterface>();

        VdsNetworkInterface bond3 = newBond("bond3");
        bonds.put(bond3.getName(), bond3);

        VdsNetworkInterface existingBond1 = newBond("bond3");
        VdsNetworkInterface existingBond2 = newBond("bond4");
        existingIfaces.add(existingBond1);
        existingIfaces.add(existingBond2);
        initDaoMocks(existingIfaces, null, helper);

        List<VdsNetworkInterface> removedBonds = helper.extractRemovedBonds(bonds);
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

    private void initDaoMocks(final List<VdsNetworkInterface> nics,
            final List<network> networks,
            final SetupNetworksHelper helper) {
        when(facadeMock.getVdsDAO()).thenReturn(vdsDAO);
        when(vdsDAO.get(any(Guid.class))).thenReturn(new VDS());

        when(facadeMock.getNetworkDAO()).thenReturn(networkDAO);
        when(networkDAO.getAllForCluster(any(Guid.class))).thenReturn(networks);

        when(facadeMock.getInterfaceDAO()).thenReturn(interfaceDAO);
        when(interfaceDAO.getAllInterfacesForVds(any(Guid.class))).thenReturn(nics);

        doReturn(facadeMock).when(helper).getDbFacade();
    }
}

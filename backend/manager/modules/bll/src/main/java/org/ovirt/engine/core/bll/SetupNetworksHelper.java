package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.action.SetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.NetworkUtils;

public class SetupNetworksHelper {
    private SetupNetworksParameters params;
    private Guid vdsGroupId;
    private List<VdcBllMessages> violations = new ArrayList<VdcBllMessages>();
    private Map<String, VdsNetworkInterface> existingIfaces;
    private Map<String, network> existingClusterNetworks;

    private List<network> modifiedNetworks = new ArrayList<network>();
    private List<String> unmanagedNetworks = new ArrayList<String>();
    private List<String> removeNetworks;
    private Map<String, VdsNetworkInterface> modifiedBonds = new HashMap<String, VdsNetworkInterface>();
    private List<VdsNetworkInterface> removedBonds;

    public SetupNetworksHelper(SetupNetworksParameters parameters, Guid vdsGroupId) {
        params = parameters;
        this.vdsGroupId = vdsGroupId;
    }

    /**
     * validate and extract data from the list of interfaces sent. The general flow is:
     * <ul>
     * <li>create mapping of existing the current topology - interfaces and logical networks.
     * <li>create maps for networks bonds and bonds-slaves.
     * <li>iterate over the interfaces and extract network/bond/slave info as we go.
     * <li>validate the extracted information by using the pre-build mappings of the current topology.
     * <li>store and encapsulate the extracted lists to later be fetched by the calling command.
     * <li>error messages are aggregated
     * </ul>
     * TODO add fail-fast to exist on the first validation error.
     *
     * @return List of violations encountered (if none, list is empty).
     */
    public List<VdcBllMessages> validate() {
        Set<String> ifaceNames = new HashSet<String>();
        Set<String> bonds = new HashSet<String>();
        Set<String> attachedNetworksNames = new HashSet<String>();

        // key = master bond name, value = list of interfaces
        Map<String, List<VdsNetworkInterface>> bondSlaves = new HashMap<String, List<VdsNetworkInterface>>();

        for (VdsNetworkInterface iface : params.getInterfaces()) {
            String name = iface.getName();
            String networkName = iface.getNetworkName();
            String bondName = iface.getBondName();
            boolean bonded = isBond(iface);

            if (ifaceNames.contains(name)) {
                violations.add(VdcBllMessages.NETWORK_INTERFACE_NAME_ALREADY_IN_USE);
                continue;
            } else {
                ifaceNames.add(name);
            }

            // if its a bond extract to bonds map
            if (bonded) {
                extractBondIfModified(bonds, iface, name);
            } else {
                if (StringUtils.isNotBlank(bondName)) {
                    extractBondSlave(bondSlaves, iface, bondName);
                }

                // validate the nic exists on host
                if (!getExistingIfaces().containsKey(NetworkUtils.StripVlan(name))) {
                    violations.add(VdcBllMessages.NETWORK_INTERFACE_NOT_EXISTS);
                }
            }

            // validate and extract to network map
            if (violations.isEmpty() && StringUtils.isNotBlank(networkName)) {
                extractNetwork(attachedNetworksNames, iface, networkName);
            }
        }

        validateBonds(bonds, bondSlaves);

        detectSlaveChanges(bondSlaves);

        extractRemoveNetworks(attachedNetworksNames, getExisitingHostNetworkNames());
        this.removedBonds = extractRemovedBonds(bonds);

        return violations;
    }

    /**
     * Detect a bond that it's slaves have changed, to add to the modified bonds list.
     *
     * @param bondSlaves
     *            The bond slaves which were sent.
     */
    private void detectSlaveChanges(Map<String, List<VdsNetworkInterface>> bondSlaves) {
        for (Map.Entry<String, List<VdsNetworkInterface>> bondEntry : bondSlaves.entrySet()) {
            String bondName = bondEntry.getKey();
            if (!modifiedBonds.containsKey(bondName)) {
                for (VdsNetworkInterface bondSlave : bondEntry.getValue()) {
                    if (interfaceWasModified(bondSlave)) {
                        modifiedBonds.put(bondName, getExistingIfaces().get(bondName));
                    }
                }
            }
        }
    }

    private Map<String, network> getExistingClusterNetworks() {
        if (existingClusterNetworks == null) {
            existingClusterNetworks = Entities.entitiesByName(
                    getDbFacade().getNetworkDAO().getAllForCluster(vdsGroupId));
        }

        return existingClusterNetworks;
    }

    private Map<String, VdsNetworkInterface> getExistingIfaces() {
        if (existingIfaces == null) {
            existingIfaces = Entities.entitiesByName(
                    getDbFacade().getInterfaceDAO().getAllInterfacesForVds(params.getVdsId()));
        }

        return existingIfaces;
    }

    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    private List<String> getExisitingHostNetworkNames() {
        List<String> list = new ArrayList<String>(getExistingIfaces().size());
        for (VdsNetworkInterface iface : getExistingIfaces().values()) {
            if (StringUtils.isNotBlank(iface.getNetworkName())) {
                list.add(iface.getNetworkName());
            }
        }

        return list;
    }

    /**
     * extracting a network is done by matching the desired network name with the network details from db on
     * clusterNetworksMap. The desired network is just a key and actual network configuration is taken from the db
     * entity.
     *
     * @param attachedNetworksNames
     *            Set of names of the networks which are attached to a NIC
     * @param iface
     *            current iterated interface
     * @param networkName
     *            the current network name of iface
     */
    private void extractNetwork(Set<String> attachedNetworksNames,
            VdsNetworkInterface iface,
            String networkName) {

        // check if network exists on cluster
        if (getExistingClusterNetworks().containsKey(networkName)) {

            // prevent attaching 2 interfaces to 1 network
            if (attachedNetworksNames.contains(networkName)) {
                violations.add(VdcBllMessages.NETWROK_ALREADY_ATTACHED_TO_INTERFACE);
            } else {
                attachedNetworksNames.add(networkName);

                if (interfaceWasModified(iface)) {
                    modifiedNetworks.add(getExistingClusterNetworks().get(networkName));
                }
            }

            // Interface must exist, it was checked before and we can't reach here if it does'nt exist already.
        } else if (networkName.equals(getExistingIfaces().get(iface.getName()).getNetworkName())) {
            unmanagedNetworks.add(networkName);
        } else {
            violations.add(VdcBllMessages.NETWORK_NOT_EXISTS_IN_CURRENT_CLUSTER);
        }
    }

    /**
     * Check if the given interface was modified (or added).
     *
     * @param iface
     *            The interface to check.
     * @return <code>true</code> if the interface was changed, or is a new one. <code>false</code> if it existed and
     *         didn't change.
     */
    private boolean interfaceWasModified(VdsNetworkInterface iface) {
        return !iface.equals(getExistingIfaces().get(iface.getName()));
    }

    private boolean isBond(VdsNetworkInterface iface) {
        return Boolean.TRUE.equals(iface.getBonded());
    }

    /**
     * build mapping of the bond name - > list of slaves. slaves are interfaces with a pointer to the master bond by
     * bondName.
     *
     * @param bondSlaves
     * @param iface
     * @param bondName
     */
    protected void extractBondSlave(Map<String, List<VdsNetworkInterface>> bondSlaves,
            VdsNetworkInterface iface,
            String bondName) {
        List<VdsNetworkInterface> value = new ArrayList<VdsNetworkInterface>();
        value.add(iface);
        if (bondSlaves.containsKey(bondName)) {
            value.addAll(bondSlaves.get(bondName));
        }

        bondSlaves.put(bondName, value);
    }

    /**
     * Extract the bond to the modified bonds list if it was added or the bond interface config has changed.
     *
     * @param bonds
     *            The bond names which have already been processed.
     * @param iface
     *            The interface of the bond.
     * @param name
     *            The bond name.
     */
    protected void extractBondIfModified(Set<String> bonds, VdsNetworkInterface iface, String name) {
        if (StringUtils.isBlank(iface.getNetworkName())) {
            violations.add(VdcBllMessages.NETWROK_NOT_EXISTS);
        } else {
            if (bonds.contains(name)) {
                violations.add(VdcBllMessages.NETWORK_BOND_NAME_EXISTS);
            } else {
                bonds.add(name);
                if (interfaceWasModified(iface)) {
                    modifiedBonds.put(name, iface);
                }
            }
        }
    }

    protected List<VdsNetworkInterface> extractRemovedBonds(Set<String> bonds) {
        List<VdsNetworkInterface> removedBonds = new ArrayList<VdsNetworkInterface>();
        for (Entry<String, VdsNetworkInterface> e : getExistingIfaces().entrySet()) {
            if (isBond(e.getValue()) && !bonds.contains(e.getKey())) {
                removedBonds.add(e.getValue());
            }
        }

        return removedBonds;
    }

    protected boolean validateBonds(Set<String> bonds,
            Map<String, List<VdsNetworkInterface>> bondSlaves) {
        boolean returnValue = true;
        for (String bondName : bonds) {
            if (bondSlaves.containsKey(bondName) && bondSlaves.get(bondName).size() < 2) {
                returnValue = false;
                violations.add(VdcBllMessages.NETWORK_BOND_PARAMETERS_INVALID);
            }
        }

        return returnValue;
    }

    /**
     * Calculate the networks that should be removed - If the network was attached to a NIC and is no longer attached to
     * it, then it will be removed.
     *
     * @param networkNames
     *            The names of managed networks which are still attached to a NIC.
     * @param exisitingHostNetworksNames
     *            The names of networks that were attached to NICs before the changes.
     */
    protected void extractRemoveNetworks(Set<String> networkNames,
            List<String> exisitingHostNetworksNames) {
        removeNetworks = new ArrayList<String>();

        for (String net : exisitingHostNetworksNames) {
            if (!networkNames.contains(net) && !unmanagedNetworks.contains(net)) {
                removeNetworks.add(net);
            }
        }
    }

    public List<VdcBllMessages> getViolations() {
        return violations;
    }

    public List<network> getNetworks() {
        return modifiedNetworks;
    }

    public List<String> getRemoveNetworks() {
        return removeNetworks;
    }

    public List<VdsNetworkInterface> getBonds() {
        return new ArrayList<VdsNetworkInterface>(modifiedBonds.values());
    }

    public List<VdsNetworkInterface> getRemovedBonds() {
        return removedBonds;
    }
}

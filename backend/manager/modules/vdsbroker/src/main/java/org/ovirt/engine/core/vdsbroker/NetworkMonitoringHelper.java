package org.ovirt.engine.core.vdsbroker;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkMonitoringHelper {
    /**
     * Determine which of the given NICs is problematic - a problematic NIC is considered to be a NIC which it's state
     * is down and it is the underlying interface of a required network.
     *
     * @param interfaces
     *            The NICs to check.
     * @param clusterNetworks
     *            The cluster's networks.
     * @return A pair of a list of the names of the NICs which are problematic, and a list of the names of the networks
     *         which are required by these NICs.
     */
    public static Pair<List<String>, List<String>> determineProblematicNics(List<VdsNetworkInterface> interfaces,
            List<Network> clusterNetworks) {
        Map<String, Boolean> bondsWithStatus = new HashMap<String, Boolean>();
        List<String> networks = new ArrayList<String>();
        List<String> brokenNics = new ArrayList<String>();
        Map<String, List<String>> bondsWithListOfNics = new HashMap<String, List<String>>();

        Map<String, Network> networksByName = NetworkUtils.networksByName(clusterNetworks);

        for (VdsNetworkInterface iface : interfaces) {

            // Handle nics that are non bonded and not vlan over bond
            if (isRequiredInterfaceDown(networksByName, interfaces, iface)) {
                brokenNics.add(iface.getName());
                networks.add(iface.getNetworkName());
            }

            // Handle bond nics
            if (iface.getBondName() != null) {
                populate(bondsWithStatus, interfaces, clusterNetworks, networks, bondsWithListOfNics, iface);
            }
        }

        // check the bond statuses, if one is down we mark it as broken
        // only if we didn't already mark a NIC as broken
        if (brokenNics.isEmpty()) {
            for (Map.Entry<String, Boolean> entry : bondsWithStatus.entrySet()) {
                if (!entry.getValue()) {
                    // add the nics name for audit log
                    for (String name : bondsWithListOfNics.get(entry.getKey())) {
                        brokenNics.add(name);
                    }
                }
            }
        }

        return new Pair<List<String>, List<String>>(brokenNics, networks);
    }

    private static void populate(Map<String, Boolean> bondsWithStatus,
            List<VdsNetworkInterface> interfaces,
            List<Network> clusterNetworks,
            List<String> networks,
            Map<String, List<String>> bondsWithListOfNics,
            VdsNetworkInterface iface) {
        Pair<Boolean, String> retVal =
                isRequiredNetworkInCluster(iface.getBondName(), interfaces, clusterNetworks);
        String networkName = retVal.getSecond();
        if (retVal.getFirst()) {
            if (!bondsWithStatus.containsKey(iface.getBondName())) {
                bondsWithStatus.put(iface.getBondName(), false);
            }
            // It is enough for at least one of the interfaces of the bond to be up
            bondsWithStatus.put(iface.getBondName(),
                    bondsWithStatus.get(iface.getBondName())
                            || (iface.getStatistics().getStatus() == InterfaceStatus.UP));

            if (!networks.contains(networkName)
                    && !bondsWithStatus.containsKey(iface.getName())) {
                networks.add(networkName);
            }
            // we remove the network from the audit log if the bond
            // is active
            else if (networks.contains(networkName) && bondsWithStatus.get(iface.getBondName())) {
                networks.remove(networkName);
            }
            if (!bondsWithListOfNics.containsKey(iface.getBondName())) {
                bondsWithListOfNics.put(iface.getBondName(), new ArrayList<String>());
            }
            bondsWithListOfNics.get(iface.getBondName()).add(iface.getName());
        }
    }

    /**
     * check if an interface implementing a required cluster network is down
     *
     * @param networksByName
     * @param iface
     */
    private static boolean isRequiredInterfaceDown(Map<String, Network> networksByName,
            List<VdsNetworkInterface> interfaces,
            VdsNetworkInterface iface) {
        if (iface.getStatistics().getStatus() != InterfaceStatus.UP
                && iface.getNetworkName() != null
                && iface.getBonded() == null
                && !isBondOrVlanOverBond(iface, interfaces)
                && networksByName.containsKey(iface.getNetworkName())) {

            Network net = networksByName.get(iface.getNetworkName());
            if (net.getCluster().getStatus() == NetworkStatus.OPERATIONAL && net.getCluster().isRequired()
                    && (iface.getVlanId() == null || !isVlanInterfaceUp(iface, interfaces))) {
                return true;
            }
        }
        return false;
    }

    // method get bond name, list of cluster network - checks if the specified
    // bonds network is in the clusterNetworks,
    // if so return true and networkName of the bonds
    private static Pair<Boolean, String> isRequiredNetworkInCluster(String bondName,
            List<VdsNetworkInterface> interfaces,
            List<Network> clusterNetworks) {
        Pair<Boolean, String> retVal = new Pair<Boolean, String>();
        for (VdsNetworkInterface iface : interfaces) {
            if (iface.getName().equals(bondName)) {
                for (Network net : clusterNetworks) {
                    // If this is the network on the bond, or on a vlan over the bond, and the network is required
                    // we want to check this network
                    if ((net.getName().equals(iface.getNetworkName())
                            || isVlanOverBondNetwork(bondName, net.getName(), interfaces))
                            && net.getCluster().isRequired()) {
                        retVal.setFirst(true);
                        retVal.setSecond(net.getName());
                        return retVal;
                    }
                }
                retVal.setFirst(false);
                return retVal;
            }
        }
        retVal.setFirst(false);
        return retVal;
    }

    // IsBond return true if the interface is bond,
    // it also check if it's vlan over bond and return true in that case
    // i.e. it return true in case of bond0 and bond0.5
    private static boolean isBondOrVlanOverBond(VdsNetworkInterface iface, List<VdsNetworkInterface> interfaces) {
        if (iface.getBonded() != null && iface.getBonded() == true) {
            return true;
        }

        // check if vlan over bond i.e if we are in bond0.5 we look for bond0
        String name = NetworkUtils.getVlanInterfaceName(iface.getName());
        if (name == null) {
            return false;
        }

        for (VdsNetworkInterface i : interfaces) {
            if (name.equals(i.getName())) {
                return (i.getBonded() != null && i.getBonded() == true);
            }
        }
        return false;
    }

    // function check if vlan over bond connected to network
    // i.e. if we have bond0 that have vlan #5 like:
    // bond0 and bond0.5
    // bond0 is not connectet to network just the bond0.5 is connected to network
    // and this method check for that case
    private static boolean isVlanOverBondNetwork(String bondName,
            String networkName,
            List<VdsNetworkInterface> interfaces) {
        for (VdsNetworkInterface iface : interfaces) {
            String name = NetworkUtils.getVlanInterfaceName(iface.getName());
            // this if check if the interface is vlan
            if (name == null) {
                continue;
            } else if (name.equals(bondName)
                    && networkName.equals(iface.getNetworkName())) {
                return true;
            }
        }
        return false;
    }

    // If vlan we search if the interface is up (i.e. not eth2.5 we look for eth2)
    private static boolean isVlanInterfaceUp(VdsNetworkInterface vlan, List<VdsNetworkInterface> interfaces) {
        String[] tokens = vlan.getName().split("[.]");
        if (tokens.length == 1) {
            // not vlan
            return true;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length - 1; i++) {
            sb.append(tokens[i])
                    .append(".");
        }
        String ifaceName = StringUtils.stripEnd(sb.toString(), ".");
        for (VdsNetworkInterface iface : interfaces) {
            if (iface.getName().equals(ifaceName)) {
                return iface.getStatistics().getStatus() == InterfaceStatus.UP;
            }
        }

        // not suppose to get here
        return false;
    }
}

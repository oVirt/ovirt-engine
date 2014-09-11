package org.ovirt.engine.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ObjectUtils;

public final class NetworkUtils {
    public static final String OS_REFERENCE_TO_MACHINE_NAME = "HOSTNAME";

    public static String getDefaultManagementNetworkName() {
        return Config.<String> getValue(ConfigValues.DefaultManagementNetwork);
    }

    public static Integer getDefaultMtu() {
        return Config.<Integer> getValue(ConfigValues.DefaultMTU);
    }

    /**
    * Returns the underlying interface name of a given nic
    *
    * @param nic
    *
    * @return Base interface name if the nic is a vlan device.
    *         Otherwise, the name of the nic
    */
    public static String stripVlan(VdsNetworkInterface nic) {
        return NetworkUtils.isVlan(nic) ? nic.getBaseInterface() : nic.getName();
    }

    public static boolean isBondVlan(List<VdsNetworkInterface> interfaces, VdsNetworkInterface iface) {
        if (isVlan(iface)) {
            for (VdsNetworkInterface i : interfaces) {
                if (Boolean.TRUE.equals(i.getBonded()) && i.getName().equals(iface.getBaseInterface())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if the proposed interface represents a VLAN of the given interface name or is equal to it.<br>
     * If either of the parameters is null, <code>false</code> is returned.
     *
     * @param proposedIface
     *            The interface to check if it's a VLAN of the other interface or it is the other interface.
     * @param iface
     *            The interface to check for.
     *
     * @return <code>true</code> if the proposed interface is a VLAN on the interface or if it is the other interface,
     *         <code>false</code> otherwise.
     */
    public static boolean interfaceBasedOn(VdsNetworkInterface proposedIface, String iface) {
        return iface != null && proposedIface != null && iface.equals(stripVlan(proposedIface));
    }

    public static boolean interfaceHasVlan(VdsNetworkInterface iface, List<VdsNetworkInterface> allIfaces) {
        for (VdsNetworkInterface i : allIfaces) {
            if (isVlan(i) && interfaceBasedOn(i, iface.getName())) {
                return true;
            }
        }
        return false;
    }

    public static Map<String, Network> networksByName(List<Network> networks) {
        if (!networks.isEmpty()) {
            Map<String, Network> byName = new HashMap<String, Network>();
            for (Network net : networks) {
                byName.put(net.getName(), net);
            }
            return byName;
        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * filter networks which are not VM networks from the newtorkNames list
     * @param networks
     *            logical networks
     * @param networkNames
     *            target names to match non-VM networks upon
     * @return
     */
    public static List<String> filterNonVmNetworkNames(List<Network> networks, Set<String> networkNames) {
        List<String> list = new ArrayList<String>();
        for (Network net : networks) {
            if (!net.isVmNetwork() && networkNames.contains(net.getName())) {
                list.add(net.getName());
            }
        }
        return list;
    }

    /**
     * Fill network details for the given network devices from the given networks.<br>
     * {@link VdsNetworkInterface.NetworkDetails#isInSync()} will be <code>true</code> IFF the logical network
     * properties are exactly the same as those defined on the network device.
     * @param networks
     *            The networks definitions to fill the details from.
     * @param ifaces
     *            The network devices to update.
     */
    public static VdsNetworkInterface.NetworkImplementationDetails calculateNetworkImplementationDetails(
            Network network,
            HostNetworkQos qos,
            VdsNetworkInterface iface) {
        if (StringUtils.isEmpty(iface.getNetworkName())) {
            return null;
        }

        if (network != null) {
            if (isNetworkInSync(iface, network, qos)) {
                return new VdsNetworkInterface.NetworkImplementationDetails(true, true);
            } else {
                return new VdsNetworkInterface.NetworkImplementationDetails(false, true);
            }
        } else {
            return new VdsNetworkInterface.NetworkImplementationDetails();
        }
    }

    /**
     * Check whether the interface has any QoS configured on it, whether via its attached network or overridden.
     *
     * @param iface
     *            The network interface.
     * @param network
     *            The network attached to the interface.
     * @return true iff any QoS is applied to the interface.
     */
    public static boolean qosConfiguredOnInterface(VdsNetworkInterface iface, Network network) {
        if (iface.isQosOverridden()) {
            return iface.getQos() != null;
        } else {
            return network != null && network.getQosId() != null;
        }
    }

    public static boolean isNetworkInSync(VdsNetworkInterface iface, Network network, HostNetworkQos qos) {
        return ((network.getMtu() == 0 && iface.getMtu() == getDefaultMtu()) || iface.getMtu() == network.getMtu())
                && Objects.equals(iface.getVlanId(), network.getVlanId())
                && iface.isBridged() == network.isVmNetwork()
                && (isQosInSync(iface, qos) || iface.isQosOverridden());
    }

    private static boolean isQosInSync(VdsNetworkInterface iface, HostNetworkQos networkQos) {
        HostNetworkQos ifaceQos = iface.getQos();
        return qosParametersEqual(ifaceQos, networkQos);
    }

    public static boolean qosParametersEqual(HostNetworkQos qos, HostNetworkQos otherQos) {
        if (qos == otherQos) {
            return true;
        } else if (qos == null || otherQos == null) {
            return false;
        } else {
            return ObjectUtils.objectsEqual(qos.getOutAverageLinkshare(), otherQos.getOutAverageLinkshare())
                    && ObjectUtils.objectsEqual(qos.getOutAverageUpperlimit(), otherQos.getOutAverageUpperlimit())
                    && ObjectUtils.objectsEqual(qos.getOutAverageRealtime(), otherQos.getOutAverageRealtime());
        }
    }

    /**
     * Returns true if a given network is non-VM network with no Vlan tagging, else false.
     *
     * @param network
     *            The network to check
     */
    public static boolean isNonVmNonVlanNetwork(Network network) {
        return !network.isVmNetwork() && !isVlan(network);
    }

    /**
     * Determine if the network is management network.
     *
     * @param net
     *            The network to check.
     * @return <code>true</code> iff the network is a management network.
     */
    public static boolean isManagementNetwork(Network net) {
        return isManagementNetwork(net.getName());
    }

    /**
     * Determine if the network is management network.
     *
     * @param networkName
     *            The network name to check.
     * @return <code>true</code> iff the network is a management network.
     */
    public static boolean isManagementNetwork(String networkName) {
        return getDefaultManagementNetworkName().equals(networkName);
    }

    /**
     * Determine if a given network is configured as a vlan
     *
     * @param network
     *            the network to check.
     * @return <code>true</code> iff the network is a vlan.
     */
    public static boolean isVlan(Network network) {
        return network.getVlanId() != null;
    }

    /**
     * Determine if a given network interface is a vlan device
     *
     * @param nic
     *            the nic to check.
     * @return <code>true</code> iff the nic is a vlan.
     */
    public static boolean isVlan(VdsNetworkInterface nic) {
        return nic.getVlanId() != null;
    }

    /**
     * Determine if a given network is labeled
     *
     * @param network
     *            the network to check.
     * @return <code>true</code> iff the network is labeled.
     */
    public static boolean isLabeled(Network network) {
        return network.getLabel() != null;
    }

    /**
     * Determine if a given network interface is labeled
     *
     * @param nic
     *            the nic to check.
     * @return <code>true</code> iff the nic is labeled.
     */
    public static boolean isLabeled(VdsNetworkInterface nic) {
        return nic.getLabels() != null && !nic.getLabels().isEmpty();
    }

    /**
     * Determine if a given network interface should be configured on hosts
     *
     * @param network
     *            the network to check.
     * @return <code>true</code> iff the network is labeled and not an external network.
     */
    public static boolean isConfiguredByLabel(Network network) {
        return isLabeled(network) && !network.isExternal();
    }

    /**
     * Constructs the vlan device name in the format of "{nic name}.{vlan-id}"
     *
     * @param underlyingNic
     *            the device on top the vlan device is created
     * @param network
     *            the network which holds the vlan-id
     * @return a name representing the vlan device
     */
    public static String constructVlanDeviceName(VdsNetworkInterface underlyingNic, Network network) {
        return underlyingNic.getName() + "." + network.getVlanId();
    }

    /**
     * Returns the cluster's display network
     */
    public static Network getDisplayNetwork(Collection<Network> clusterNetworks) {
        Network displayNetwork = null;

        for (Network network : clusterNetworks) {
            if (network.getCluster().isDisplay()) {
                displayNetwork = network;
                break;
            }
        }

        return displayNetwork;
    }

    /**
     * @return A unique host name representation
     */
    public static String getUniqueHostName(VDS host) {
        return host.getHostName() + "-" + DigestUtils.md5Hex(host.getId().toByteArray()).substring(0, 6);
    }
}

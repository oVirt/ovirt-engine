package org.ovirt.engine.core.utils;

import static java.util.stream.Collectors.toList;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.IpV6Address;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.network.function.NicToIpv4AddressFunction;
import org.ovirt.engine.core.utils.network.function.NicToIpv6AddressFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NetworkUtils {
    private static final Logger log = LoggerFactory.getLogger(NetworkUtils.class);
    public static Integer getDefaultMtu() {
        return Config.<Integer> getValue(ConfigValues.DefaultMTU);
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
        return iface != null && proposedIface != null && iface.equals(NetworkCommonUtils.stripVlan(proposedIface));
    }

    public static boolean interfaceHasVlan(VdsNetworkInterface iface, List<VdsNetworkInterface> allIfaces) {
        for (VdsNetworkInterface i : allIfaces) {
            if (NetworkCommonUtils.isVlan(i) && interfaceBasedOn(i, iface.getName())) {
                return true;
            }
        }
        return false;
    }

    public static Map<String, Network> networksByName(List<Network> networks) {
        if (!networks.isEmpty()) {
            Map<String, Network> byName = new HashMap<>();
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
     */
    public static List<String> filterNonVmNetworkNames(List<Network> networks, Set<String> networkNames) {
        List<String> list = new ArrayList<>();
        for (Network net : networks) {
            if (!net.isVmNetwork() && networkNames.contains(net.getName())) {
                list.add(net.getName());
            }
        }
        return list;
    }

    /**
     * Check whether the network attachment has any QoS configured on it, whether via its network or overridden.
     *
     * @param networkAttachment
     *            The network interface.
     * @param network
     *            The network attached to the interface.
     * @return true iff any QoS is applied to the interface.
     */
    public static boolean qosConfiguredOnInterface(NetworkAttachment networkAttachment, Network network) {
        if (networkAttachment != null && networkAttachment.isQosOverridden()) {
            return networkAttachment.getHostNetworkQos() != null && !networkAttachment.getHostNetworkQos().isEmpty();
        } else {
            return network != null && network.getQosId() != null;
        }
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

    /**
     * resolve the host ip address
     *
     * @param host
     *            the host which it's address is about to be resolved
     * @return if succeeded, string representing the host ip, null otherwise
     */
    public static String getHostIp(VDS host) {
        try {
            final InetAddress address = InetAddress.getByName(host.getHostName());
            return address.getHostAddress().trim();
        } catch (UnknownHostException ex) {
            final String msg = "Failed to resolve host ip by name '{}'";
            log.warn(msg, " Details: '{}' ", host.getHostName(), ex.getCause());
            log.debug(msg, host.getHostName(), ex);
            return null;
        }
    }

    /**
     * returns whether the network has a role in the cluster
     *
     * @return whether the network has a role (display, migration or gluster) in the cluster
     */
    public static boolean isRoleNetwork(NetworkCluster networkCluster) {
        return networkCluster.isDisplay() || networkCluster.isMigration() || networkCluster.isGluster();
    }

    public static IpConfiguration createIpConfigurationFromVdsNetworkInterface(VdsNetworkInterface nic) {
        if (nic == null) {
            return NetworkCommonUtils.createDefaultIpConfiguration();
        }

        final List<IPv4Address> iPv4Addresses = Stream.of(nic).map(new NicToIpv4AddressFunction()).collect(toList());
        final List<IpV6Address> ipV6Addresses = Stream.of(nic).map(new NicToIpv6AddressFunction()).collect(toList());

        IpConfiguration ipConfiguration = new IpConfiguration();
        ipConfiguration.setIPv4Addresses(iPv4Addresses);
        ipConfiguration.setIpV6Addresses(ipV6Addresses);

        return ipConfiguration;
    }

    public static <E extends VmNetworkInterface> Map<Guid, List<E>> vmInterfacesByVmId(List<E> vnics) {
        return vnics == null
                ? Collections.emptyMap()
                : vnics.stream().collect(Collectors.groupingBy(VmNetworkInterface::getVmId));
    }

    public static <E extends VdsNetworkInterface> Map<String, E> hostInterfacesByNetworkName(Collection<E> hostNics) {
        return hostNics == null
                ? Collections.emptyMap()
                : hostNics.stream()
                        .filter(hostNic -> hostNic.getNetworkName() != null)
                        .collect(Collectors.toMap(VdsNetworkInterface::getNetworkName, Function.<E>identity()));
    }
}

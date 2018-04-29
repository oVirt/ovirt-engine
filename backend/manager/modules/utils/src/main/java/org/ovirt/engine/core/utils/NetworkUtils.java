package org.ovirt.engine.core.utils;

import static java.util.stream.Collectors.toList;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.IpV6Address;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.utils.network.function.NicToIpv4AddressFunction;
import org.ovirt.engine.core.utils.network.function.NicToIpv6AddressFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NetworkUtils {
    private static final Logger log = LoggerFactory.getLogger(NetworkUtils.class);
    private static final Pattern VALID_VDS_NAME_PATTERN = Pattern.compile(
            String.format("^[0-9a-zA-Z_-]{1,%d}$", BusinessEntitiesDefinitions.HOST_NIC_NAME_LENGTH));
    private static Integer getHostDefaultMtu() {
        return Config.<Integer> getValue(ConfigValues.DefaultMTU);
    }

    private static Integer getVmDefaultMtu(Network network) {
        return network.isTunnelled() ?
                Config.<Integer> getValue(ConfigValues.TunnelledDefaultMTU) : getHostDefaultMtu();
    }

    public static int getVmMtuActualValue(Network network) {
        return network.isDefaultMtu() ? NetworkUtils.getVmDefaultMtu(network) : network.getMtu();
    }

    public static int getHostMtuActualValue(Network network) {
        return network.isDefaultMtu() ? NetworkUtils.getHostDefaultMtu() : network.getMtu();
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
        return allIfaces.stream().anyMatch(i -> NetworkCommonUtils.isVlan(i) && interfaceBasedOn(i, iface.getName()));
    }

    public static Map<String, Network> networksByName(List<Network> networks) {
        return networks.stream().collect(Collectors.toMap(Network::getName, Function.identity()));
    }

    /**
     * filter networks which are not VM networks from the newtorkNames list
     * @param networks
     *            logical networks
     * @param networkNames
     *            target names to match non-VM networks upon
     */
    public static List<String> filterNonVmNetworkNames(List<Network> networks, Set<String> networkNames) {
        return networks.stream()
                .filter(net -> !net.isVmNetwork() && networkNames.contains(net.getName()))
                .map(Network::getName)
                .collect(Collectors.toList());
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
     * Returns the cluster's display network
     */
    public static Network getDisplayNetwork(Collection<Network> clusterNetworks) {
        return clusterNetworks.stream().filter(n -> n.getCluster().isDisplay()).findFirst().orElse(null);
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
     * resolve the ip address the url references
     *
     * @param url
     *            the url which it's address is about to be resolved
     * @return if succeeded, string representing the ip address the url references, null otherwise
     */
    public static String getIpAddress(String url) {
        try {
            final URI uri = new URI(url);
            return InetAddress.getByName(uri.getHost()).getHostAddress();
        } catch (URISyntaxException | UnknownHostException ex) {
            final String msg = "Failed to resolve ip from URL '{}'";
            log.warn(msg, " Details: '{}' ", url, ex.getCause());
            log.debug(msg, url, ex);
            return null;
        }
    }

    /**
     * returns whether the network has a role in the cluster
     *
     * @return whether the network has a role (display, migration or gluster) in the cluster
     */
    public static boolean isRoleNetwork(NetworkCluster networkCluster) {
        return networkCluster.isDisplay()
                || networkCluster.isMigration()
                || networkCluster.isGluster()
                || networkCluster.isDefaultRoute();
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

    public static void setNetworkVdsmName(Network network) {
        String networkName = network.getName();
        if (VALID_VDS_NAME_PATTERN.matcher(networkName).matches()) {
            network.setVdsmName(networkName);
        } else {
            network.setVdsmName("on" + network.getId().toString()
                    .replaceAll("[^a-zA-Z0-9]+", "")
                    .substring(0, BusinessEntitiesDefinitions.HOST_NIC_NAME_LENGTH - 2));
        }
    }

    public static <E extends VdsNetworkInterface> Map<String, E> hostInterfacesByNetworkName(Collection<E> hostNics) {
        return hostNics == null
                ? Collections.emptyMap()
                : hostNics.stream()
                        .filter(hostNic -> hostNic.getNetworkName() != null)
                        .collect(Collectors.toMap(VdsNetworkInterface::getNetworkName, Function.identity()));
    }

    public static String stripIpv6ZoneIndex(String ip) {
        return ip == null ? null : ip.lastIndexOf('%') < 0 ? ip : ip.substring(0, ip.lastIndexOf('%'));
    }
}

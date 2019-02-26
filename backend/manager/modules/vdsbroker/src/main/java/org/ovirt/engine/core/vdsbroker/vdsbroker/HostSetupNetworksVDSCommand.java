package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol.AUTOCONF;
import static org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol.DHCP;
import static org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol.POLY_DHCP_AUTOCONF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.action.CreateOrUpdateBond;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NameServer;
import org.ovirt.engine.core.common.network.SwitchType;
import org.ovirt.engine.core.common.validation.IPv4MaskValidator;
import org.ovirt.engine.core.common.vdscommands.HostNetwork;
import org.ovirt.engine.core.common.vdscommands.HostSetupNetworksVdsCommandParameters;
import org.ovirt.engine.core.utils.NetworkUtils;

public class HostSetupNetworksVDSCommand<T extends HostSetupNetworksVdsCommandParameters> extends FutureVDSCommand<T> {

    protected static final String DHCP_BOOT_PROTOCOL = "dhcp";
    protected static final String DHCPV6_BOOT_PROTOCOL = "dhcpv6";
    protected static final String DHCPV6_AUTOCONF = "ipv6autoconf";
    protected static final String BOOT_PROTOCOL = "bootproto";
    protected static final String BONDING_OPTIONS = "options";
    protected static final String SLAVES = "nics";
    private static final String DEFAULT_ROUTE = "defaultRoute";
    private static final String UNSPECIFIED_IPV6_ADDRESS = "::";
    private static final Set<Ipv6BootProtocol> IPV6_AUTOCONF_PROTOCOL_SET = EnumSet.of(AUTOCONF, POLY_DHCP_AUTOCONF);
    private static final Set<Ipv6BootProtocol> IPV6_DHCP_PROTOCOL_SET = EnumSet.of(DHCP, POLY_DHCP_AUTOCONF);

    private static final Map<String, String> REMOVE_OBJ = Collections.singletonMap("remove", Boolean.TRUE.toString());

    public HostSetupNetworksVDSCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        httpTask = getBroker().setupNetworks(generateNetworks(), generateBonds(), generateOptions(), getParameters().isManagementNetworkChanged());
    }

    private Map<String, Object> generateNetworks() {
        Map<String, Object> networks = new HashMap<>();

        for (HostNetwork hostNetwork : getParameters().getNetworks()) {
            networks.put(hostNetwork.getVdsmName(), createNetworkAttributes(hostNetwork));
        }

        for (String net : getParameters().getRemovedNetworks()) {
            networks.put(net, REMOVE_OBJ);
        }

        return networks;
    }

    private Map<String, Object> createNetworkAttributes(HostNetwork hostNetwork) {
        Map<String, Object> attributes = new HashMap<>();
        if (hostNetwork.isBonding()) {
            attributes.put("bonding", hostNetwork.getNicName());
        } else {
            attributes.put("nic", hostNetwork.getNicName());
        }

        if (hostNetwork.isVlan()) {
            attributes.put("vlan", hostNetwork.getVlan().toString());
        }

        attributes.put(VdsProperties.MTU, NetworkUtils.getHostMtuActualValue(hostNetwork.getNetwork()));

        attributes.put("bridged", Boolean.toString(hostNetwork.isVmNetwork()));
        if (hostNetwork.isVmNetwork()) {
            attributes.put(VdsProperties.STP, hostNetwork.isStp() ? "yes" : "no");
        }

        if (hostNetwork.getIpv4BootProtocol() != null) {
            addIpv4BootProtocol(attributes, hostNetwork);
        }
        if (hostNetwork.getIpv6BootProtocol() != null) {
            addIpv6BootProtocol(attributes, hostNetwork);
        }

        if (hostNetwork.isDefaultRoute()) {
            attributes.put(DEFAULT_ROUTE, Boolean.TRUE);
        }

        addSwitchTypeIfSpecified(attributes);

        if (hostNetwork.hasProperties()) {
            attributes.put(VdsProperties.NETWORK_CUSTOM_PROPERTIES, hostNetwork.getProperties());
        }

        if (hostNetwork.isQosConfiguredOnInterface()) {
            HostNetworkQosMapper qosMapper = new HostNetworkQosMapper(attributes);
            qosMapper.serialize(hostNetwork.getQos());
        }

        List<NameServer> nameServers = hostNetwork.getNameServers();
        if (nameServers != null) {
            List<String> nameServerIps = nameServers.stream().map(NameServer::getAddress).collect(Collectors.toList());
            attributes.put(VdsProperties.name_servers, nameServerIps);
        }

        return attributes;
    }

    void addIpv4BootProtocol(Map<String, Object> opts, HostNetwork attachment) {
        if (attachment.getIpv4BootProtocol() == Ipv4BootProtocol.DHCP) {
            opts.put(BOOT_PROTOCOL, DHCP_BOOT_PROTOCOL);
        } else if (attachment.getIpv4BootProtocol() == Ipv4BootProtocol.STATIC_IP) {
            putIfNotEmpty(opts, "ipaddr", attachment.getIpv4Address());
            putIpv4PrefixOrNetmaskIfNotEmpty(opts, attachment.getIpv4Netmask());
            putIfNotEmpty(opts, "gateway", attachment.getIpv4Gateway());
        }
    }

    private void addIpv6BootProtocol(Map<String, Object> opts, HostNetwork attachment) {
        final Ipv6BootProtocol ipv6BootProtocol = attachment.getIpv6BootProtocol();
        opts.put(DHCPV6_BOOT_PROTOCOL, IPV6_DHCP_PROTOCOL_SET.contains(ipv6BootProtocol));
        opts.put(DHCPV6_AUTOCONF, IPV6_AUTOCONF_PROTOCOL_SET.contains(ipv6BootProtocol));
        if (Ipv6BootProtocol.STATIC_IP == ipv6BootProtocol) {
            putIfNotEmpty(opts, "ipv6addr", getIpv6Address(attachment));
            opts.put("ipv6gateway", selectIpv6Gateway(attachment));
        }
    }

    /**
     * An Ipv6 gateway should be passed to vdsm only if the attachment is also the default route role network of the cluster.
     * Otherwise, the gateway should be reset explicitly to 'unspecified' to guarantee that there is only one gateway on each
     * host. This in turn ensures that there is a single ipv6 default route entry in the ipv6 routing tables of the host.
     */
    private String selectIpv6Gateway(HostNetwork attachment) {
        boolean isIpv6DefaultRouteAttachment = attachment.isDefaultRoute() && attachment.getIpv6Gateway() != null;
        return isIpv6DefaultRouteAttachment ? attachment.getIpv6Gateway() : UNSPECIFIED_IPV6_ADDRESS;
    }

    private String getIpv6Address(HostNetwork attachment) {
        final String ipv6Address = attachment.getIpv6Address();
        final Integer ipv6Prefix = attachment.getIpv6Prefix();
        return ipv6Prefix == null ? ipv6Address : String.format("%s/%d", ipv6Address, ipv6Prefix);
    }

    private void putIpv4PrefixOrNetmaskIfNotEmpty(Map<String, Object> opts, String netmask) {
        if (IPv4MaskValidator.getInstance().isPrefixValid(netmask)) {
            putIfNotEmpty(opts, "prefix", netmask.replace("/", ""));
        } else {
            putIfNotEmpty(opts, "netmask", netmask);
        }
    }

    private Map<String, Object> generateBonds() {
        Map<String, Object> bonds = new HashMap<>();

        for (CreateOrUpdateBond bond : getParameters().getCreateOrUpdateBonds()) {
            bonds.put(bond.getName(), createBondAttributes(bond));
        }

        for (String bond : getParameters().getRemovedBonds()) {
            bonds.put(bond, REMOVE_OBJ);
        }

        return bonds;
    }

    private Map<String, Object> createBondAttributes(CreateOrUpdateBond bond) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(SLAVES, new ArrayList<>(bond.getSlaves()));
        addSwitchTypeIfSpecified(attributes);
        putIfNotEmpty(attributes, BONDING_OPTIONS, bond.getBondOptions());
        return attributes;
    }

    private Map<String, Object> generateOptions() {
        Map<String, Object> options = new HashMap<>();
        options.put(VdsProperties.CONNECTIVITY_CHECK, Boolean.toString(getParameters().isRollbackOnFailure()));
        options.put(VdsProperties.COMMIT_ON_SUCCESS, getParameters().isCommitOnSuccess());

        // VDSM uses the connectivity timeout only if 'connectivityCheck' is set to true
        if (getParameters().isRollbackOnFailure()) {
            options.put(VdsProperties.CONNECTIVITY_TIMEOUT, getParameters().getConnectivityTimeout());
        }

        return options;
    }

    private static void putIfNotEmpty(Map<String, Object> map, String key, String value) {
        if (StringUtils.isNotEmpty(value)) {
            map.put(key, value);
        }
    }

    /**
     * switch type is optional, specifies switch type.  Legacy will be used if switch option is not set/passed.
     */
    private void addSwitchTypeIfSpecified(Map<String, Object> resultMap) {
        SwitchType switchType = getParameters().getClusterSwitchType();
        if (switchType != null) {
            resultMap.put(VdsProperties.SWITCH_KEY, switchType.getOptionValue());
        }
    }
}

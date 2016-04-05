package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.action.CreateOrUpdateBond;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.validation.MaskValidator;
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
            Map<String, Object> attributes = new HashMap<>();
            if (hostNetwork.isBonding()) {
                attributes.put("bonding", hostNetwork.getNicName());
            } else {
                attributes.put("nic", hostNetwork.getNicName());
            }

            if (hostNetwork.isVlan()) {
                attributes.put("vlan", hostNetwork.getVlan().toString());
            }

            if (hostNetwork.getMtu() == 0) {
                attributes.put(VdsProperties.MTU, NetworkUtils.getDefaultMtu());
            } else {
                attributes.put(VdsProperties.MTU, hostNetwork.getMtu());
            }

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

            if (hostNetwork.getSwitchType() != null) {
                /**
                 * optional, specifies switch type.  Legacy will be used if switch option is not passed.
                 */
                attributes.put(VdsProperties.SWITCH_KEY, hostNetwork.getSwitchType().getOptionValue());
            }

            if (hostNetwork.hasProperties()) {
                attributes.put(VdsProperties.NETWORK_CUSTOM_PROPERTIES, hostNetwork.getProperties());
            }

            if (hostNetwork.isQosConfiguredOnInterface()) {
                HostNetworkQosMapper qosMapper = new HostNetworkQosMapper(attributes);
                qosMapper.serialize(hostNetwork.getQos());
            }

            networks.put(hostNetwork.getNetworkName(), attributes);
        }

        for (String net : getParameters().getRemovedNetworks()) {
            networks.put(net, REMOVE_OBJ);
        }

        return networks;
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
        opts.put(DHCPV6_BOOT_PROTOCOL, Ipv6BootProtocol.DHCP == ipv6BootProtocol);
        opts.put(DHCPV6_AUTOCONF, Ipv6BootProtocol.AUTOCONF == ipv6BootProtocol);
        if (Ipv6BootProtocol.STATIC_IP == ipv6BootProtocol) {
            putIfNotEmpty(opts, "ipv6addr", getIpv6Address(attachment));
            putIfNotEmpty(opts, "ipv6gateway", attachment.getIpv6Gateway());
        }
    }

    private String getIpv6Address(HostNetwork attachment) {
        final String ipv6Address = attachment.getIpv6Address();
        final Integer ipv6Prefix = attachment.getIpv6Prefix();
        return ipv6Prefix == null ? ipv6Address : String.format("%s/%d", ipv6Address, ipv6Prefix);
    }

    private void putIpv4PrefixOrNetmaskIfNotEmpty(Map<String, Object> opts, String netmask) {
        if (MaskValidator.getInstance().isPrefixValid(netmask)) {
            putIfNotEmpty(opts, "prefix", netmask.replace("/", ""));
        } else {
            putIfNotEmpty(opts, "netmask", netmask);
        }
    }

    private Map<String, Object> generateBonds() {
        Map<String, Object> bonds = new HashMap<>();

        for (CreateOrUpdateBond bond : getParameters().getCreateOrUpdateBonds()) {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(SLAVES, new ArrayList<>(bond.getSlaves()));
            putIfNotEmpty(attributes, BONDING_OPTIONS, bond.getBondOptions());
            bonds.put(bond.getName(), attributes);
        }

        for (String bond : getParameters().getRemovedBonds()) {
            bonds.put(bond, REMOVE_OBJ);
        }

        return bonds;
    }

    private Map<String, Object> generateOptions() {
        Map<String, Object> options = new HashMap<>();
        options.put(VdsProperties.CONNECTIVITY_CHECK, Boolean.toString(getParameters().isRollbackOnFailure()));

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
}

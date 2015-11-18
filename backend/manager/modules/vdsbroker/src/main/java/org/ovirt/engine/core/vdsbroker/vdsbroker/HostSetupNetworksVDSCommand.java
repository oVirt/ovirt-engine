package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.validation.MaskValidator;
import org.ovirt.engine.core.common.vdscommands.HostNetwork;
import org.ovirt.engine.core.common.vdscommands.HostSetupNetworksVdsCommandParameters;
import org.ovirt.engine.core.utils.NetworkUtils;

public class HostSetupNetworksVDSCommand<T extends HostSetupNetworksVdsCommandParameters> extends FutureVDSCommand<T> {

    protected static final String DHCP_BOOT_PROTOCOL = "dhcp";
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
                attributes.put(VdsProperties.MTU, NetworkUtils.getDefaultMtu().toString());
            } else {
                attributes.put(VdsProperties.MTU, String.valueOf(hostNetwork.getMtu()));
            }

            attributes.put("bridged", Boolean.toString(hostNetwork.isVmNetwork()));
            if (hostNetwork.isVmNetwork()) {
                attributes.put(VdsProperties.STP, hostNetwork.isStp() ? "yes" : "no");
            }

            if (hostNetwork.getBootProtocol() != null) {
                addBootProtocol(attributes, hostNetwork);
            }

            if (hostNetwork.isDefaultRoute()) {
                attributes.put(DEFAULT_ROUTE, Boolean.TRUE);
            }

            if (hostNetwork.hasProperties()) {
                attributes.put(VdsProperties.NETWORK_CUSTOM_PROPERTIES, hostNetwork.getProperties());
            }

            if (getParameters().getHostNetworkQosSupported() && hostNetwork.isQosConfiguredOnInterface()) {
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

    private void addBootProtocol(Map<String, Object> opts, HostNetwork attachment) {
        switch (attachment.getBootProtocol()) {
        case DHCP:
            opts.put(BOOT_PROTOCOL, DHCP_BOOT_PROTOCOL);
            break;
        case STATIC_IP:
            putIfNotEmpty(opts, "ipaddr", attachment.getAddress());
            putPrefixOrNetmaskIfNotEmpty(opts, attachment.getNetmask());
            putIfNotEmpty(opts, "gateway", attachment.getGateway());
            break;
        default:
            break;
        }
    }

    private void putPrefixOrNetmaskIfNotEmpty(Map<String, Object> opts, String netmask) {
        if (MaskValidator.getInstance().isPrefixValid(netmask)) {
            putIfNotEmpty(opts, "prefix", netmask.replace("/", ""));
        } else {
            putIfNotEmpty(opts, "netmask", netmask);
        }
    }

    private Map<String, Object> generateBonds() {
        Map<String, Object> bonds = new HashMap<>();

        for (Bond bond : getParameters().getBonds()) {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(SLAVES, bond.getSlaves());
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
            options.put(VdsProperties.CONNECTIVITY_TIMEOUT, getParameters().getConectivityTimeout());
        }

        return options;
    }

    private static void putIfNotEmpty(Map<String, Object> map, String key, String value) {
        if (StringUtils.isNotEmpty(value)) {
            map.put(key, value);
        }
    }
}

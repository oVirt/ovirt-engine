package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.vdscommands.SetupNetworksVdsCommandParameters;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.network.NetworkQoSDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetupNetworksVDSCommand<T extends SetupNetworksVdsCommandParameters> extends FutureVDSCommand<T> {

    protected static final String DHCP_BOOT_PROTOCOL = "dhcp";
    protected static final String BOOT_PROTOCOL = "bootproto";
    protected static final String BONDING_OPTIONS = "options";
    protected static final String SLAVES = "nics";
    private static final String DEFAULT_ROUTE = "defaultRoute";
    private static final Map<String, String> REMOVE_OBJ = Collections.singletonMap("remove", Boolean.TRUE.toString());
    private static final Logger log = LoggerFactory.getLogger(SetupNetworksVDSCommand.class);

    public SetupNetworksVDSCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        httpTask = getBroker().setupNetworks(generateNetworks(), generateBonds(), generateOptions());
    }

    private Map<String, Object> generateNetworks() {
        Map<String, Object> networks = new HashMap<String, Object>();
        NetworkQoSDao qosDao = getDbFacade().getNetworkQosDao();
        for (Network network : getParameters().getNetworks()) {
            Map<String, Object> opts = new HashMap<String, Object>();
            VdsNetworkInterface iface =
                    findNetworkInterface(network.getName(), getParameters().getInterfaces(), getParameters().getBonds());
            String ifaceNameWithoutVlan = NetworkUtils.stripVlan(iface);
            Boolean bonded = findInterfaceByName(ifaceNameWithoutVlan).getBonded();
            String type = (bonded != null && bonded) ? "bonding" : "nic";
            opts.put(type, ifaceNameWithoutVlan);
            if (NetworkUtils.isVlan(network)) {
                opts.put("vlan", network.getVlanId().toString());
            }

            if (iface.getBootProtocol() != null) {
                addBootProtocol(opts, iface);
            }

            if (network.getMtu() == 0) {
                opts.put("mtu", NetworkUtils.getDefaultMtu().toString());
            } else {
                opts.put("mtu", String.valueOf(network.getMtu()));
            }

            opts.put("bridged", Boolean.toString(network.isVmNetwork()));
            if (network.isVmNetwork()) {
                opts.put(VdsProperties.STP, network.getStp() ? "yes" : "no");
            }

            VDS host = getDbFacade().getVdsDao().get(getParameters().getVdsId());
            Version version = host.getVdsGroupCompatibilityVersion();
            if (qosConfiguredOnInterface(iface, network)
                    && FeatureSupported.hostNetworkQos(version)) {
                NetworkQosMapper qosMapper =
                        new NetworkQosMapper(opts, VdsProperties.HOST_QOS_INBOUND, VdsProperties.HOST_QOS_OUTBOUND);
                qosMapper.serialize(iface.isQosOverridden() ? iface.getQos() : qosDao.get(network.getQosId()));
            }

            Set<Version> supportedClusterVersionsSet = host.getSupportedClusterVersionsSet();
            if (supportedClusterVersionsSet == null || supportedClusterVersionsSet.isEmpty()) {
                log.warn("Host '{}' ('{}') doesn't contain Supported Cluster Versions, therefore 'defaultRoute'"
                        + " will not be sent via the SetupNetworks", host.getName(), host.getId());
            } else if (FeatureSupported.defaultRoute(Collections.max(supportedClusterVersionsSet))
                    && NetworkUtils.isManagementNetwork(network)
                    && (iface.getBootProtocol() == NetworkBootProtocol.DHCP
                    || (iface.getBootProtocol() == NetworkBootProtocol.STATIC_IP
                    && StringUtils.isNotEmpty(iface.getGateway())))) {
                opts.put(DEFAULT_ROUTE, Boolean.TRUE);
            }

            if (iface.hasCustomProperties()) {
                opts.put(VdsProperties.NETWORK_CUSTOM_PROPERTIES, iface.getCustomProperties());
            }

            networks.put(network.getName(), opts);
        }

        for (String net : getParameters().getRemovedNetworks()) {
            networks.put(net, REMOVE_OBJ);
        }

        return networks;
    }

    private boolean qosConfiguredOnInterface(VdsNetworkInterface iface, Network network) {
        if (iface.isQosOverridden()) {
            return iface.getQos() != null;
        } else {
            return network.getQosId() != null;
        }
    }

    private void addBootProtocol(Map<String, Object> opts, VdsNetworkInterface iface) {
        switch (iface.getBootProtocol()) {
        case DHCP:
            opts.put(BOOT_PROTOCOL, DHCP_BOOT_PROTOCOL);
            break;
        case STATIC_IP:
            putIfNotEmpty(opts, "ipaddr", iface.getAddress());
            putIfNotEmpty(opts, "netmask", iface.getSubnet());
            putIfNotEmpty(opts, "gateway", iface.getGateway());
            break;
        default:
            break;
        }
    }

    private Map<String, Object> generateBonds() {
        Map<String, Object> bonds = new HashMap<String, Object>();

        for (VdsNetworkInterface bond : getParameters().getBonds()) {
            Map<String, Object> opts = new HashMap<String, Object>();
            opts.put(SLAVES, getBondNics(bond, getParameters().getInterfaces()));

            if (!StringUtils.isEmpty(bond.getBondOptions())) {
                opts.put(BONDING_OPTIONS, bond.getBondOptions());
            }
            bonds.put(bond.getName(), opts);
        }

        for (String bond : getParameters().getRemovedBonds()) {
            bonds.put(bond, REMOVE_OBJ);
        }

        return bonds;
    }

    private Map<String, Object> generateOptions() {
        Map<String, Object> options = new HashMap<String, Object>();

        options.put(VdsProperties.CONNECTIVITY_CHECK, Boolean.toString(getParameters().isCheckConnectivity()));

        // VDSM uses the connectivity timeout only if 'connectivityCheck' is set to true
        if (getParameters().isCheckConnectivity()) {
            options.put(VdsProperties.CONNECTIVITY_TIMEOUT, getParameters().getConectivityTimeout());
        }
        return options;
    }

    private static void putIfNotEmpty(Map<String, Object> map, String key, String value) {
        if (!StringUtils.isEmpty(value)) {
            map.put(key, value);
        }
    }

    private static List<String> getBondNics(VdsNetworkInterface bond, List<VdsNetworkInterface> interfaces) {
        List<String> nics = new ArrayList<String>();

        for (VdsNetworkInterface i : interfaces) {
            if (bond.getName().equals(i.getBondName())) {
                nics.add(i.getName());
            }
        }
        return nics;
    }

    private static VdsNetworkInterface findNetworkInterface(String network,
            List<VdsNetworkInterface> interfaces,
            List<VdsNetworkInterface> bonds) {
        for (VdsNetworkInterface i : interfaces) {
            if (network.equals(i.getNetworkName())) {
                return i;
            }
        }

        for (VdsNetworkInterface i : bonds) {
            if (network.equals(i.getNetworkName())) {
                return i;
            }
        }

        return null;
    }

    private VdsNetworkInterface findInterfaceByName(String name) {
        for (VdsNetworkInterface iface : getParameters().getInterfaces()) {
            if (name.equals(iface.getName())) {
                return iface;
            }
        }

        return null;
    }
}

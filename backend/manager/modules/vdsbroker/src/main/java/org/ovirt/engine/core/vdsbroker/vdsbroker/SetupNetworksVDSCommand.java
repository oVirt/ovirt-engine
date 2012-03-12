package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.vdscommands.SetupNetworksVdsCommandParameters;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class SetupNetworksVDSCommand<T extends SetupNetworksVdsCommandParameters> extends FutureVDSCommand<T> {

    public SetupNetworksVDSCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        XmlRpcStruct bonds = new XmlRpcStruct();
        XmlRpcStruct networks = new XmlRpcStruct();
        Map<String, String> removeObj = new HashMap<String, String>();
        removeObj.put("remove", "true");

        // Networks
        for (network net : getParameters().getNetworks()) {
            Map<String, String> opts = new HashMap<String, String>();
            VdsNetworkInterface i = findNetworkInterface(net.getname(), getParameters().getInterfaces(),
                    getParameters().getBonds());
            String type = (i.getBonded() != null && i.getBonded()) ? "bonding" : "nic";

            opts.put(type, NetworkUtils.StripVlan(i.getName()));
            if (net.getvlan_id() != null) {
                opts.put("vlan", net.getvlan_id().toString());
            }
            // TODO: add bootproto to network object
            switch (i.getBootProtocol()) {
            case Dhcp:
                opts.put(VdsProperties.bootproto, VdsProperties.dhcp);
                break;
            case StaticIp:
                if (!StringHelper.isNullOrEmpty(i.getAddress())) {
                    opts.put("ipaddr", i.getAddress());
                }
                if (!StringHelper.isNullOrEmpty(i.getSubnet())) {
                    opts.put("netmask", i.getSubnet());
                }
                if (!StringHelper.isNullOrEmpty(i.getGateway())) {
                    opts.put("gateway", i.getGateway());
                }
                break;
            }

            if (net.getMtu() != 0) {
                opts.put("mtu", String.valueOf(net.getMtu()));
            }

            networks.add(net.getname(), opts);
        }

        // Removed Networks
        for (network net : getParameters().getRemovedNetworks()) {
            networks.add(net.getname(), removeObj);
        }

        // Bonds
        for (VdsNetworkInterface bond : getParameters().getBonds()) {
            XmlRpcStruct opts = new XmlRpcStruct();
            opts.add("nics", getBondNics(bond, getParameters().getInterfaces()));
            // opts.add("nics", "eth1,eth2");

            if (!StringHelper.isNullOrEmpty(bond.getBondOptions())) {
                opts.add("BONDING_OPTS", bond.getBondOptions());
            }
            bonds.add(bond.getName(), opts);
        }

        // Removed Bonds
        for (VdsNetworkInterface bond : getParameters().getRemovedBonds()) {
            bonds.add(bond.getName(), removeObj);
        }

        // Options
        XmlRpcStruct options = new XmlRpcStruct();

        if (getParameters().isCheckConnectivity()) {
            options.add(VdsProperties.connectivityCheck, "true");
        }
        else {
            options.add(VdsProperties.connectivityCheck, "false");
        }

        if (getParameters().getConectivityTimeout() >= 0) {
            options.add(VdsProperties.connectivityTimeout,
                    (new Integer(getParameters().getConectivityTimeout())).toString());
        }

        httpTask = getBroker().setupNetworks(networks, bonds, options);

    }

    private List<String> getBondNics(VdsNetworkInterface bond, List<VdsNetworkInterface> interfaces) {
        List<String> nics = new ArrayList<String>();

        for (VdsNetworkInterface i : interfaces) {
            if (bond.getName().equals(i.getBondName())) {
                nics.add(i.getName());
            }
        }
        return nics;
    }

    private VdsNetworkInterface findNetworkInterface(String network,
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

}

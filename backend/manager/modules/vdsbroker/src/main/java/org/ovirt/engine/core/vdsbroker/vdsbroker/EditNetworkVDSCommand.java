package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.vdscommands.NetworkVdsmVDSCommandParameters;

public class EditNetworkVDSCommand<P extends NetworkVdsmVDSCommandParameters> extends VdsBrokerCommand<P> {
    public EditNetworkVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        String networkName = (getParameters().getNetworkName() == null) ? "" : getParameters()
                .getNetworkName();
        String oldNetwork = (getParameters().getOldNetworkName() == null) ? "" : getParameters()
                .getOldNetworkName();
        String vlanId = (getParameters().getVlanId() != null) ? getParameters().getVlanId().toString()
                : "";
        String bond = (getParameters().getBondName() == null) ? "" : getParameters().getBondName();
        String[] nics = (getParameters().getNics() == null) ? new String[] {} : getParameters().getNics();
        Map<String, String> options = new HashMap<String, String>();

        switch (getParameters().getBootProtocol()) {
        case Dhcp:
            options.put(VdsProperties.bootproto, VdsProperties.dhcp);
            break;
        case StaticIp:
            if (!StringUtils.isEmpty(getParameters().getInetAddr())) {
                options.put(VdsProperties.ipaddr, getParameters().getInetAddr());
            }
            if (!StringUtils.isEmpty(getParameters().getNetworkMask())) {
                options.put(VdsProperties.netmask, getParameters().getNetworkMask());
            }
            if (!StringUtils.isEmpty(getParameters().getGateway())) {
                options.put(VdsProperties.gateway, getParameters().getGateway());
            }
            break;
        }

        if (!StringUtils.isEmpty(getParameters().getBondingOptions())) {
            options.put(VdsProperties.bonding_opts, getParameters().getBondingOptions());
        }

        options.put(VdsProperties.stp, (getParameters().getStp()) ? "yes" : "no");
        options.put("bridged", Boolean.toString(getParameters().isVmNetwork()));

        Network network = getParameters().getNetwork();
        if (network != null && network.getMtu() != 0) {
            options.put("mtu", String.valueOf(network.getMtu()));
        }

        // options[VdsProperties.force] = "true";
        if (getParameters().getCheckConnectivity()) {
            options.put(VdsProperties.connectivityCheck, "true");
            options.put(VdsProperties.connectivityTimeout,
                    (new Integer(getParameters().getConnectionTimeout())).toString());
        }

        status = getBroker().editNetwork(oldNetwork, networkName, vlanId, bond, nics, options);
        ProceedProxyReturnValue();
    }
}

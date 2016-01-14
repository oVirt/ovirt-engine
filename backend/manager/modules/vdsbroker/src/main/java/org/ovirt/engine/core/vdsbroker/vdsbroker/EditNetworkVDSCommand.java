package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.vdscommands.NetworkVdsmVDSCommandParameters;
import org.ovirt.engine.core.utils.NetworkUtils;

public class EditNetworkVDSCommand<P extends NetworkVdsmVDSCommandParameters> extends VdsBrokerCommand<P> {
    public EditNetworkVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        String networkName = (getParameters().getNetworkName() == null) ? "" : getParameters()
                .getNetworkName();
        String oldNetwork = (getParameters().getOldNetworkName() == null) ? "" : getParameters()
                .getOldNetworkName();
        String vlanId = (getParameters().getVlanId() != null) ? getParameters().getVlanId().toString()
                : "";
        String bond = (getParameters().getBondName() == null) ? "" : getParameters().getBondName();
        String[] nics = (getParameters().getNics() == null) ? new String[] {} : getParameters().getNics();
        Map<String, String> options = new HashMap<>();

        switch (getParameters().getBootProtocol()) {
        case DHCP:
            options.put(VdsProperties.BOOT_PROTOCOL, VdsProperties.DHCP);
            break;
        case STATIC_IP:
            if (!StringUtils.isEmpty(getParameters().getInetAddr())) {
                options.put(VdsProperties.IP_ADDRESS, getParameters().getInetAddr());
            }
            if (!StringUtils.isEmpty(getParameters().getNetworkMask())) {
                options.put(VdsProperties.NETMASK, getParameters().getNetworkMask());
            }
            if (!StringUtils.isEmpty(getParameters().getGateway())) {
                options.put(VdsProperties.GATEWAY, getParameters().getGateway());
            }
            break;
        default:
            break;
        }

        if (!StringUtils.isEmpty(getParameters().getBondingOptions())) {
            options.put(VdsProperties.BONDING_OPTIONS, getParameters().getBondingOptions());
        }

        options.put(VdsProperties.STP, getParameters().getStp() ? "yes" : "no");
        options.put("bridged", Boolean.toString(getParameters().isVmNetwork()));

        Network network = getParameters().getNetwork();
        if (network != null) {
            if (network.getMtu() == 0) {
                options.put("mtu", NetworkUtils.getDefaultMtu().toString());
            } else {
                options.put("mtu", String.valueOf(network.getMtu()));
            }
        }

        // options[VdsProperties.force] = "true";
        if (getParameters().getCheckConnectivity()) {
            options.put(VdsProperties.CONNECTIVITY_CHECK, "true");
            options.put(VdsProperties.CONNECTIVITY_TIMEOUT,
                    String.valueOf(getParameters().getConnectionTimeout()));
        }

        status = getBroker().editNetwork(oldNetwork, networkName, vlanId, bond, nics, options);
        proceedProxyReturnValue();
    }
}

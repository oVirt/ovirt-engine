package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public abstract class BootProtocolResolver {

    protected final Map<String, Object> entry;
    private final VdsNetworkInterface iface;
    private final VDS host;

    protected BootProtocolResolver(Map<String, Object> entry, VdsNetworkInterface iface, VDS host) {
        this.entry = entry;
        this.iface = iface;
        this.host = host;
    }

    protected abstract String fetchIpAddress();

    protected abstract String fetchGateway();

    protected abstract boolean bootProtocolDhcp();

    public void resolve() {
        if (entry == null) {
            return;
        }

        NetworkBootProtocol bootproto = NetworkBootProtocol.NONE;
        if (bootProtocolDhcp()) {
            bootproto = NetworkBootProtocol.DHCP;
        } else if (StringUtils.isNotEmpty(fetchIpAddress())) {
            bootproto = NetworkBootProtocol.STATIC_IP;
        }

        if (bootproto == NetworkBootProtocol.STATIC_IP) {
            String gateway = fetchGateway();
            if (StringUtils.isNotEmpty(gateway)) {
                VdsBrokerObjectsBuilder.setGatewayIfNecessary(iface, host, gateway);
            }
        }

        iface.setBootProtocol(bootproto);
    }

}

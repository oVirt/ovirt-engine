package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public class CfgBootProtocolResolver extends BootProtocolResolver {

    public CfgBootProtocolResolver(Map<String, Object> entry, VdsNetworkInterface iface, VDS host) {
        super((Map<String, Object>) entry.get("cfg"), iface, host);
    }

    @Override
    protected String fetchIpAddress() {
        return (String) entry.get(VdsProperties.IP_ADDRESS);
    }

    @Override
    protected String fetchGateway() {
        return (String) entry.get(VdsProperties.GATEWAY);
    }

    @Override
    protected boolean bootProtocolDhcp() {
        return "dhcp".equalsIgnoreCase((String) entry.get("BOOTPROTO"));
    }

}

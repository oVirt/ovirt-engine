package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public class NoCfgBootProtocolResolver extends BootProtocolResolver {

    private String ipAddress;

    public NoCfgBootProtocolResolver(Map<String, Object> entry, VdsNetworkInterface iface, VDS host) {
        super(entry, iface, host);
        ipAddress = iface.getAddress();
    }

    @Override
    protected String fetchIpAddress() {
        return ipAddress;
    }

    @Override
    protected String fetchGateway() {
        return (String) entry.get(VdsProperties.GLOBAL_GATEWAY);
    }

    @Override
    protected boolean bootProtocolDhcp() {
        return Boolean.TRUE.equals(entry.get("dhcpv4"));
    }

}

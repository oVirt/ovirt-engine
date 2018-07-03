package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

class NoCfgIpv6InfoFetcher implements Ipv6InfoFetcher {

    private final String ipAddress;
    private final Map<String, Object> nicProperties;

    public NoCfgIpv6InfoFetcher(Map<String, Object> nicProperties,
            String ipAddress) {
        this.nicProperties = nicProperties;
        this.ipAddress = ipAddress;
    }

    @Override
    public String fetchIpAddress() {
        return ipAddress;
    }

    @Override
    public String fetchGateway() {
        return (String) nicProperties.get(VdsProperties.IPV6_GLOBAL_GATEWAY);
    }

    @Override
    public boolean isBootProtocolDhcp() {
        return Boolean.TRUE.equals(nicProperties.get("dhcpv6"));
    }

    @Override
    public boolean isBootProtocolAutoconf() {
        // TODO: YZ - to be revised once is implemented on VDSM side.
        return Boolean.TRUE.equals(nicProperties.get("ipv6autoconf"));
    }

    @Override
    public boolean isPolyDhcpAutoconfBootProtocol() {
        return isBootProtocolDhcp() && isBootProtocolAutoconf();
    }
}

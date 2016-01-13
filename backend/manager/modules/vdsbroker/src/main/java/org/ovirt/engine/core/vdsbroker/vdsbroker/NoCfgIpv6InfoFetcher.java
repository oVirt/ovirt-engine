package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

class NoCfgIpv6InfoFetcher implements IpInfoFetcher {

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

}

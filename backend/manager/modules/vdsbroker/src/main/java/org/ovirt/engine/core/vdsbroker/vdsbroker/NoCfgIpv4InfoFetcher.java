package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

class NoCfgIpv4InfoFetcher implements IpInfoFetcher {

    private final String ipAddress;
    private final Map<String, Object> nicProperties;

    public NoCfgIpv4InfoFetcher(Map<String, Object> nicProperties,
            String ipAddress) {
        this.ipAddress = ipAddress;
        this.nicProperties = nicProperties;
    }

    @Override
    public String fetchIpAddress() {
        return ipAddress;
    }

    @Override
    public String fetchGateway() {
        return (String) nicProperties.get(VdsProperties.GLOBAL_GATEWAY);
    }

    @Override
    public boolean isBootProtocolDhcp() {
        return Boolean.TRUE.equals(nicProperties.get("dhcpv4"));
    }

}

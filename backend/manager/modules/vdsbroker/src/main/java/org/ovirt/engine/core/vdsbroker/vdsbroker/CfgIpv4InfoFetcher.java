package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

class CfgIpv4InfoFetcher implements IpInfoFetcher {

    private final Map<String, Object> nicProperties;

    CfgIpv4InfoFetcher(Map<String, Object> nicProperties) {
        this.nicProperties = nicProperties;
    }

    @Override
    public String fetchIpAddress() {
        return (String) nicProperties.get(VdsProperties.IP_ADDRESS);
    }

    @Override
    public String fetchGateway() {
        return (String) nicProperties.get(VdsProperties.GATEWAY);
    }

    @Override
    public boolean isBootProtocolDhcp() {
        return "dhcp".equalsIgnoreCase((String) nicProperties.get(VdsProperties.BOOT_PROTOCOL));
    }

}

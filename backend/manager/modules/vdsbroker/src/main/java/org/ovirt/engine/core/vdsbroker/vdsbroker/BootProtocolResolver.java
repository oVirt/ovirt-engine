package org.ovirt.engine.core.vdsbroker.vdsbroker;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;

@ApplicationScoped
class BootProtocolResolver {

    public NetworkBootProtocol resolve(IpInfoFetcher ipInfoFetcher) {
        if (ipInfoFetcher.isBootProtocolDhcp()) {
            return NetworkBootProtocol.DHCP;
        } else if (StringUtils.isNotEmpty(ipInfoFetcher.fetchIpAddress())) {
            return NetworkBootProtocol.STATIC_IP;
        } else {
            return NetworkBootProtocol.NONE;
        }
    }
}

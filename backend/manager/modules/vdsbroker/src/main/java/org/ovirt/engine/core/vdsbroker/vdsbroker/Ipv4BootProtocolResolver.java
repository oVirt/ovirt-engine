package org.ovirt.engine.core.vdsbroker.vdsbroker;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;

@ApplicationScoped
class Ipv4BootProtocolResolver implements BootProtocolResolver<Ipv4BootProtocol, IpInfoFetcher> {

    @Override
    public Ipv4BootProtocol resolve(IpInfoFetcher ipInfoFetcher) {
        if (ipInfoFetcher.isBootProtocolDhcp()) {
            return Ipv4BootProtocol.DHCP;
        } else if (StringUtils.isNotEmpty(ipInfoFetcher.fetchIpAddress())) {
            return Ipv4BootProtocol.STATIC_IP;
        } else {
            return Ipv4BootProtocol.NONE;
        }
    }
}

package org.ovirt.engine.core.vdsbroker.vdsbroker;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;

@ApplicationScoped
class Ipv6BootProtocolResolver implements BootProtocolResolver<Ipv6BootProtocol, Ipv6InfoFetcher> {

    @Override
    public Ipv6BootProtocol resolve(Ipv6InfoFetcher ipInfoFetcher) {
        if (ipInfoFetcher.isPolyDhcpAutoconfBootProtocol()) {
            return Ipv6BootProtocol.POLY_DHCP_AUTOCONF;
        } else if (ipInfoFetcher.isBootProtocolDhcp()) {
            return Ipv6BootProtocol.DHCP;
        } else if (ipInfoFetcher.isBootProtocolAutoconf()) {
            return Ipv6BootProtocol.AUTOCONF;
        } else if (StringUtils.isNotEmpty(ipInfoFetcher.fetchIpAddress())) {
            return Ipv6BootProtocol.STATIC_IP;
        } else {
            return Ipv6BootProtocol.NONE;
        }
    }
}

package org.ovirt.engine.core.vdsbroker.vdsbroker;

interface Ipv6InfoFetcher extends IpInfoFetcher {
    boolean isBootProtocolAutoconf();

    boolean isPolyDhcpAutoconfBootProtocol();
}

package org.ovirt.engine.core.vdsbroker.vdsbroker;

interface IpInfoFetcher {
    String fetchIpAddress();

    String fetchGateway();

    boolean isBootProtocolDhcp();
}

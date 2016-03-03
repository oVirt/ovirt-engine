package org.ovirt.engine.core.vdsbroker.vdsbroker;

public interface BootProtocolResolver<T, F extends IpInfoFetcher> {
    T resolve(F ipInfoFetcher);
}

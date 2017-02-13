package org.ovirt.engine.core.utils.network.vm;

import org.ovirt.engine.core.common.businessentities.VmInitNetwork;

public class VmInitNetworkIpv6InfoFetcher extends VmInitNetworkIpInfoFetcher {

    public VmInitNetworkIpv6InfoFetcher(VmInitNetwork vmInitNetwork) {
        super(vmInitNetwork);
    }

    @Override
    public String fetchIp() {
        return getVmInitNetwork().getIpv6Address();
    }

    @Override
    public String fetchNetmask() {
        final Integer ipv6Prefix = getVmInitNetwork().getIpv6Prefix();
        return ipv6Prefix == null ? null : ipv6Prefix.toString();
    }

    @Override
    public String fetchGateway() {
        return getVmInitNetwork().getIpv6Gateway();
    }

    @Override
    public String fetchBootProtocol() {
        return getVmInitNetwork().getIpv6BootProtocol().getDisplayName();
    }
}

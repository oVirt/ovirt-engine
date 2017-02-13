package org.ovirt.engine.core.utils.network.vm;

import org.ovirt.engine.core.common.businessentities.VmInitNetwork;

public class VmInitNetworkIpv4InfoFetcher extends VmInitNetworkIpInfoFetcher {

    public VmInitNetworkIpv4InfoFetcher(VmInitNetwork vmInitNetwork) {
        super(vmInitNetwork);
    }

    @Override
    public String fetchIp() {
        return getVmInitNetwork().getIp();
    }

    @Override
    public String fetchNetmask() {
        return getVmInitNetwork().getNetmask();
    }

    @Override
    public String fetchGateway() {
        return getVmInitNetwork().getGateway();
    }

    @Override
    public String fetchBootProtocol() {
        return getVmInitNetwork().getBootProtocol().getDisplayName();
    }
}

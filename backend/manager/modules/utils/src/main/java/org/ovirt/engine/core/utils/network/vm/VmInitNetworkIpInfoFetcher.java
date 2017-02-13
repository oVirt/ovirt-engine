package org.ovirt.engine.core.utils.network.vm;

import org.ovirt.engine.core.common.businessentities.VmInitNetwork;

public abstract class VmInitNetworkIpInfoFetcher {
    private final VmInitNetwork vmInitNetwork;

    protected VmInitNetworkIpInfoFetcher(VmInitNetwork vmInitNetwork) {
        this.vmInitNetwork = vmInitNetwork;
    }

    protected VmInitNetwork getVmInitNetwork() {
        return vmInitNetwork;
    }

    public String fetchName() {
        return getVmInitNetwork().getName();
    }

    public abstract String fetchIp();
    public abstract String fetchNetmask();
    public abstract String fetchGateway();
    public abstract String fetchBootProtocol();
}

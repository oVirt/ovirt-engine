package org.ovirt.engine.core.utils.network.function;

import java.util.function.Function;

import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public class NicToIpv4AddressFunction implements Function<VdsNetworkInterface, IPv4Address> {

    @Override
    public IPv4Address apply(VdsNetworkInterface nic) {
        IPv4Address ipv4Address = new IPv4Address();
        if (nic.getIpv4BootProtocol() == Ipv4BootProtocol.STATIC_IP) {
            ipv4Address.setAddress(nic.getIpv4Address());
            ipv4Address.setNetmask(nic.getIpv4Subnet());
            ipv4Address.setGateway(nic.getIpv4Gateway());
        }
        ipv4Address.setBootProtocol(nic.getIpv4BootProtocol());
        return ipv4Address;
    }
}

package org.ovirt.engine.core.utils.network.function;

import java.util.function.Function;

import org.ovirt.engine.core.common.businessentities.network.IpV6Address;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public class NicToIpv6AddressFunction implements Function<VdsNetworkInterface, IpV6Address> {

    @Override
    public IpV6Address apply(VdsNetworkInterface nic) {
        IpV6Address ipv6Address = new IpV6Address();
        if (nic.getIpv6BootProtocol() == Ipv6BootProtocol.STATIC_IP) {
            ipv6Address.setAddress(nic.getIpv6Address());
            ipv6Address.setPrefix(nic.getIpv6Prefix());
            ipv6Address.setGateway(nic.getIpv6Gateway());
        }
        ipv6Address.setBootProtocol(nic.getIpv6BootProtocol());
        return ipv6Address;
    }
}

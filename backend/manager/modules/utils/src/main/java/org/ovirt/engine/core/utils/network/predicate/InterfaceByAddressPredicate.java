package org.ovirt.engine.core.utils.network.predicate;

import java.util.function.Predicate;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public class InterfaceByAddressPredicate implements Predicate<VdsNetworkInterface> {

    private final String address;
    private final Predicate<String> ipAddressPredicate;

    public InterfaceByAddressPredicate(String address) {
        this.address = address;
        this.ipAddressPredicate = new IpAddressPredicate(address);
    }

    @Override
    public boolean test(VdsNetworkInterface iface) {
        return address != null &&
                (ipAddressPredicate.test(iface.getIpv4Address()) || ipAddressPredicate.test(iface.getIpv6Address()));
    }
}

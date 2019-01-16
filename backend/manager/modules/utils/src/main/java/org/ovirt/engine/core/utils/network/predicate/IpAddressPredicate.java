package org.ovirt.engine.core.utils.network.predicate;

import java.net.InetAddress;
import java.util.Objects;
import java.util.function.Predicate;

import org.ovirt.engine.core.utils.NetworkUtils;

public final class IpAddressPredicate implements Predicate<String> {

    private final String baseAddressString;
    private final InetAddress baseAddress;

    public IpAddressPredicate(String baseAddress) {
        this.baseAddressString = baseAddress;
        this.baseAddress = NetworkUtils.toInetAddressOrNull(baseAddress);
    }

    @Override
    public boolean test(String address) {
        if (Objects.equals(baseAddressString, address)) {
            return true;
        }
        final InetAddress inetAddress = NetworkUtils.toInetAddressOrNull(address);
        return address != null && inetAddress != null && baseAddress != null && baseAddress.equals(inetAddress);
    }
}

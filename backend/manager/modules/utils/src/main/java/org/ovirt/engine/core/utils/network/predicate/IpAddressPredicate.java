package org.ovirt.engine.core.utils.network.predicate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.function.Predicate;

public final class IpAddressPredicate implements Predicate<String> {

    private final String baseAddressString;
    private final InetAddress baseAddress;

    public IpAddressPredicate(String baseAddress) {
        this.baseAddressString = baseAddress;
        this.baseAddress = convertToInetAddress(baseAddress);
    }

    @Override
    public boolean test(String address) {
        if (Objects.equals(baseAddressString, address)) {
            return true;
        }
        final InetAddress inetAddress = convertToInetAddress(address);
        return address != null && inetAddress != null && baseAddress != null && baseAddress.equals(inetAddress);
    }

    private InetAddress convertToInetAddress(String ipAddress) {
        if (ipAddress == null) {
            return null;
        } else {
            try {
                return InetAddress.getByName(ipAddress);
            } catch (UnknownHostException e) {
                return null;
            }
        }
    }
}

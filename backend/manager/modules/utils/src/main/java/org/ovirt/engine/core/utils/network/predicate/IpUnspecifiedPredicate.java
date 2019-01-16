package org.ovirt.engine.core.utils.network.predicate;

import java.net.InetAddress;
import java.util.function.Predicate;

import org.ovirt.engine.core.utils.NetworkUtils;

public class IpUnspecifiedPredicate implements Predicate<String> {

    private static final IpUnspecifiedPredicate INSTANCE = new IpUnspecifiedPredicate();

    public static IpUnspecifiedPredicate ipUnspecifiedPredicate() {
        return INSTANCE;
    }

    private IpUnspecifiedPredicate() {
        super();
    }

    @Override
    public boolean test(String address) {
        final InetAddress inetAddress = NetworkUtils.toInetAddressOrNull(address);
        return inetAddress == null || inetAddress.isAnyLocalAddress();
    }
}

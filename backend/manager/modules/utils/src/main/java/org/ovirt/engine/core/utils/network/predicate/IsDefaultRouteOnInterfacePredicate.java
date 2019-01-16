package org.ovirt.engine.core.utils.network.predicate;

import static org.ovirt.engine.core.utils.network.predicate.IpUnspecifiedPredicate.ipUnspecifiedPredicate;

import java.util.function.Predicate;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public final class IsDefaultRouteOnInterfacePredicate implements Predicate<VdsNetworkInterface> {

    private static final IsDefaultRouteOnInterfacePredicate INSTANCE = new IsDefaultRouteOnInterfacePredicate();

    public static IsDefaultRouteOnInterfacePredicate isDefaultRouteOnInterfacePredicate() {
        return INSTANCE;
    }

    private IsDefaultRouteOnInterfacePredicate() {
        super();
    }

    @Override
    public boolean test(VdsNetworkInterface iface) {
        return iface != null && (iface.isIpv4DefaultRoute() || hasIpv6Gateway(iface));
    }

    private boolean hasIpv6Gateway(VdsNetworkInterface iface) {
        return iface!= null && !ipUnspecifiedPredicate().test(iface.getIpv6Gateway());
    }
}

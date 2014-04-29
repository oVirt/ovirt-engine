package org.ovirt.engine.core.vdsbroker.vdsbroker.predicates;

import org.apache.commons.lang.Validate;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.utils.linq.Predicate;

public final class IsNetworkOnInterfacePredicate implements Predicate<VdsNetworkInterface> {
    private final String networkName;

    public IsNetworkOnInterfacePredicate(String networkName) {
        Validate.notNull(networkName, "networkName can not be null");
        this.networkName = networkName;
    }

    @Override
    public boolean eval(VdsNetworkInterface vdsNetworkInterface) {
        return networkName.equals(vdsNetworkInterface.getNetworkName());
    }
}

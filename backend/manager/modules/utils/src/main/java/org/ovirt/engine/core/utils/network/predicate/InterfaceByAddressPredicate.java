package org.ovirt.engine.core.utils.network.predicate;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.utils.linq.Predicate;

public class InterfaceByAddressPredicate implements Predicate<VdsNetworkInterface> {

    private final String managementAddress;

    public InterfaceByAddressPredicate(String managementAddress) {
        this.managementAddress = managementAddress;
    }

    @Override
    public boolean eval(VdsNetworkInterface iface) {
        return managementAddress == null ? false : managementAddress.equals(iface.getAddress());
    }

}

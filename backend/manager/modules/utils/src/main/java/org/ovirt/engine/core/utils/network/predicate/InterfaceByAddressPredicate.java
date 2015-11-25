package org.ovirt.engine.core.utils.network.predicate;

import java.util.function.Predicate;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public class InterfaceByAddressPredicate implements Predicate<VdsNetworkInterface> {

    private final String managementAddress;

    public InterfaceByAddressPredicate(String managementAddress) {
        this.managementAddress = managementAddress;
    }

    @Override
    public boolean test(VdsNetworkInterface iface) {
        return managementAddress == null ? false : managementAddress.equals(iface.getAddress());
    }

}

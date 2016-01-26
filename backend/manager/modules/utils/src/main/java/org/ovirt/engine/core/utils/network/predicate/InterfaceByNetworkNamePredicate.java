package org.ovirt.engine.core.utils.network.predicate;

import java.util.function.Predicate;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public final class InterfaceByNetworkNamePredicate implements Predicate<VdsNetworkInterface> {
    final String hostManagementNetworkName;

    public InterfaceByNetworkNamePredicate(String hostManagementNetworkName) {
        this.hostManagementNetworkName = hostManagementNetworkName;
    }

    @Override
    public boolean test(VdsNetworkInterface iface) {
        return iface.getNetworkName() != null && iface.getNetworkName().equals(hostManagementNetworkName);
    }
}

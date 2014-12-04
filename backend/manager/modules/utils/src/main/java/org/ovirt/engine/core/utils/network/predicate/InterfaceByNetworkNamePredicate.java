package org.ovirt.engine.core.utils.network.predicate;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.utils.linq.Predicate;

final public class InterfaceByNetworkNamePredicate implements Predicate<VdsNetworkInterface> {
    final String hostManagementNetworkName;

    public InterfaceByNetworkNamePredicate(String hostManagementNetworkName) {
        this.hostManagementNetworkName = hostManagementNetworkName;
    }

    @Override
    public boolean eval(VdsNetworkInterface iface) {
        return iface.getNetworkName() != null && iface.getNetworkName().equals(hostManagementNetworkName);
    }
}

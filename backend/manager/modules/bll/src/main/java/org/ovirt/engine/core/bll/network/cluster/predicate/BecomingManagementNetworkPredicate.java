package org.ovirt.engine.core.bll.network.cluster.predicate;

import java.util.Objects;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;

@Singleton
@Named
final class BecomingManagementNetworkPredicate implements Predicate<NetworkCluster> {

    private final NetworkClusterDao networkClusterDao;

    @Inject
    BecomingManagementNetworkPredicate(NetworkClusterDao networkClusterDao) {
        Objects.requireNonNull(networkClusterDao, "networkClusterDao cannot be null");
        this.networkClusterDao = networkClusterDao;
    }

    @Override
    public boolean test(NetworkCluster networkCluster) {
        if (networkCluster.isManagement()) {
            final NetworkCluster currentNetworkCluster = networkClusterDao.get(networkCluster.getId());
            if (currentNetworkCluster != null && !currentNetworkCluster.isManagement()) {
                return true;
            }
        }
        return false;
    }
}

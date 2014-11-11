package org.ovirt.engine.core.bll.network.cluster.predicate;

import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.utils.linq.Predicate;

@Singleton
@Named
final class ManagementNetworkAppointmentPredicate implements Predicate<NetworkCluster> {

    private final NetworkClusterDao networkClusterDao;

    @Inject
    ManagementNetworkAppointmentPredicate(NetworkClusterDao networkClusterDao) {
        Objects.requireNonNull(networkClusterDao, "networkClusterDao cannot be null");
        this.networkClusterDao = networkClusterDao;
    }

    @Override
    public boolean eval(NetworkCluster networkCluster) {
        if (networkCluster.isManagement()) {
            final NetworkCluster currentNetworkCluster = networkClusterDao.get(networkCluster.getId());
            if (currentNetworkCluster != null && !currentNetworkCluster.isManagement()) {
                return true;
            }
        }
        return false;
    }
}

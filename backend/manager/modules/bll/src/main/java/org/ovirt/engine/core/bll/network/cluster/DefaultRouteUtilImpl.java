package org.ovirt.engine.core.bll.network.cluster;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;

@Singleton
final class DefaultRouteUtilImpl implements DefaultRouteUtil {

    private final NetworkClusterDao networkClusterDao;

    @Inject
    public DefaultRouteUtilImpl(NetworkClusterDao networkClusterDao) {
        this.networkClusterDao = networkClusterDao;
    }

    @Override
    public boolean isDefaultRouteNetwork(Guid networkId, Guid clusterId) {
        final NetworkCluster networkCluster = networkClusterDao.get(new NetworkClusterId(clusterId, networkId));
        return networkCluster != null && networkCluster.isDefaultRoute();
    }

}

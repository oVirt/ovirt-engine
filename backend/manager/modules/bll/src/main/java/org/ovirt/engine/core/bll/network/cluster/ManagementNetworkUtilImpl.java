package org.ovirt.engine.core.bll.network.cluster;

import java.util.List;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeLocator;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@Singleton
final class ManagementNetworkUtilImpl implements ManagementNetworkUtil {

    @Override
    public Network getManagementNetwork(Guid clusterId) {
        return getNetworkDao().getManagementNetwork(clusterId);
    }

    @Override
    public boolean isManagementNetwork(Guid networkId) {
        final List<NetworkCluster> networkClusters = getNetworkClusterDao().getAllForNetwork(networkId);
        final NetworkCluster managementNetworkCluster =
                LinqUtils.firstOrNull(networkClusters, new Predicate<NetworkCluster>() {
                    @Override
                    public boolean eval(NetworkCluster networkCluster) {
                        return networkCluster.isManagement();
                    }
                });
        return managementNetworkCluster != null;
    }

    @Override
    public boolean isManagementNetwork(Guid networkId, Guid clusterId) {
        final NetworkCluster networkCluster = getNetworkClusterDao().get(new NetworkClusterId(clusterId, networkId));
        return networkCluster != null && networkCluster.isManagement();
    }

    private NetworkClusterDao getNetworkClusterDao() {
        return DbFacadeLocator.getDbFacade().getNetworkClusterDao();
    }

    private NetworkDao getNetworkDao() {
        return DbFacadeLocator.getDbFacade().getNetworkDao();
    }
}

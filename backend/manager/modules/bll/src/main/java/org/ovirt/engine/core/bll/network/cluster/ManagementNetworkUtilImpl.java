package org.ovirt.engine.core.bll.network.cluster;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.Validate;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

@Singleton
final class ManagementNetworkUtilImpl implements ManagementNetworkUtil {

    private final NetworkDao networkDao;
    private final NetworkClusterDao networkClusterDao;

    @Inject
    ManagementNetworkUtilImpl(NetworkDao networkDao, NetworkClusterDao networkClustrerDao) {
        Validate.notNull(networkDao, "networkDao cannot be null");
        Validate.notNull(networkClustrerDao, "networkClustrerDao cannot be null");

        this.networkDao = networkDao;
        this.networkClusterDao = networkClustrerDao;
    }

    @Override
    public Network getManagementNetwork(Guid clusterId) {
        return networkDao.getManagementNetwork(clusterId);
    }

    @Override
    public boolean isManagementNetwork(Guid networkId) {
        final List<NetworkCluster> networkClusters = networkClusterDao.getAllForNetwork(networkId);
        return networkClusters.stream().anyMatch(NetworkCluster::isManagement);
    }

    @Override
    public boolean isManagementNetwork(Guid networkId, Guid clusterId) {
        final NetworkCluster networkCluster = networkClusterDao.get(new NetworkClusterId(clusterId, networkId));
        return networkCluster != null && networkCluster.isManagement();
    }

    @Override
    public boolean isManagementNetwork(String networkName, Guid clusterId) {
        final Network managementNetwork = getManagementNetwork(clusterId);
        return managementNetwork != null && managementNetwork.getName().equals(networkName);
    }

    /**
     * Retrieves the default management network name from the {@link Config}.
     * <p/>Should be used in very rare cases. {@link #getManagementNetwork(Guid)} provides more appropriate solution
     * for vast majority of the cases.
     *
     * @return the default management network name.
     */
    @Override
    public String getDefaultManagementNetworkName() {
        return Config.getValue(ConfigValues.DefaultManagementNetwork);
    }
}

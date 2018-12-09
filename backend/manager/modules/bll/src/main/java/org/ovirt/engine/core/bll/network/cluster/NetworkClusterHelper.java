package org.ovirt.engine.core.bll.network.cluster;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;

/**
 * Class to hold common methods that are used in several different places.
 */
public class NetworkClusterHelper {

    private final NetworkClusterDao networkClusterDao;
    private final NetworkAttachmentDao networkAttachmentDao;
    private final VdsStaticDao vdsStaticDao;
    private final VdsDynamicDao vdsDynamicDao;
    private final ManagementNetworkUtil managementNetworkUtil;

    @Inject
    public NetworkClusterHelper(NetworkClusterDao networkClusterDao,
            NetworkAttachmentDao networkAttachmentDao,
            VdsStaticDao vdsStaticDao,
            VdsDynamicDao vdsDynamicDao,
            ManagementNetworkUtil managementNetworkUtil) {
        this.networkClusterDao = Objects.requireNonNull(networkClusterDao);
        this.networkAttachmentDao = Objects.requireNonNull(networkAttachmentDao);
        this.vdsStaticDao = Objects.requireNonNull(vdsStaticDao);
        this.vdsDynamicDao = Objects.requireNonNull(vdsDynamicDao);
        this.managementNetworkUtil = Objects.requireNonNull(managementNetworkUtil);
    }

    private NetworkCluster getManagementNetworkCluster(NetworkCluster networkCluster) {
        Guid clusterId = networkCluster.getClusterId();
        Network mgmt = managementNetworkUtil.getManagementNetwork(clusterId);
        return networkClusterDao.get(new NetworkClusterId(clusterId, mgmt.getId()));
    }

    public void removeNetworkAndReassignRoles(NetworkCluster networkCluster) {
        NetworkCluster oldNetworkCluster = networkClusterDao.get(networkCluster.getId());
        networkClusterDao.remove(networkCluster.getClusterId(), networkCluster.getNetworkId());
        networkAttachmentDao.removeByNetworkId(networkCluster.getNetworkId());

        boolean updateManagementNetwork = false;

        final NetworkCluster managementNetworkCluster = getManagementNetworkCluster(networkCluster);
        if (oldNetworkCluster.isDisplay()) {
            managementNetworkCluster.setDisplay(true);
            updateManagementNetwork = true;
        }

        if (oldNetworkCluster.isMigration()) {
            managementNetworkCluster.setMigration(true);
            updateManagementNetwork = true;
        }

        if (oldNetworkCluster.isDefaultRoute()) {
            managementNetworkCluster.setDefaultRoute(true);
            updateManagementNetwork = true;
        }

        if (updateManagementNetwork) {
            networkClusterDao.update(managementNetworkCluster);
        }
    }

    /**
     * Updates status of network. Update is performed only if there's at least one host in cluster, which [host]
     * has status {@link VDSStatus#Up}. If network is nonrequired, it set to {@link NetworkStatus#OPERATIONAL}.
     * Otherwise all hosts having status {@link VDSStatus#Up} in given cluster are scanned, whether all of them has
     * network of <em>networkName</em> attached to some nic. If so, <em>networkCluster</em> is marked as
     * {@link NetworkStatus#OPERATIONAL}, if not it's marked as {@link NetworkStatus#NON_OPERATIONAL}.
     *
     * @param clusterId clusterId of cluster to which <em>network</em> belongs.
     * @param network network to update.
     */
    public void setStatus(Guid clusterId, final Network network) {
        setStatus(clusterId, Collections.singletonList(network));
    }

    public void setStatus(Guid clusterId, final Collection<Network> networks) {
        final RequiredNetworkClusterStatusUpdater requiredNetworkClusterStatusUpdater =
                new RequiredNetworkClusterStatusUpdater(clusterId);
        List<NetworkCluster> networkClusters = networkClusterDao.getAllForCluster(clusterId);
        for (Network network : networks) {
            NetworkCluster networkCluster = networkClusters.stream().filter(nc -> nc.getNetworkId().equals(network.getId())).findFirst().orElse(null);
            boolean doUpdateNetworkClusterStatus = networkCluster != null;
            if (doUpdateNetworkClusterStatus) {
                if (networkCluster.isRequired()) {
                    requiredNetworkClusterStatusUpdater.update(networkCluster, network.getName());
                } else {
                    updateNetworkClusterStatus(networkCluster, NetworkStatus.OPERATIONAL);
                }
            }
        }
    }

    /**
     * Updates <em>networkCluster</em> with new <em>networkStatus</em> and persists passed <em>networkCluster</em> if
     * <em>newStatus</em> is different from current one saving DB roundtrip if no update is needed.
     *
     * @param networkCluster NetworkCluster record to update
     * @param newStatus network status to be set
     */
    private void updateNetworkClusterStatus(NetworkCluster networkCluster, NetworkStatus newStatus) {
        if (networkCluster.getStatus() != newStatus) {
            networkCluster.setStatus(newStatus);
            networkClusterDao.updateStatus(networkCluster);
        }
    }

    private class RequiredNetworkClusterStatusUpdater {

        private final Guid clusterId;
        private Boolean atLeastOneHostIsUp;

        private RequiredNetworkClusterStatusUpdater(Guid clusterId) {
            this.clusterId = clusterId;
        }

        /**
         * Updates status of the required network in the cluster. See {@link #setStatus(Guid, Network)} for details.
         */
        public void update(NetworkCluster networkCluster, String networkName) {
            if (isAtLeastOneHostIsUp()) {
                final NetworkStatus networkStatusToSet = vdsStaticDao.checkIfExistsHostThatMissesNetworkInCluster(
                        clusterId,
                        networkName,
                        VDSStatus.Up)
                                ? NetworkStatus.NON_OPERATIONAL
                                : NetworkStatus.OPERATIONAL;

                updateNetworkClusterStatus(networkCluster, networkStatusToSet);
            }
        }

        private boolean isAtLeastOneHostIsUp() {
            if (atLeastOneHostIsUp == null) {
                atLeastOneHostIsUp = vdsDynamicDao.checkIfExistsHostWithStatusInCluster(clusterId, VDSStatus.Up);
            }
            return atLeastOneHostIsUp;
        }
    }
}

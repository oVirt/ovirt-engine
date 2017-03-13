package org.ovirt.engine.core.bll.network.cluster;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.network.host.HostNicsUtil;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;

/**
 * Class to hold common methods that are used in several different places.
 */
public class NetworkClusterHelper {

    private final NetworkClusterDao networkClusterDao;
    private final NetworkAttachmentDao networkAttachmentDao;
    private final VdsDao vdsDao;
    private final ManagementNetworkUtil managementNetworkUtil;
    private final HostNicsUtil hostNicsUtil;

    @Inject
    public NetworkClusterHelper(NetworkClusterDao networkClusterDao,
            NetworkAttachmentDao networkAttachmentDao,
            VdsDao vdsDao,
            ManagementNetworkUtil managementNetworkUtil,
            HostNicsUtil hostNicsUtil) {
        this.networkClusterDao = Objects.requireNonNull(networkClusterDao);
        this.networkAttachmentDao = Objects.requireNonNull(networkAttachmentDao);
        this.vdsDao = Objects.requireNonNull(vdsDao);
        this.managementNetworkUtil = Objects.requireNonNull(managementNetworkUtil);
        this.hostNicsUtil = Objects.requireNonNull(hostNicsUtil);
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
        RequiredNetworkClusterStatusUpdater requiredNetworkClusterStatusUpdater =
                new RequiredNetworkClusterStatusUpdater(clusterId);
        for (Network network : networks) {
            NetworkCluster networkCluster = networkClusterDao.get(new NetworkClusterId(clusterId, network.getId()));
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
     * @param hosts list of hosts to check
     * @param networkName name of network
     * @return true if there's at least one host, which does not have given network attached to one of its nics.
     */
    private boolean atLeastOneHostDoesNotHaveNetworkAttached(List<VDS> hosts, String networkName) {
        for (VDS host : hosts) {
            List<VdsNetworkInterface> hostInterfaces = hostNicsUtil.findHostNics(host.getStaticData());
            boolean hostHasInterfaceWithGivenNetwork =
                    hostInterfaces.stream().anyMatch(e -> StringUtils.equals(e.getNetworkName(), networkName));
            if (!hostHasInterfaceWithGivenNetwork) {
                return true;
            }
        }
        return false;
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
        private List<VDS> activeHostsInCluster;

        private RequiredNetworkClusterStatusUpdater(Guid clusterId) {
            this.clusterId = clusterId;
        }

        /**
         * Updates status of the required network in the cluster. See {@link #setStatus(Guid, Network)} for details.
         */
        public void update(NetworkCluster networkCluster, String networkName) {
            List<VDS> hostsWithUpStatusInCluster = findActiveHostsInCluster(clusterId);
            boolean atLeastOneHostIsUp = !hostsWithUpStatusInCluster.isEmpty();

            if (atLeastOneHostIsUp) {
                NetworkStatus networkStatusToSet =
                        atLeastOneHostDoesNotHaveNetworkAttached(hostsWithUpStatusInCluster, networkName)
                                ? NetworkStatus.NON_OPERATIONAL
                                : NetworkStatus.OPERATIONAL;

                updateNetworkClusterStatus(networkCluster, networkStatusToSet);
            }
        }

        /**
         * Finds active hosts in the give cluster.
         *
         * @param clusterId
         *            cluster id
         * @return all hosts which has status {@link VDSStatus#Up}.
         */
        private List<VDS> findActiveHostsInCluster(Guid clusterId) {
            if (activeHostsInCluster == null) {
                final List<VDS> hostsInCluster = vdsDao.getAllForCluster(clusterId);
                activeHostsInCluster =
                        hostsInCluster.stream().filter(e -> e.getStatus() == VDSStatus.Up).collect(Collectors.toList());
            }
            return activeHostsInCluster;
        }
    }
}

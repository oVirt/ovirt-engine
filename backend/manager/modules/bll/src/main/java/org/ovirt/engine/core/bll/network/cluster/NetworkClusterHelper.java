package org.ovirt.engine.core.bll.network.cluster;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.di.Injector;

/**
 * Class to hold common static methods that are used in several different places.
 */
public class NetworkClusterHelper {

    private final NetworkCluster networkCluster;
    private NetworkCluster managementNetworkCluster;

    public NetworkClusterHelper(NetworkCluster networkCluster) {
        this.networkCluster = networkCluster;
    }

    private static NetworkClusterDao getNetworkClusterDao() {
        return DbFacade.getInstance().getNetworkClusterDao();
    }

    private NetworkAttachmentDao getNetworkAttachmentDao() {
        return Injector.get(NetworkAttachmentDao.class);
    }

    private static VdsDao getVdsDao() {
        return Injector.get(VdsDao.class);
    }

    private NetworkCluster getManagementNetworkCluster() {
        if (managementNetworkCluster == null) {
            Guid clusterId = networkCluster.getClusterId();
            final ManagementNetworkUtil managementNetworkUtil = Injector.get(ManagementNetworkUtil.class);
            Network mgmt = managementNetworkUtil.getManagementNetwork(clusterId);
            managementNetworkCluster = getNetworkClusterDao().get(new NetworkClusterId(clusterId, mgmt.getId()));
        }

        return managementNetworkCluster;
    }

    public void removeNetworkAndReassignRoles() {
        NetworkCluster oldNetworkCluster = getNetworkClusterDao().get(networkCluster.getId());
        getNetworkClusterDao().remove(networkCluster.getClusterId(), networkCluster.getNetworkId());
        getNetworkAttachmentDao().removeByNetworkId(networkCluster.getNetworkId());

        boolean updateManagementNetwork = false;

        if (oldNetworkCluster.isDisplay()) {
            getManagementNetworkCluster().setDisplay(true);
            updateManagementNetwork = true;
        }

        if (oldNetworkCluster.isMigration()) {
            getManagementNetworkCluster().setMigration(true);
            updateManagementNetwork = true;
        }

        if (updateManagementNetwork) {
            getNetworkClusterDao().update(managementNetworkCluster);
        }
    }

    /**
     * Updates status of network. Update is performed only if there's at least one host in cluster, which [host]
     * has status {@link VDSStatus#Up}. If network is nonrequired, it set to {@link NetworkStatus#OPERATIONAL}.
     * Otherwise all hosts having status {@link VDSStatus#Up} in given cluster are scanned, whether all of them has
     * network of <em>networkName</em> attached to some nic. If so, <em>networkCluster</em> is marked as
     * {@link NetworkStatus#OPERATIONAL}, if not it's marked as {@link NetworkStatus#NON_OPERATIONAL}.
     *
     *
     * @param clusterId clusterId of cluster to which <em>network</em> belongs.
     * @param network network to update.
     */
    public static void setStatus(Guid clusterId, final Network network) {
        NetworkCluster networkCluster = getNetworkClusterDao().get(new NetworkClusterId(clusterId, network.getId()));
        boolean doUpdateNetworkClusterStatus = networkCluster != null;
        if (doUpdateNetworkClusterStatus) {
            if (networkCluster.isRequired()) {
                updateStatusOfRequiredNetworkCluster(networkCluster, clusterId, network.getName());
            } else {
                updateNetworkClusterStatus(networkCluster, NetworkStatus.OPERATIONAL);
            }
        }
    }


    /**
     * updates status of required network in cluster. See this {@link #setStatus(Guid, Network) javadoc} for details.
     */
    private static void updateStatusOfRequiredNetworkCluster(NetworkCluster networkCluster, Guid clusterId, String networkName) {
        List<VDS> hostsInCluster = getVdsDao().getAllForCluster(clusterId);
        List<VDS> hostsWithUpStatusInCluster = getHostsWithUpStatus(hostsInCluster);
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
     *
     * @param hosts list of hosts to check
     * @param networkName name of network
     * @return true if there's at least one host, which does not have given network attached to one of its nics.
     */
    private static boolean atLeastOneHostDoesNotHaveNetworkAttached(List<VDS> hosts, String networkName) {
        for (VDS host : hosts) {
            List<VdsNetworkInterface> hostInterfaces = getHostInterfaces(host.getId());
            boolean hostHasInterfaceWithGivenNetwork =
                    hostInterfaces.stream().anyMatch(e -> StringUtils.equals(e.getNetworkName(), networkName));
            if (!hostHasInterfaceWithGivenNetwork) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param hostId id of host.
     * @return all <em>VdsNetworkInterface</em> records for given hostId.
     */
    private static List<VdsNetworkInterface> getHostInterfaces(Guid hostId) {
        IdQueryParameters params = new IdQueryParameters(hostId);
        return Backend.getInstance().runInternalQuery(VdcQueryType.GetVdsInterfacesByVdsId, params).getReturnValue();
    }

    /**
     *
     * @param hosts hosts to filter
     * @return all hosts which has status {@link VDSStatus#Up}.
     */
    private static List<VDS> getHostsWithUpStatus(List<VDS> hosts) {
        return hosts.stream().filter(e -> e.getStatus() == VDSStatus.Up).collect(Collectors.toList());
    }

    /**
     * Updates <em>networkCluster</em> with new <em>networkStatus</em> and persists passed <em>networkCluster</em> if
     * <em>newStatus</em> is different from current one saving DB roundtrip if no update is needed.
     *
     * @param networkCluster NetworkCluster record to update
     * @param newStatus network status to be set
     */
    private static void updateNetworkClusterStatus(NetworkCluster networkCluster, NetworkStatus newStatus) {
        if (networkCluster.getStatus() != newStatus) {
            networkCluster.setStatus(newStatus);
            getNetworkClusterDao().updateStatus(networkCluster);
        }
    }

}

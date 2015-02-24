package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.BrickDetails;
import org.ovirt.engine.core.common.businessentities.gluster.BrickProperties;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServer;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerInfo;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeAdvancedDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.businessentities.gluster.PeerStatus;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.gluster.GlusterFeatureSupported;
import org.ovirt.engine.core.common.utils.ListUtils;
import org.ovirt.engine.core.common.utils.gluster.GlusterCoreUtil;
import org.ovirt.engine.core.common.vdscommands.RemoveVdsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.common.vdscommands.gluster.AddGlusterServerVDSParameters;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeAdvancedDetailsVDSParameters;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumesListVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for keeping the Gluster related data of engine in sync with the actual data retrieved from
 * GlusterFS. This helps to make sure that any changes done on Gluster servers using the Gluster CLI are propagated to
 * engine as well.
 */
public class GlusterSyncJob extends GlusterJob {
    private final Logger log = LoggerFactory.getLogger(GlusterSyncJob.class);
    private static final GlusterSyncJob instance = new GlusterSyncJob();
    private final AuditLogDirector auditLogDirector = new AuditLogDirector();

    private GlusterSyncJob() {
    }

    public static GlusterSyncJob getInstance() {
        return instance;
    }

    /**
     * Refreshes details of all volume across all clusters being managed in the engine. It can end up doing the
     * following in engine DB to make sure that the volume details in engine DB are in sync with GlusterFS:<br>
     * <li>Insert volumes</li><li>Delete volumes</li><li>Update properties of volume e.g. status, volume type</li><li>
     * Add / remove bricks to / from volumes</li><li>Set / Unset volume options</li><br>
     * These are all fetched using the 'volume info' command on gluster CLI, which is relatively lightweight, and hence
     * this method is scheduled more frequently as compared to the other method <code>refreshHeavyWeightData</code>,
     * which uses 'volume status' to fetch and update status of volume bricks.
     */
    @OnTimerMethodAnnotation("refreshLightWeightData")
    public void refreshLightWeightData() {
        log.debug("Refreshing Gluster Data [lightweight]");
        List<VDSGroup> clusters = getClusterDao().getAll();

        for (VDSGroup cluster : clusters) {
            if (cluster.supportsGlusterService()) {
                try {
                    refreshClusterData(cluster);
                } catch (Exception e) {
                    log.error("Error while refreshing Gluster lightweight data of cluster '{}': {}",
                            cluster.getName(),
                            e.getMessage());
                    log.debug("Exception", e);
                }
            }
        }
    }

    private void refreshClusterData(VDSGroup cluster) {
        log.debug("Refreshing Gluster lightweight Data for cluster '{}'", cluster.getName());

        List<VDS> existingServers = getVdsDao().getAllForVdsGroup(cluster.getId());
        VDS upServer = getClusterUtils().getUpServer(cluster.getId());
        if (upServer == null) {
            log.debug("No server UP in cluster '{}'. Can't refresh it's data at this point.", cluster.getName());
            return;
        }

        refreshServerData(cluster, upServer, existingServers);
        refreshVolumeData(cluster, upServer, existingServers);
    }

    /**
     * If any servers have been added to the Gluster cluster directly from the Gluster CLI, we still don't add them
     * automatically to the engine DB, as addition of servers requires user approval from the GUI. If the cluster is a
     * gluster-only cluster, and one or more servers have been removed directly from the Gluster CLI, we remove them
     * from the engine DB, and also invoke the corresponding VDS command.
     *
     * @param cluster
     * @param upServer
     * @param existingServers
     */
    private void refreshServerData(VDSGroup cluster, VDS upServer, List<VDS> existingServers) {
        if (cluster.supportsVirtService()) {
            // If the cluster supports virt service as well, we should not be removing any servers from it, even if they
            // have been removed from the Gluster cluster using the Gluster cli, as they could potentially be used for
            // running VMs
            log.debug("As cluster '{}' supports virt service as well, it's servers will not be synced with glusterfs",
                    cluster.getName());
            return;
        }

        acquireLock(cluster.getId());

        log.debug("Refreshing Gluster Server data for cluster '{}' using server '{}'",
                cluster.getName(),
                upServer.getName());
        try {
            List<GlusterServerInfo> fetchedServers = fetchServers(cluster, upServer, existingServers);
            if (fetchedServers != null) {
                syncServers(cluster.getId(), existingServers, fetchedServers);
            }
        } catch(Exception e) {
            log.error("Error while refreshing server data for cluster '{}' from database: {}",
                    cluster.getName(),
                    e.getMessage());
            log.debug("Exception", e);
        } finally {
            releaseLock(cluster.getId());
        }
    }

    private void syncServers(Guid clusterId, List<VDS> existingServers, List<GlusterServerInfo> fetchedServers) {
        log.debug("Existing servers list returned '{}' comparing with fetched servers '{}'", existingServers, fetchedServers);

        boolean serverRemoved = false;
        Network glusterNetwork = findGlusterNetwork(clusterId);
        for (VDS server : existingServers) {

            if (isRemovableStatus(server.getStatus())) {
                GlusterServerInfo glusterServer = findGlusterServer(server, fetchedServers);
                if (glusterServer == null) {
                    log.info("Server '{}' has been removed directly using the gluster CLI. Removing it from engine as well.",
                            server.getName());
                    logUtil.logServerMessage(server, AuditLogType.GLUSTER_SERVER_REMOVED_FROM_CLI);
                    try (EngineLock lock = getGlusterUtil().acquireGlusterLockWait(server.getId())) {
                        removeServerFromDb(server);
                        // if last but one server, reset alternate probed address for last server
                        checkAndResetKnownAddress(existingServers, server);
                        // remove the server from resource manager
                        runVdsCommand(VDSCommandType.RemoveVds, new RemoveVdsVDSCommandParameters(server.getId()));
                        serverRemoved = true;
                    } catch (Exception e) {
                        log.error("Error while removing server '{}' from database: {}",
                                server.getName(),
                                e.getMessage());
                        log.debug("Exception", e);
                    }
                }
                else if (server.getStatus() == VDSStatus.Up && glusterServer.getStatus() == PeerStatus.DISCONNECTED) {
                    // check gluster is running, if down then move the host to Non-Operational
                    VDSReturnValue returnValue =
                            runVdsCommand(VDSCommandType.GlusterServersList,
                                    new VdsIdVDSCommandParametersBase(server.getId()));
                    if (!returnValue.getSucceeded()) {
                        setNonOperational(server);
                    }
                } else {
                    // check if all interfaces with gluster network have been peer probed.
                    peerProbeAlternateInterfaces(glusterNetwork, server);
                }
            }
        }
        if (serverRemoved) {
            log.info("Servers detached using gluster CLI is removed from engine after inspecting the Gluster servers"
                    + " list returned '{}' - comparing with db servers '{}'",
                    fetchedServers, existingServers);
        }
    }

    // Check if only 1 host remaining in cluster, if so reset it's known address so that new host will
    // be peer probed with this alternate address
    private void checkAndResetKnownAddress(List<VDS> servers, VDS removedServer) {
        if (servers.size() == 2) {
            for (VDS server : servers) {
                // set the known address on the remaining server.
                if (!Objects.equals(server.getId(), removedServer.getId())) {
                    getGlusterServerDao().updateKnownAddresses(server.getId(), null);
                }
            }
        }
    }

    private void peerProbeAlternateInterfaces(Network glusterNetwork, VDS host) {
        if (glusterNetwork == null || host.getStatus() != VDSStatus.Up) {
            return;
        }
        GlusterServer glusterServer = getGlusterServerDao().get(host.getId());
        if (glusterServer == null) {
            return;
        }
        List<VdsNetworkInterface> interfaces = getInterfaceDao().getAllInterfacesForVds(host.getId());
        for (VdsNetworkInterface iface : interfaces) {
            if (glusterNetwork.getName().equals(iface.getNetworkName()) &&
                    StringUtils.isNotBlank(iface.getAddress())
                    && !glusterServer.getKnownAddresses().contains(iface.getAddress())) {
                // get another server in the cluster
                VDS upServer = getAlternateUpServerInCluster(host.getVdsGroupId(), host.getId());
                if (upServer != null) {
                    boolean peerProbed = glusterPeerProbeAdditionalInterface(upServer.getId(), iface.getAddress());
                    if (peerProbed) {
                        getGlusterServerDao().addKnownAddress(host.getId(), iface.getAddress());
                    }
                }
            }
        }

    }

    private Network findGlusterNetwork(Guid clusterId) {
        List<Network> allNetworksInCluster = getNetworkDao().getAllForCluster(clusterId);

        for (Network network : allNetworksInCluster) {
            if (network.getCluster().isGluster()) {
                return network;
            }
        }
        return null;
    }

    private VDS getAlternateUpServerInCluster(Guid clusterId, Guid vdsId) {
        List<VDS> vdsList = getVdsDao().getAllForVdsGroupWithStatus(clusterId, VDSStatus.Up);
        // If the cluster already having Gluster servers, get an up server
        if (vdsList.isEmpty()) {
            return null;
        }
        for (VDS vds : vdsList) {
            if (!vdsId.equals(vds.getId())) {
                return vds;
            }
        }
        return null;
    }

    private boolean glusterPeerProbeAdditionalInterface(Guid upServerId, String newServerName) {
        try {
            VDSReturnValue returnValue =
                    runVdsCommand(VDSCommandType.AddGlusterServer,
                            new AddGlusterServerVDSParameters(upServerId, newServerName));
            if (!returnValue.getSucceeded()) {
                AuditLogableBase logable = new AuditLogableBase(upServerId);
                logable.updateCallStackFromThrowable(returnValue.getExceptionObject());
                auditLogDirector.log(logable, AuditLogType.GLUSTER_SERVER_ADD_FAILED);
            }
            return returnValue.getSucceeded();
        } catch (Exception e) {
            log.info("Exception in peer probing alernate name '{}' on host with id '{}'", newServerName, upServerId);
            log.debug("Exception", e);
            return false;
        }
    }

    private void removeServerFromDb(final VDS server) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {

                removeVdsStatisticsFromDb(server);
                removeVdsDynamicFromDb(server);
                removeVdsStaticFromDb(server);

                return null;
            }
        });
    }

    /**
     * We need to be particularly careful about what servers we remove from the DB. A newly added (bootstrapped) server
     * gets peer probed after it's first reboot, and we don't want to accidentally remove such legitimate servers just
     * before they are probed.
     *
     * @param status
     * @return
     */
    private boolean isRemovableStatus(VDSStatus status) {
        switch (status) {
        case Up:
        case Down:
            return true;
        default:
            return false;
        }
    }

    /**
     * Returns the equivalent GlusterServer from the list of fetched servers.
     *
     * @param server
     * @param fetchedServers
     * @return GlusterServerInfo
     */
    private GlusterServerInfo findGlusterServer(VDS server, List<GlusterServerInfo> fetchedServers) {
        if (GlusterFeatureSupported.glusterHostUuidSupported(server.getVdsGroupCompatibilityVersion())) {
            // compare gluster host uuid stored in server with the ones fetched from list
            GlusterServer glusterServer = getGlusterServerDao().getByServerId(server.getId());
            for (GlusterServerInfo fetchedServer : fetchedServers) {
                if (fetchedServer.getUuid().equals(glusterServer.getGlusterServerUuid())) {
                    return fetchedServer;
                }
            }
        } else {
            List<String> vdsIps = getVdsIps(server);
            for (GlusterServerInfo fetchedServer : fetchedServers) {
                if (fetchedServer.getHostnameOrIp().equals(server.getHostName())
                        || vdsIps.contains(fetchedServer.getHostnameOrIp())) {
                    return fetchedServer;
                }
            }
        }
        return null;
    }

    private List<String> getVdsIps(VDS vds) {
        List<String> vdsIps = new ArrayList<String>();
        for (VdsNetworkInterface iface : getInterfaceDao().getAllInterfacesForVds(vds.getId())) {
            if (iface.getAddress() != null) {
                vdsIps.add(iface.getAddress());
            }
        }
        return vdsIps;
    }

    private List<GlusterServerInfo> fetchServers(VDSGroup cluster, VDS upServer, List<VDS> existingServers) {
        // Create a copy of the existing servers as the fetchServer method can potentially remove elements from it
        List<VDS> tempServers = new ArrayList<VDS>(existingServers);
        List<GlusterServerInfo> fetchedServers = fetchServers(upServer, tempServers);

        if (fetchedServers == null) {
            log.error("gluster peer status command failed on all servers of the cluster '{}'."
                    + "Can't refresh it's data at this point.", cluster.getName());
            return null;
        }

        if (fetchedServers.size() == 1 && existingServers.size() > 2) {
            log.info("Gluster servers list fetched from server '{}' has only one server", upServer.getName());
            // It's possible that the server we are using to get list of servers itself has been removed from the
            // cluster, and hence is returning a single server (itself)
            GlusterServerInfo server = fetchedServers.iterator().next();
            if (isSameServer(upServer, server)) {
                // Find a different UP server, and get servers list from it
                tempServers.remove(upServer);
                upServer = getNewUpServer(tempServers, upServer);
                if (upServer == null) {
                    log.warn("The only UP server in cluster '{}' seems to have been removed from it using gluster CLI. "
                            + "Can't refresh it's data at this point.",
                            cluster.getName());
                    return null;
                }

                fetchedServers = fetchServers(upServer, tempServers);
                if (fetchedServers == null) {
                    log.warn("The only UP server in cluster '{}' (or the only one on which gluster peer status "
                            + "command is working) seems to have been removed from it using gluster CLI. "
                            + "Can't refresh it's data at this point.", cluster.getName());
                    return null;
                }
            }
        }
        return fetchedServers;
    }

    private boolean isSameServer(VDS upServer, GlusterServerInfo server) {
        if (GlusterFeatureSupported.glusterHostUuidSupported(upServer.getVdsGroupCompatibilityVersion())) {
            GlusterServer glusterUpServer = getGlusterServerDao().getByServerId(upServer.getId());
            return glusterUpServer.getGlusterServerUuid().equals(server.getUuid());
        } else {
            return server.getHostnameOrIp().equals(upServer.getHostName())
                || getVdsIps(upServer).contains(server.getHostnameOrIp());
        }
    }

    /**
     * Fetches list of gluster servers by executing the gluster peer status command on the given UP server. If the
     * gluster command fails, tries on other UP servers from the list of existing Servers recursively. Returns null if
     * the command fails on all the servers.
     *
     * @param upServer
     * @param existingServers
     * @return
     */
    private List<GlusterServerInfo> fetchServers(VDS upServer, List<VDS> existingServers) {
        List<GlusterServerInfo> fetchedServers = null;
        while (fetchedServers == null && !existingServers.isEmpty()) {
            log.debug("Fetching gluster servers list from server '{}'", upServer.getName());
            fetchedServers = fetchServers(upServer);
            if (fetchedServers == null) {
                log.info("Gluster servers list failed in server '{}' moving it to NonOperational",
                        upServer.getName());
                logUtil.logServerMessage(upServer, AuditLogType.GLUSTER_SERVERS_LIST_FAILED);
                // Couldn't fetch servers from the up server. Mark it as non-operational
                setNonOperational(upServer);
                existingServers.remove(upServer);
                upServer = getNewUpServer(existingServers, upServer);
            }
        }
        return fetchedServers;
    }

    private void setNonOperational(VDS server) {
        SetNonOperationalVdsParameters nonOpParams =
                new SetNonOperationalVdsParameters(server.getId(),
                        NonOperationalReason.GLUSTER_COMMAND_FAILED,
                        Collections.singletonMap(GlusterConstants.COMMAND, "gluster peer status"));
        Backend.getInstance().runInternalAction(VdcActionType.SetNonOperationalVds,
                nonOpParams,
                ExecutionHandler.createInternalJobContext());
    }

    /**
     * Returns an UP server from given list of servers, provided it is not same as the given server.
     *
     * @param servers
     * @param exceptServer
     * @return
     */
    private VDS getNewUpServer(List<VDS> servers, VDS exceptServer) {
        for (VDS server : servers) {
            if (server.getStatus() == VDSStatus.Up && !server.getId().equals(exceptServer.getId())) {
                return server;
            }
        }
        return null;
    }

    private void refreshVolumeData(VDSGroup cluster, VDS upServer, List<VDS> existingServers) {
        acquireLock(cluster.getId());
        try {
            // Pass a copy of the existing servers as the fetchVolumes method can potentially remove elements from it
            Map<Guid, GlusterVolumeEntity> volumesMap = fetchVolumes(upServer, new ArrayList<VDS>(existingServers));
            if (volumesMap == null) {
                log.error("gluster volume info command failed on all servers of the cluster '{}'."
                        + "Can't refresh it's data at this point.", cluster.getName());
                return;
            }

            // remove deleted volumes must happen before adding new ones,
            // to handle cases where user deleted a volume and created a
            // new one with same name in a very short time
            removeDeletedVolumes(cluster.getId(), volumesMap);
            updateExistingAndNewVolumes(cluster.getId(), volumesMap);
        } finally {
            releaseLock(cluster.getId());
        }
    }

    /**
     * Fetches list of gluster volumes by executing the gluster volume info command on the given UP server. If the
     * gluster command fails, tries on other UP servers from the list of existing Servers recursively. Returns null if
     * the command fails on all the servers.
     *
     * @param upServer
     * @param existingServers
     * @return
     */
    private Map<Guid, GlusterVolumeEntity> fetchVolumes(VDS upServer, List<VDS> existingServers) {
        Map<Guid, GlusterVolumeEntity> fetchedVolumes = null;
        while (fetchedVolumes == null && existingServers.size() > 0) {
            fetchedVolumes = fetchVolumes(upServer);
            if (fetchedVolumes == null) {
                // Couldn't fetch volumes from the up server. Mark it as non-operational
                logUtil.logServerMessage(upServer, AuditLogType.GLUSTER_VOLUME_INFO_FAILED);
                setNonOperational(upServer);
                existingServers.remove(upServer);
                upServer = getNewUpServer(existingServers, upServer);
            }
        }
        return fetchedVolumes;
    }

    @SuppressWarnings("unchecked")
    protected Map<Guid, GlusterVolumeEntity> fetchVolumes(VDS upServer) {
        VDSReturnValue result =
                runVdsCommand(VDSCommandType.GlusterVolumesList, new GlusterVolumesListVDSParameters(upServer.getId(),
                        upServer.getVdsGroupId()));

        return result.getSucceeded() ? (Map<Guid, GlusterVolumeEntity>) result.getReturnValue() : null;
    }

    private void removeDeletedVolumes(Guid clusterId, Map<Guid, GlusterVolumeEntity> volumesMap) {
        List<Guid> idsToRemove = new ArrayList<Guid>();
        for (GlusterVolumeEntity volume : getVolumeDao().getByClusterId(clusterId)) {
            if (!volumesMap.containsKey(volume.getId())) {
                idsToRemove.add(volume.getId());
                log.debug("Volume '{}' has been removed directly using the gluster CLI. Removing it from engine as well.",
                        volume.getName());
                logUtil.logVolumeMessage(volume, AuditLogType.GLUSTER_VOLUME_DELETED_FROM_CLI);
            }
        }

        if (!idsToRemove.isEmpty()) {
            try {
                getVolumeDao().removeAll(idsToRemove);
            } catch (Exception e) {
                log.error("Error while removing volumes from database!", e);
            }
        }
    }

    private void updateExistingAndNewVolumes(Guid clusterId, Map<Guid, GlusterVolumeEntity> volumesMap) {
        for (Entry<Guid, GlusterVolumeEntity> entry : volumesMap.entrySet()) {
            GlusterVolumeEntity volume = entry.getValue();
            log.debug("Analyzing volume '{}'", volume.getName());

            GlusterVolumeEntity existingVolume = getVolumeDao().getById(entry.getKey());
            if (existingVolume == null) {
                try {
                    createVolume(volume);
                } catch (Exception e) {
                    log.error("Could not save volume {} in database: {}", volume.getName(), e.getMessage());
                    log.debug("Exception", e);
                }
            } else {
                try {
                    log.debug("Volume '{}' exists in engine. Checking if it needs to be updated.",
                            existingVolume.getName());
                    updateVolume(existingVolume, volume);
                } catch (Exception e) {
                    log.error("Error while updating volume '{}': {}", volume.getName(), e.getMessage());
                    log.debug("Exception", e);
                }
            }
        }
    }

    /**
     * Creates a new volume in engine
     *
     * @param volume
     */
    private void createVolume(final GlusterVolumeEntity volume) {
        if (volume.getBricks() == null) {
            log.warn("Bricks of volume '{}' were not fetched. " +
                    "Hence will not add it to engine at this point.", volume.getName());
            return;
        }

        for (GlusterBrickEntity brick : volume.getBricks()) {
            if (brick == null) {
                log.warn("Volume '{}' contains a apparently corrupt brick(s). " +
                        "Hence will not add it to engine at this point.",
                        volume.getName());
                return;
            } else if (brick.getServerId() == null) {
                log.warn("Volume '{}' contains brick(s) from unknown hosts. " +
                        "Hence will not add it to engine at this point.",
                        volume.getName());
                return;
            }

            // Set initial brick status as similar to volume status.
            // As actual brick status is updated by another sync job,
            // till it happens, this gives better UI experience
            brick.setStatus(volume.getStatus());
        }

        logUtil.logVolumeMessage(volume, AuditLogType.GLUSTER_VOLUME_CREATED_FROM_CLI);
        log.debug("Volume '{}' has been created directly using the gluster CLI. Creating it in engine as well.",
                volume.getName());
        getVolumeDao().save(volume);
    }

    private void updateVolume(GlusterVolumeEntity existingVolume, GlusterVolumeEntity fetchedVolume) {
        updateVolumeProperties(existingVolume, fetchedVolume);
        updateBricks(existingVolume, fetchedVolume);
        updateOptions(existingVolume, fetchedVolume);
        updateTransportTypes(existingVolume, fetchedVolume);
    }

    private void updateTransportTypes(GlusterVolumeEntity existingVolume, GlusterVolumeEntity fetchedVolume) {
        Set<TransportType> existingTransportTypes = existingVolume.getTransportTypes();
        Set<TransportType> fetchedTransportTypes = fetchedVolume.getTransportTypes();
        if (ListUtils.listsEqual(existingTransportTypes, fetchedTransportTypes)) {
            // transport types not changed. return without updating DB.
            return;
        }

        Collection<TransportType> addedTransportTypes =
                ListUtils.getAddedElements(existingTransportTypes, fetchedTransportTypes);
        if (!addedTransportTypes.isEmpty()) {
            log.info("Adding transport type(s) '{}' to volume '{}'",
                    addedTransportTypes,
                    existingVolume.getName());
            getVolumeDao().addTransportTypes(existingVolume.getId(), addedTransportTypes);
        }

        Collection<TransportType> removedTransportTypes =
                ListUtils.getAddedElements(fetchedTransportTypes, existingTransportTypes);
        if (!removedTransportTypes.isEmpty()) {
            log.info("Removing transport type(s) '{}' from volume '{}'",
                    removedTransportTypes,
                    existingVolume.getName());
            getVolumeDao().removeTransportTypes(existingVolume.getId(), removedTransportTypes);
        }
    }

    private void updateBricks(GlusterVolumeEntity existingVolume, GlusterVolumeEntity fetchedVolume) {
        List<GlusterBrickEntity> fetchedBricks = fetchedVolume.getBricks();
        if (fetchedBricks == null) {
            log.warn("Bricks of volume '{}' were not fetched. " +
                    "Hence will not try to update them in engine at this point.",
                    fetchedVolume.getName());
            return;
        }

        removeDeletedBricks(existingVolume, fetchedBricks);
        updateExistingAndNewBricks(existingVolume, fetchedBricks);
    }

    @SuppressWarnings("serial")
    private void removeDeletedBricks(GlusterVolumeEntity existingVolume, List<GlusterBrickEntity> fetchedBricks) {
        List<Guid> idsToRemove = new ArrayList<Guid>();
        for (final GlusterBrickEntity existingBrick : existingVolume.getBricks()) {
            if (!GlusterCoreUtil.containsBrick(fetchedBricks, existingBrick)) {
                idsToRemove.add(existingBrick.getId());
                log.info("Detected brick '{}' removed from volume '{}'. Removing it from engine DB as well.",
                        existingBrick.getQualifiedName(),
                        existingVolume.getName());
                logUtil.logAuditMessage(existingVolume.getClusterId(), existingVolume, null,
                        AuditLogType.GLUSTER_VOLUME_BRICK_REMOVED_FROM_CLI,
                        new HashMap<String, String>() {
                            {
                                put(GlusterConstants.BRICK, existingBrick.getQualifiedName());
                            }
                        });
            }
        }
        if (!idsToRemove.isEmpty()) {
            try {
                getBrickDao().removeAll(idsToRemove);
            } catch (Exception e) {
                log.error("Error while removing bricks from database: {}", e.getMessage());
                log.debug("Exception", e);
            }
        }
    }

    @SuppressWarnings("serial")
    private void updateExistingAndNewBricks(GlusterVolumeEntity existingVolume, List<GlusterBrickEntity> fetchedBricks) {
        for (final GlusterBrickEntity fetchedBrick : fetchedBricks) {
            GlusterBrickEntity existingBrick = GlusterCoreUtil.findBrick(existingVolume.getBricks(), fetchedBrick);
            if (existingBrick == null) {
                // server id could be null if the new brick resides on a server that is not yet added in the engine
                // adding such servers to engine required manual approval by user, and hence can't be automated.
                if (fetchedBrick.getServerId() != null) {
                    log.info("New brick '{}' added to volume '{}' from gluster CLI. Updating engine DB accordingly.",
                            fetchedBrick.getQualifiedName(),
                            existingVolume.getName());
                    fetchedBrick.setStatus(existingVolume.isOnline() ? GlusterStatus.UP : GlusterStatus.DOWN);
                    getBrickDao().save(fetchedBrick);
                    logUtil.logAuditMessage(existingVolume.getClusterId(), existingVolume, null,
                            AuditLogType.GLUSTER_VOLUME_BRICK_ADDED_FROM_CLI,
                            new HashMap<String, String>() {
                                {
                                    put(GlusterConstants.BRICK, fetchedBrick.getQualifiedName());
                                }
                            });
                }
            } else {
                // brick found. update it if required. Only property that could be different is the brick order
                if (!Objects.equals(existingBrick.getBrickOrder(), fetchedBrick.getBrickOrder())) {
                    log.info("Brick order for brick '{}' changed from '{}' to '{}' because of direct CLI operations. Updating engine DB accordingly.",
                            existingBrick.getQualifiedName(),
                            existingBrick.getBrickOrder(),
                            fetchedBrick.getBrickOrder());
                    getBrickDao().updateBrickOrder(existingBrick.getId(), fetchedBrick.getBrickOrder());
                }
                // update network id, if different
                if (!Objects.equals(existingBrick.getNetworkId(), fetchedBrick.getNetworkId())) {
                    log.info("Network address for brick '{}' detected as  '{}'. Updating engine DB accordingly.",
                            existingBrick.getQualifiedName(),
                            fetchedBrick.getNetworkAddress());
                    getBrickDao().updateBrickNetworkId(existingBrick.getId(), fetchedBrick.getNetworkId());
                }
            }
        }
    }

    private void updateOptions(GlusterVolumeEntity existingVolume, GlusterVolumeEntity fetchedVolume) {
        Collection<GlusterVolumeOptionEntity> existingOptions = existingVolume.getOptions();
        Collection<GlusterVolumeOptionEntity> fetchedOptions = fetchedVolume.getOptions();

        updateExistingAndNewOptions(existingVolume, fetchedOptions);
        removeDeletedOptions(fetchedVolume, existingOptions);
    }

    @SuppressWarnings("serial")
    private void removeDeletedOptions(GlusterVolumeEntity fetchedVolume,
            Collection<GlusterVolumeOptionEntity> existingOptions) {
        List<Guid> idsToRemove = new ArrayList<Guid>();
        for (final GlusterVolumeOptionEntity existingOption : existingOptions) {
            if (fetchedVolume.getOption(existingOption.getKey()) == null) {
                idsToRemove.add(existingOption.getId());
                log.info("Detected option '{}' reset on volume '{}'. Removing it from engine DB as well.",
                        existingOption.getKey(),
                        fetchedVolume.getName());
                // The option "group" gets implicitly replaced with a set of options defined in the group file
                // Hence it is not required to log it as a removed option, as that would be misleading.
                if (!GlusterConstants.OPTION_GROUP.equals(existingOption.getKey())) {
                    logUtil.logAuditMessage(fetchedVolume.getClusterId(), fetchedVolume, null,
                            AuditLogType.GLUSTER_VOLUME_OPTION_RESET_FROM_CLI,
                            new HashMap<String, String>() {
                                {
                                    put(GlusterConstants.OPTION_KEY, existingOption.getKey());
                                    put(GlusterConstants.OPTION_VALUE, existingOption.getValue());
                                }
                            });
                }
            }
        }
        if (!idsToRemove.isEmpty()) {
            try {
                getOptionDao().removeAll(idsToRemove);
            } catch (Exception e) {
                log.error("Error while removing options of volume '{}' from database: {}",
                        fetchedVolume.getName(),
                        e.getMessage());
                log.debug("Exception", e);
            }
        }
    }

    private void updateExistingAndNewOptions(final GlusterVolumeEntity existingVolume,
            Collection<GlusterVolumeOptionEntity> fetchedOptions) {

        Map<String, GlusterVolumeOptionEntity> existingOptions = new HashMap<>();
        Map<String, GlusterVolumeOptionEntity> newOptions = new HashMap<>();

        for (final GlusterVolumeOptionEntity fetchedOption : fetchedOptions) {
            final GlusterVolumeOptionEntity existingOption = existingVolume.getOption(fetchedOption.getKey());
            if (existingOption == null) {
                newOptions.put(fetchedOption.getKey(), fetchedOption);
            } else if (!existingOption.getValue().equals(fetchedOption.getValue())) {
                fetchedOption.setId(existingOption.getId());
                existingOptions.put(fetchedOption.getKey(), fetchedOption);
            }
        }

        final List<GlusterVolumeOptionEntity> newOptionsSortedList =
                new ArrayList<GlusterVolumeOptionEntity>(newOptions.values());
        final List<GlusterVolumeOptionEntity> existingOptionsSortedList =
                new ArrayList<GlusterVolumeOptionEntity>(existingOptions.values());
        Collections.sort(newOptionsSortedList);
        Collections.sort(existingOptionsSortedList);

        // Insert the new options in a single transaction
        if (!newOptionsSortedList.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required,
                    new TransactionMethod<Void>() {
                        @Override
                        public Void runInTransaction() {
                            saveNewOptions(existingVolume, newOptionsSortedList);
                            return null;
                        }
                    });
        }

        // Update the existing options in a single transaction
        if (!existingOptionsSortedList.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required,
                    new TransactionMethod<Void>() {
                        @Override
                        public Void runInTransaction() {
                            updateExistingOptions(existingVolume, existingOptionsSortedList);
                            return null;
                        }
                    });
        }
    }

    private void saveNewOptions(GlusterVolumeEntity volume, Collection<GlusterVolumeOptionEntity> entities) {
        getOptionDao().saveAll(entities);
        for (final GlusterVolumeOptionEntity entity : entities) {
            logUtil.logAuditMessage(volume.getClusterId(), volume, null,
                    AuditLogType.GLUSTER_VOLUME_OPTION_SET_FROM_CLI,
                    new HashMap<String, String>() {
                        {
                            put(GlusterConstants.OPTION_KEY, entity.getKey());
                            put(GlusterConstants.OPTION_VALUE, entity.getValue());
                        }
                    });
            log.info("New option '{}'='{}' set on volume '{}' from gluster CLI. Updating engine DB accordingly.",
                    entity.getKey(),
                    entity.getValue(),
                    volume.getName());
        }
    }

    private void updateExistingOptions(final GlusterVolumeEntity volume, Collection<GlusterVolumeOptionEntity> entities) {
        getOptionDao().updateAll("UpdateGlusterVolumeOption", entities);
        for (final GlusterVolumeOptionEntity entity : entities) {
            logUtil.logAuditMessage(volume.getClusterId(), volume, null,
                    AuditLogType.GLUSTER_VOLUME_OPTION_CHANGED_FROM_CLI,
                    new HashMap<String, String>() {
                        {
                            put(GlusterConstants.OPTION_KEY, entity.getKey());
                            put(GlusterConstants.OPTION_OLD_VALUE, volume.getOption(entity.getKey()).getValue());
                            put(GlusterConstants.OPTION_NEW_VALUE, entity.getValue());
                        }
                    });
            log.info("Detected change in value of option '{}' of volume '{}' from '{}' to '{}'. Updating engine DB accordingly.",
                    volume.getOption(entity.getKey()),
                    volume.getName(),
                    volume.getOption(entity.getKey()).getValue(),
                    entity.getValue());
        }
    }

    /**
     * Updates basic properties of the volume. Does not include bricks, options, and transport types
     *
     * @param existingVolume
     *            Volume that is to be updated
     * @param fetchedVolume
     *            Volume fetched from GlusterFS, containing latest properties
     */
    @SuppressWarnings("incomplete-switch")
    public void updateVolumeProperties(GlusterVolumeEntity existingVolume, GlusterVolumeEntity fetchedVolume) {
        boolean changed = false;

        if (existingVolume.getVolumeType() != fetchedVolume.getVolumeType()) {
            existingVolume.setVolumeType(fetchedVolume.getVolumeType());
            changed = true;
        }

        if (existingVolume.getVolumeType().isReplicatedType() &&
                !Objects.equals(existingVolume.getReplicaCount(), fetchedVolume.getReplicaCount())) {
                existingVolume.setReplicaCount(fetchedVolume.getReplicaCount());
                changed = true;
        }

        if (existingVolume.getVolumeType().isStripedType() &&
                !Objects.equals(existingVolume.getStripeCount(), fetchedVolume.getStripeCount())) {
            existingVolume.setStripeCount(fetchedVolume.getStripeCount());
            changed = true;
        }

        if (changed) {
            log.info("Updating volume '{}' with fetched properties.", existingVolume.getName());
            getVolumeDao().updateGlusterVolume(existingVolume);
            logUtil.logVolumeMessage(existingVolume, AuditLogType.GLUSTER_VOLUME_PROPERTIES_CHANGED_FROM_CLI);
        }

        if (existingVolume.getStatus() != fetchedVolume.getStatus()) {
            existingVolume.setStatus(fetchedVolume.getStatus());
            GlusterDBUtils.getInstance().updateVolumeStatus(existingVolume.getId(), fetchedVolume.getStatus());
            logUtil.logVolumeMessage(existingVolume,
                    fetchedVolume.getStatus() == GlusterStatus.UP ? AuditLogType.GLUSTER_VOLUME_STARTED_FROM_CLI
                            : AuditLogType.GLUSTER_VOLUME_STOPPED_FROM_CLI);
        }
    }

    /**
     * Refreshes the brick statuses from GlusterFS. This method is scheduled less frequently as it uses the 'volume
     * status' command, that adds significant overhead on Gluster processes, and hence should not be invoked too
     * frequently.
     */
    @OnTimerMethodAnnotation("refreshHeavyWeightData")
    public void refreshHeavyWeightData() {
        log.debug("Refreshing Gluster Data [heavyweight]");

        for (VDSGroup cluster : getClusterDao().getAll()) {
            if (GlusterFeatureSupported.refreshHeavyWeight(cluster.getCompatibilityVersion())
                    && cluster.supportsGlusterService()) {
                try {
                    refreshClusterHeavyWeightData(cluster);
                } catch (Exception e) {
                    log.error("Error while refreshing Gluster heavyweight data of cluster '{}': {}",
                            cluster.getName(),
                            e.getMessage());
                    log.debug("Exception", e);
                }
            }
        }
    }

    private void refreshClusterHeavyWeightData(VDSGroup cluster) {
        VDS upServer = getClusterUtils().getRandomUpServer(cluster.getId());
        if (upServer == null) {
            log.debug("No server UP in cluster '{}'. Can't refresh it's data at this point.", cluster.getName());
            return;
        }

        for (GlusterVolumeEntity volume : getVolumeDao().getByClusterId(cluster.getId())) {
            log.debug("Refreshing brick statuses for volume '{}' of cluster '{}'",
                    volume.getName(),
                    cluster.getName());
            // brick statuses can be fetched only for started volumes
            if (volume.isOnline()) {
                acquireLock(cluster.getId());
                try {
                    refreshVolumeDetails(upServer, volume);
                } catch (Exception e) {
                    log.error("Error while refreshing brick statuses for volume '{}' of cluster '{}': {}",
                            volume.getName(),
                            cluster.getName(),
                            e.getMessage());
                    log.debug("Exception", e);
                } finally {
                    releaseLock(cluster.getId());
                }
            }
        }
    }

    public void refreshVolumeDetails(VDS upServer, GlusterVolumeEntity volume) {
        List<GlusterBrickEntity> bricksToUpdate = new ArrayList<GlusterBrickEntity>();
        List<GlusterBrickEntity> brickPropertiesToUpdate = new ArrayList<GlusterBrickEntity>();
        List<GlusterBrickEntity> brickPropertiesToAdd = new ArrayList<GlusterBrickEntity>();

        GlusterVolumeAdvancedDetails volumeAdvancedDetails = getVolumeAdvancedDetails(upServer, volume.getClusterId(), volume.getName());

        if (volumeAdvancedDetails.getCapacityInfo() != null) {
            if (volume.getAdvancedDetails().getCapacityInfo() == null) {
                getVolumeDao().addVolumeCapacityInfo(volumeAdvancedDetails.getCapacityInfo());
            } else {
                getVolumeDao().updateVolumeCapacityInfo(volumeAdvancedDetails.getCapacityInfo());
            }
        }

        Map<Guid, BrickProperties> brickPropertiesMap =
                getBrickPropertiesMap(volumeAdvancedDetails);
        for (GlusterBrickEntity brick : volume.getBricks()) {
            BrickProperties brickProperties = brickPropertiesMap.get(brick.getId());
            if (brickProperties != null) {
                if (brickProperties.getStatus() != brick.getStatus()) {
                    logBrickStatusChange(volume, brick, brickProperties.getStatus());
                    brick.setStatus(brickProperties.getStatus());
                    bricksToUpdate.add(brick);
                }
                if (brick.getBrickProperties() == null) {
                    BrickDetails brickDetails = new BrickDetails();
                    brickDetails.setBrickProperties(brickProperties);
                    brick.setBrickDetails(brickDetails);
                    brickPropertiesToAdd.add(brick);
                } else if (brickProperties.getTotalSize() != brick.getBrickProperties().getTotalSize()
                        || brickProperties.getFreeSize() != brick.getBrickProperties().getFreeSize()) {
                    brick.getBrickDetails().setBrickProperties(brickProperties);
                    brickPropertiesToUpdate.add(brick);
                }
            }
        }

        if (!brickPropertiesToAdd.isEmpty()) {
            getBrickDao().addBrickProperties(brickPropertiesToAdd);
        }

        if (!brickPropertiesToUpdate.isEmpty()) {
            getBrickDao().updateBrickProperties(brickPropertiesToUpdate);
        }

        if (!bricksToUpdate.isEmpty()) {
            getBrickDao().updateBrickStatuses(bricksToUpdate);
        }
    }

    private void logBrickStatusChange(GlusterVolumeEntity volume, final GlusterBrickEntity brick, final GlusterStatus fetchedStatus) {
        log.debug("Detected that status of brick '{}' in volume '{}' changed from '{}' to '{}'",
                brick.getQualifiedName(), volume.getName(), brick.getStatus(), fetchedStatus);
        logUtil.logAuditMessage(volume.getClusterId(), volume, null,
                AuditLogType.GLUSTER_BRICK_STATUS_CHANGED,
                new HashMap<String, String>() {
                    {
                        put(GlusterConstants.BRICK_PATH, brick.getQualifiedName());
                        put(GlusterConstants.OPTION_OLD_VALUE, brick.getStatus().toString());
                        put(GlusterConstants.OPTION_NEW_VALUE, fetchedStatus.toString());
                    }
                });
    }

    private Map<Guid, BrickProperties> getBrickPropertiesMap(GlusterVolumeAdvancedDetails volumeDetails) {
        Map<Guid, BrickProperties> brickStatusMap = new HashMap<Guid, BrickProperties>();
        for (BrickDetails brickDetails : volumeDetails.getBrickDetails()) {
            if (brickDetails.getBrickProperties().getBrickId() != null) {
                brickStatusMap.put(brickDetails.getBrickProperties().getBrickId(), brickDetails.getBrickProperties());
            }
        }
        return brickStatusMap;
    }

    protected GlusterVolumeAdvancedDetails getVolumeAdvancedDetails(VDS upServer, Guid clusterId, String volumeName) {
        VDSReturnValue result = runVdsCommand(VDSCommandType.GetGlusterVolumeAdvancedDetails,
                new GlusterVolumeAdvancedDetailsVDSParameters(upServer.getId(),
                        clusterId,
                        volumeName,
                        null,
                        false, true));

        return result.getSucceeded() ? (GlusterVolumeAdvancedDetails) result.getReturnValue() : null;
    }

    private void removeVdsStatisticsFromDb(VDS server) {
        getVdsStatisticsDao().remove(server.getId());
    }

    private void removeVdsStaticFromDb(VDS server) {
        getVdsStaticDao().remove(server.getId());
    }

    private void removeVdsDynamicFromDb(VDS server) {
        getVdsDynamicDao().remove(server.getId());
    }

}

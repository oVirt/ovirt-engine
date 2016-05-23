package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.attestationbroker.AttestThread;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.pm.FenceProxyLocator;
import org.ovirt.engine.core.bll.pm.HostFenceActionExecutor;
import org.ovirt.engine.core.bll.storage.StorageHandlingCommandBase;
import org.ovirt.engine.core.bll.storage.pool.StoragePoolStatusHandler;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ConnectHostToStoragePoolServersParameters;
import org.ovirt.engine.core.common.action.HostStoragePoolParametersBase;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.action.gluster.SyncGlusterStorageDevicesParameter;
import org.ovirt.engine.core.common.businessentities.AttestationResultEnum;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.KdumpStatus;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServer;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerInfo;
import org.ovirt.engine.core.common.businessentities.gluster.PeerStatus;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult.Status;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.eventqueue.Event;
import org.ovirt.engine.core.common.eventqueue.EventQueue;
import org.ovirt.engine.core.common.eventqueue.EventResult;
import org.ovirt.engine.core.common.eventqueue.EventType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.MomPolicyVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.common.vdscommands.gluster.AddGlusterServerVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AlertDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.gluster.GlusterServerDao;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.attestation.AttestationService;
import org.ovirt.engine.core.vdsbroker.attestation.AttestationValue;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;

/**
 * Initialize Vds on its loading. For storages: First connect all storage
 * servers to VDS. Second connect Vds to storage Pool.
 *
 * After server initialized - its will be moved to Up status.
 */
@NonTransactiveCommandAttribute
public class InitVdsOnUpCommand extends StorageHandlingCommandBase<HostStoragePoolParametersBase> {
    private boolean fenceSucceeded = false;
    private FenceOperationResult fenceStatusResult;
    private boolean vdsProxyFound;
    private List<StorageDomainStatic> problematicDomains;
    private boolean connectPoolSucceeded;
    private boolean glusterHostUuidFound;
    private boolean glusterPeerListSucceeded;
    private boolean glusterPeerProbeSucceeded;
    private static Integer MAX_RETRIES_GLUSTER_PROBE_STATUS;
    @Inject
    private EventQueue eventQueue;
    @Inject
    private ResourceManager resourceManager;

    public InitVdsOnUpCommand(HostStoragePoolParametersBase parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setVds(parameters.getVds());
    }

    private boolean initTrustedService() {
        List <String> hosts = new ArrayList<>();

        if (AttestThread.isTrustedVds(getVds().getId())) {
            return true;
        }

        hosts.add(getVds().getHostName());
        List<AttestationValue> value = new ArrayList<>();
        try {
            value = AttestationService.getInstance().attestHosts(hosts);
        } catch (Exception e) {
            log.error("Encounter an exception while attesting host's trustworthiness for Host '{}': {}",
                    hosts,
                    e.getMessage());
            log.debug("Exception", e);
        }
        if (value.size() > 0 && value.get(0).getTrustLevel() == AttestationResultEnum.TRUSTED) {
            return true;
        } else {
            setNonOperational(NonOperationalReason.UNTRUSTED, null);
            return false;
        }
    }

    @Override
    protected void executeCommand() {
        Cluster cluster = getCluster();

        boolean initSucceeded = true;

        initHostKdumpDetectionStatus();

        /* Host is UP, re-set the policy controlled power management flag */
        getVds().setPowerManagementControlledByPolicy(true);
        DbFacade.getInstance().getVdsDynamicDao().updateVdsDynamicPowerManagementPolicyFlag(
                getVds().getId(),
                getVds().isPowerManagementControlledByPolicy());

        if (cluster.supportsTrustedService()) {
            initSucceeded = initTrustedService();
        }

        if (initSucceeded && cluster.supportsVirtService()) {
            initSucceeded = initVirtResources();
        }

        if (initSucceeded && cluster.supportsGlusterService()) {
            initSucceeded = initGlusterHost();
        }

        setSucceeded(initSucceeded);

        if (getSucceeded()) {
            AuditLogableBase logable = new AuditLogableBase(getVds().getId());
            logable.addCustomValue("HostStatus", getVds().getStatus().toString());
            auditLogDirector.log(logable, AuditLogType.VDS_DETECTED);
        }
    }

    private boolean initVirtResources() {
        resourceManager.clearLastStatusEventStampsFromVds(getVdsId());
        if (initializeStorage()) {
            processFence();
            processStoragePoolStatus();
            runUpdateMomPolicy(getCluster(), getVds());
            refreshHostDeviceList();
        } else {
            Map<String, String> customLogValues = new HashMap<>();
            customLogValues.put("StoragePoolName", getStoragePoolName());
            if (problematicDomains != null && !problematicDomains.isEmpty()) {
                customLogValues.put("StorageDomainNames",
                        problematicDomains.stream().map(StorageDomainStatic::getName).collect(Collectors.joining(", ")));
            }
            setNonOperational(NonOperationalReason.STORAGE_DOMAIN_UNREACHABLE, customLogValues);
            return false;
        }
        return true;
    }

    private void refreshHostDeviceList() {
        try {
            runInternalAction(VdcActionType.RefreshHostDevices, new VdsActionParameters(getVdsId()));
        } catch (EngineException e) {
            log.error("Could not refresh host devices for host '{}'", getVds().getName());
        }
    }

    private void processFence() {
        vdsProxyFound = new FenceProxyLocator(getVds()).isProxyHostAvailable();
        if (getVds().isPmEnabled() && vdsProxyFound) {
            HostFenceActionExecutor executor = new HostFenceActionExecutor(getVds());
            fenceStatusResult = executor.fence(FenceActionType.STATUS);
            fenceSucceeded = fenceStatusResult.getStatus() == Status.SUCCESS;
        }
    }

    private void processStoragePoolStatus() {
        if (getVds().getSpmStatus() != VdsSpmStatus.None) {
            StoragePool pool = DbFacade.getInstance().getStoragePoolDao().get(getVds().getStoragePoolId());
            if (pool != null && pool.getStatus() == StoragePoolStatus.NotOperational) {
                pool.setStatus(StoragePoolStatus.NonResponsive);
                DbFacade.getInstance().getStoragePoolDao().updateStatus(pool.getId(), pool.getStatus());
                StoragePoolStatusHandler.poolStatusChanged(pool.getId(), pool.getStatus());
            }
        }
    }

    private void setNonOperational(NonOperationalReason reason, Map<String, String> customLogValues) {
        SetNonOperationalVdsParameters tempVar =
                new SetNonOperationalVdsParameters(getVds().getId(), reason, customLogValues);
        runInternalAction(VdcActionType.SetNonOperationalVds,
                tempVar,
                ExecutionHandler.createInternalJobContext(getContext()));
    }

    private boolean initializeStorage() {
        boolean returnValue = false;

        // if no pool or pool is uninitialized or in maintenance mode no need to
        // connect any storage
        if (getStoragePool() == null || StoragePoolStatus.Uninitialized == getStoragePool().getStatus()
                || StoragePoolStatus.Maintenance == getStoragePool().getStatus()) {
            returnValue = true;
            connectPoolSucceeded = true;
        } else {
            ConnectHostToStoragePoolServersParameters params = new ConnectHostToStoragePoolServersParameters(getStoragePool(), getVds());
            runInternalAction(VdcActionType.ConnectHostToStoragePoolServers, params);
            EventResult connectResult = connectHostToPool();
            if (connectResult != null) {
                returnValue = connectResult.isSuccess();
                problematicDomains = (List<StorageDomainStatic>) connectResult.getResultData();
            }
            connectPoolSucceeded = returnValue;
        }
        return returnValue;
    }

    /**
     * The following method should connect host to pool
     * The method will perform a connect storage pool operation,
     * if operation will wail on StoragePoolWrongMaster or StoragePoolMasterNotFound errors
     * we will try to run reconstruct
     */
    private EventResult connectHostToPool() {
        final VDS vds = getVds();
        EventResult result =
                eventQueue.submitEventSync(new Event(getStoragePool().getId(),
                                null,
                                vds.getId(),
                                EventType.VDSCONNECTTOPOOL,
                                "Trying to connect host " + vds.getHostName() + " with id " + vds.getId()
                                        + " to the pool " + getStoragePool().getId()),
                        () -> runConnectHostToPoolEvent(getStoragePool().getId(), vds));
        return result;
    }

    private EventResult runConnectHostToPoolEvent(final Guid storagePoolId, final VDS vds) {
        EventResult result = new EventResult(true, EventType.VDSCONNECTTOPOOL);
        StoragePool storagePool = getStoragePoolDao().get(storagePoolId);
        StorageDomain masterDomain = getStorageDomainDao().getStorageDomains(storagePoolId, StorageDomainType.Master).get(0);
        List<StoragePoolIsoMap> storagePoolIsoMap = getStoragePoolIsoMapDao().getAllForStoragePool(storagePoolId);
        boolean masterDomainInactiveOrUnknown = masterDomain.getStatus() == StorageDomainStatus.Inactive
                || masterDomain.getStatus() == StorageDomainStatus.Unknown;
        VDSError error = null;
        try {
            VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.ConnectStoragePool,
                    new ConnectStoragePoolVDSCommandParameters(
                            vds, storagePool, masterDomain.getId(), storagePoolIsoMap));
            if (!vdsReturnValue.getSucceeded()) {
                error = vdsReturnValue.getVdsError();
            }
        } catch (EngineException e) {
            error = e.getVdsError();
        }

        if (error != null) {
            if (error.getCode() != EngineError.CannotConnectMultiplePools && masterDomainInactiveOrUnknown) {
                log.info("Could not connect host '{}' to pool '{}', as the master domain is in inactive/unknown"
                                + " status - not failing the operation",
                        vds.getName(),
                        storagePool.getName());
            } else {
                log.error("Could not connect host '{}' to pool '{}': {}",
                        vds.getName(),
                        storagePool.getName(),
                        error.getMessage());
                result.setSuccess(false);
            }
        }

        if (result.isSuccess()) {
            Pair<Boolean, List<StorageDomainStatic>> vdsStatsResults = proceedVdsStats(!masterDomainInactiveOrUnknown, storagePool);
            result.setSuccess(vdsStatsResults.getFirst());
            if (!result.isSuccess()) {
                result.setResultData(vdsStatsResults.getSecond());
                auditLogDirector.log(new AuditLogableBase(getVdsId()),
                        AuditLogType.VDS_STORAGE_VDS_STATS_FAILED);
            }
        }
        return result;
    }

    private VDSReturnValue runUpdateMomPolicy(final Cluster cluster, final VDS vds) {
        VDSReturnValue returnValue = new VDSReturnValue();
        try {
            returnValue = runVdsCommand(VDSCommandType.SetMOMPolicyParameters,
                            new MomPolicyVDSParameters(vds,
                                    cluster.isEnableBallooning(),
                                    cluster.isEnableKsm(),
                                    cluster.isKsmMergeAcrossNumaNodes())
                                            );
        } catch (EngineException e) {
            log.error("Could not update MoM policy on host '{}'", vds.getName());
            returnValue.setSucceeded(false);
        }

        return returnValue;
    }

    private Pair<Boolean, List<StorageDomainStatic>> proceedVdsStats(boolean shouldCheckReportedDomains, StoragePool storagePool) {
        Pair<Boolean, List<StorageDomainStatic>> returnValue = new Pair<>(true, null);
        try {
            runVdsCommand(VDSCommandType.GetStats, new VdsIdAndVdsVDSCommandParametersBase(getVds()));
            if (shouldCheckReportedDomains) {
                List<Guid> problematicDomainsIds =
                        IrsBrokerCommand.fetchDomainsReportedAsProblematic(getVds().getDomains(), storagePool);
                for (Guid domainId : problematicDomainsIds) {
                    StorageDomainStatic domainInfo = getStorageDomainStaticDao().get(domainId);
                    log.error("Storage Domain '{}' of pool '{}' is in problem in host '{}'",
                            domainInfo != null ? domainInfo.getStorageName() : domainId,
                            getStoragePool().getName(),
                            getVds().getName());
                    if (domainInfo == null || domainInfo.getStorageDomainType().isDataDomain()) {
                        returnValue.setFirst(false);
                        if (returnValue.getSecond() == null) {
                            returnValue.setSecond(new ArrayList<>());
                        }
                        returnValue.getSecond().add(domainInfo);
                    }
                }
            }
        } catch (EngineException e) {
            log.error("Could not get Host statistics for Host '{}': {}",
                    getVds().getName(),
                    e.getMessage());
            log.debug("Exception", e);
            returnValue.setFirst(false);
        }
        return returnValue;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        AuditLogType type = AuditLogType.UNASSIGNED;

        if (getCluster().supportsVirtService()) {
            if (!connectPoolSucceeded) {
                type = AuditLogType.CONNECT_STORAGE_POOL_FAILED;
            } else if (getVds().isPmEnabled() && fenceSucceeded) {
                type = AuditLogType.VDS_FENCE_STATUS;
            } else if (getVds().isPmEnabled() && !fenceSucceeded) {
                type = AuditLogType.VDS_FENCE_STATUS_FAILED;
            }

            // PM alerts
            AuditLogableBase logable = new AuditLogableBase(getVds().getId());
            if (getVds().isPmEnabled()) {
                if (!vdsProxyFound) {
                    logable.addCustomValue("Reason",
                            auditLogDirector.getMessage(AuditLogType.VDS_ALERT_FENCE_NO_PROXY_HOST));
                    AlertDirector.alert(logable, AuditLogType.VDS_ALERT_FENCE_TEST_FAILED, auditLogDirector);
                } else if (!fenceSucceeded) {
                    logable.addCustomValue("Reason", fenceStatusResult.getMessage());
                    AlertDirector.alert(logable, AuditLogType.VDS_ALERT_FENCE_TEST_FAILED, auditLogDirector);
                }
            } else {
                AlertDirector.alert(logable, AuditLogType.VDS_ALERT_FENCE_IS_NOT_CONFIGURED, auditLogDirector);
            }
        }

        if (type == AuditLogType.UNASSIGNED && getCluster().supportsGlusterService()) {
            if (!glusterHostUuidFound) {
                type = AuditLogType.GLUSTER_HOST_UUID_NOT_FOUND;
            } else if (!glusterPeerListSucceeded) {
                type = AuditLogType.GLUSTER_SERVERS_LIST_FAILED;
            } else if (!glusterPeerProbeSucceeded) {
                type = AuditLogType.GLUSTER_SERVER_ADD_FAILED;
            }
        }

        return type;
    }

    private boolean initGlusterHost() {
        glusterHostUuidFound = true;
        VDSReturnValue returnValue = runVdsCommand(VDSCommandType.GetGlusterHostUUID,
                new VdsIdVDSCommandParametersBase(getVds().getId()));
        if (returnValue.getSucceeded() && returnValue.getReturnValue() != null) {
            Guid addedServerUuid = Guid.createGuidFromString((String) returnValue.getReturnValue());
            if (hostUuidExists(addedServerUuid)) {
                setNonOperational(NonOperationalReason.GLUSTER_HOST_UUID_ALREADY_EXISTS, null);
                return false;
            }
            saveGlusterHostUuid(addedServerUuid);
        }
        else {
            glusterHostUuidFound = false;
            setNonOperational(NonOperationalReason.GLUSTER_HOST_UUID_NOT_FOUND, null);
        }
        refreshGlusterStorageDevices();
        boolean ret = glusterHostUuidFound && initGlusterPeerProcess();
        getDbFacade().getGlusterServerDao().updatePeerStatus(getVds().getId(),
                ret == true ? PeerStatus.CONNECTED : PeerStatus.DISCONNECTED);
        return ret;
    }

    private void refreshGlusterStorageDevices() {
        try{
            runInternalAction(VdcActionType.SyncStorageDevices, new SyncGlusterStorageDevicesParameter(getVds().getId(), true));
        } catch (EngineException e) {
            log.error("Could not refresh storage devices from gluster host '{}'", getVds().getName());
        }
    }

    /**
     *
     * This method executes a "gluster peer probe" to add the newly added host to the cluster - this
     * is done only if there's another UP server in cluster and the host being added is not already
     * part of the UP server's peer list.
     * Also, acquiring a wait lock only during a gluster peer process (wait as there's periodic job that also
     * acquires lock.
     */
    private boolean initGlusterPeerProcess() {
       // If "gluster peer probe" and "gluster peer status" are executed simultaneously, the results
       // are unpredictable. Hence locking the cluster to ensure the sync job does not lead to race
       // condition.
        try (EngineLock lock = GlusterUtil.getInstance().acquireGlusterLockWait(getVds().getClusterId())) {
            glusterPeerListSucceeded = true;
            glusterPeerProbeSucceeded = true;
            Map<String, String> customLogValues = new HashMap<>();
            List<VDS> vdsList = getVdsDao().getAllForClusterWithStatus(getClusterId(), VDSStatus.Up);
            // If the cluster already having Gluster servers, get an up server
            if (vdsList != null && vdsList.size() > 0) {
                VDS upServer = null;
                for (VDS vds : vdsList) {
                    if (!getVdsId().equals(vds.getId())) {
                        upServer = vds;
                        break;
                    }
                }

                // If new server is not part of the existing gluster peers, add into peer group
                if (upServer != null) {
                    List<GlusterServerInfo> glusterServers = getGlusterPeers(upServer.getId());
                    customLogValues.put("Server", upServer.getHostName());
                    if (glusterServers.size() == 0) {
                        customLogValues.put("Command", "gluster peer status");
                        setNonOperational(NonOperationalReason.GLUSTER_COMMAND_FAILED, customLogValues);
                        return false;
                    }
                    else if (!getGlusterUtil().isHostExists(glusterServers, getVds())) {
                        if (!glusterPeerProbe(upServer.getId(), getVds().getHostName())) {
                            customLogValues.put("Command", "gluster peer probe " + getVds().getHostName());
                            setNonOperational(NonOperationalReason.GLUSTER_COMMAND_FAILED, customLogValues);
                            return false;
                        }

                        int retries = 0;
                        while (retries < getMaxRetriesGlusterProbeStatus()) {
                            // though gluster peer probe succeeds, it takes some time for the host to be
                            // listed as a peer. Return success only when the host is acknowledged as peer
                            // from another upServer.
                            VDS newUpServer =  getNewUpServer(upServer);
                            if (newUpServer == null) {
                                //there's no other up server. so there's no issue with peer status results
                                return true;
                            }
                            List<GlusterServerInfo> newGlusterServers = getGlusterPeers(newUpServer.getId());
                            if (!getGlusterUtil().isHostExists(newGlusterServers, getVds())) {
                                log.info("Failed to find host '{}' in gluster peer list from '{}' on attempt {}",
                                        getVds(), newUpServer, ++retries);
                                // if num of attempts done
                                if (retries == getMaxRetriesGlusterProbeStatus()) {
                                    customLogValues.put("Command", "gluster peer status " + getVds().getHostName());
                                    setNonOperational(NonOperationalReason.GLUSTER_COMMAND_FAILED, customLogValues);
                                    return false;
                                }
                                try { //give time for gluster peer probe to propogate to servers.
                                    Thread.sleep(1000);
                                } catch (Exception e) {
                                    log.error(e.getMessage());
                                    break;
                                }
                            } else {
                                return true;
                            }
                        }
                    }
                }
            }
            return true;
        }
    }

    private GlusterUtil getGlusterUtil() {
        return GlusterUtil.getInstance();
    }

    private VDS getNewUpServer(VDS upServer) {
        List<VDS> vdsList = getVdsDao().getAllForClusterWithStatus(getClusterId(), VDSStatus.Up);
        VDS newUpServer = null;
        for (VDS vds : vdsList) {
            if (!getVdsId().equals(vds.getId()) && !upServer.getId().equals(vds.getId())) {
                newUpServer = vds;
                break;
            }
        }
        return newUpServer;
    }

    private boolean hostUuidExists(Guid addedServerUuid) {
        GlusterServerDao glusterServerDao = DbFacade.getInstance().getGlusterServerDao();
        GlusterServer glusterServer = glusterServerDao.getByGlusterServerUuid(addedServerUuid);
        if (glusterServer == null || glusterServer.getId().equals(getVds().getId())) {
            return false;
        }
        return true;
    }

    private void saveGlusterHostUuid(Guid addedServerUuid) {
        GlusterServerDao glusterServerDao = DbFacade.getInstance().getGlusterServerDao();
        GlusterServer glusterServer = glusterServerDao.getByServerId(getVds().getId());
        if (glusterServer == null) {
            glusterServer = new GlusterServer();
            glusterServer.setId(getVds().getId());
            glusterServer.setGlusterServerUuid(addedServerUuid);
            glusterServer.setPeerStatus(PeerStatus.CONNECTED);
            glusterServerDao.save(glusterServer);
        }
    }

    @SuppressWarnings("unchecked")
    private List<GlusterServerInfo> getGlusterPeers(Guid upServerId) {
        List<GlusterServerInfo> glusterServers = new ArrayList<>();
        VDSReturnValue returnValue = runVdsCommand(VDSCommandType.GlusterServersList,
                        new VdsIdVDSCommandParametersBase(upServerId));
        if (!returnValue.getSucceeded()) {
            getReturnValue().getFault().setError(returnValue.getVdsError().getCode());
            getReturnValue().getFault().setMessage(returnValue.getVdsError().getMessage());
            AuditLogableBase logable = new AuditLogableBase(upServerId);
            logable.updateCallStackFromThrowable(returnValue.getExceptionObject());
            auditLogDirector.log(logable, AuditLogType.GLUSTER_SERVERS_LIST_FAILED);
            glusterPeerListSucceeded = false;
        } else {
            glusterServers = (List<GlusterServerInfo>) returnValue.getReturnValue();
        }
        return glusterServers;
    }

    private boolean glusterPeerProbe(Guid upServerId, String newServerName) {
        try {
            VDSReturnValue returnValue = runVdsCommand(VDSCommandType.AddGlusterServer,
                    new AddGlusterServerVDSParameters(upServerId, newServerName));
            if (!returnValue.getSucceeded()) {
                getReturnValue().getFault().setError(returnValue.getVdsError().getCode());
                getReturnValue().getFault().setMessage(returnValue.getVdsError().getMessage());
                AuditLogableBase logable = new AuditLogableBase(getVdsId());
                logable.updateCallStackFromThrowable(returnValue.getExceptionObject());
                auditLogDirector.log(logable, AuditLogType.GLUSTER_SERVER_ADD_FAILED);
                glusterPeerProbeSucceeded = false;
            }
            return returnValue.getSucceeded();
        } catch (Exception e) {
            log.error("Could not peer probe the gluster server '{}': {}",
                    getVds().getHostName(),
                    e.getMessage());
            log.debug("Exception", e);
            glusterPeerProbeSucceeded = false;
            return false;
        }
    }

    private void initHostKdumpDetectionStatus() {
        // host is UP, remove kdump status
        getDbFacade().getVdsKdumpStatusDao().remove(getVdsId());

        if (getVds().isPmEnabled() &&
                getVds().isPmKdumpDetection() &&
                getVds().getKdumpStatus() != KdumpStatus.ENABLED) {
            AuditLogableBase base = new AuditLogableBase();
            base.setVds(getVds());
            auditLogDirector.log(base, AuditLogType.KDUMP_DETECTION_NOT_CONFIGURED_ON_VDS);
        }
    }

    private int getMaxRetriesGlusterProbeStatus() {
        if (MAX_RETRIES_GLUSTER_PROBE_STATUS == null) {
            MAX_RETRIES_GLUSTER_PROBE_STATUS = Config.<Integer> getValue(ConfigValues.GlusterPeerStatusRetries);
        }
        return MAX_RETRIES_GLUSTER_PROBE_STATUS;
    }
}

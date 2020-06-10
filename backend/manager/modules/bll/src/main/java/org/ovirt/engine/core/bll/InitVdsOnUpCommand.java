package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.attestationbroker.AttestThread;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.hostedengine.HostedEngineHelper;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.pm.FenceProxyLocator;
import org.ovirt.engine.core.bll.pm.HostFenceActionExecutor;
import org.ovirt.engine.core.bll.storage.StorageHandlingCommandBase;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ConnectHostToStoragePoolServersParameters;
import org.ovirt.engine.core.common.action.HostStoragePoolParametersBase;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.action.VdsActionParameters;
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
import org.ovirt.engine.core.common.businessentities.VDSDomainsData;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult.Status;
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
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.MessageBundler;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsKdumpStatusDao;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.attestation.AttestationService;
import org.ovirt.engine.core.vdsbroker.attestation.AttestationValue;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsProxy;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsProxyManager;

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
    private boolean haMaintenanceFailed;

    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private EventQueue eventQueue;
    @Inject
    private ResourceManager resourceManager;
    @Inject
    private InitGlusterCommandHelper glusterCommandHelper;
    @Inject
    private IrsProxyManager irsProxyManager;
    @Inject
    private VdsDynamicDao vdsDynamicDao;
    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;
    @Inject
    private VdsKdumpStatusDao vdsKdumpStatusDao;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;
    @Inject
    private HostedEngineHelper hostedEngineHelper;
    @Inject
    private ClusterCpuFlagsManager clusterCpuFlagsManager;

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
            value = AttestationService.INSTANCE.attestHosts(hosts);
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

        if (isHeHostBeingMovedToForeignCluster(getVds())) {
            setNonOperational(NonOperationalReason.HE_HOST_IN_NON_HE_CLUSTER, null);
            logHeHostInForeignClusterAudit(cluster);
            initSucceeded = false;
        } else {
            // make sure the CPU flags are stored in the DB
            vdsDynamicDao.updateCpuFlags(getVds().getId(), getVds().getCpuFlags());
            clusterCpuFlagsManager.updateClusterCpuFlags(getCluster());

            initHostKdumpDetectionStatus();

            /* Host is UP, re-set the policy controlled power management flag */
            getVds().setPowerManagementControlledByPolicy(true);
            vdsDynamicDao.updateVdsDynamicPowerManagementPolicyFlag(
                    getVds().getId(),
                    getVds().isPowerManagementControlledByPolicy());

            if (getVds().getHighlyAvailableIsConfigured()) {
                haMaintenanceFailed = !hostedEngineHelper.updateHaLocalMaintenanceMode(getVds(), false);
            }

            if (cluster.supportsTrustedService()) {
                initSucceeded = initTrustedService();
            }

            if (initSucceeded && cluster.supportsVirtService()) {
                initSucceeded = initVirtResources();
            }

            if (initSucceeded && cluster.supportsGlusterService()) {
                initSucceeded = glusterCommandHelper.initGlusterHost(getVds());
            }
        }

        setSucceeded(initSucceeded);

        if (getSucceeded()) {
            addCustomValue("HostStatus", getVds().getStatus().toString());
            auditLogDirector.log(this, AuditLogType.VDS_DETECTED);
        }
    }

    private void logHeHostInForeignClusterAudit(Cluster cluster) {
        AuditLogable event = new AuditLogableImpl();
        event.setVdsId(getVdsId());
        event.setVdsName(getVdsName());
        event.setClusterId(cluster.getId());
        event.setClusterName(cluster.getName());
        auditLogDirector.log(event, AuditLogType.HOSTED_ENGINE_CONFIGURED_HOST_IN_NON_HE_CLUSTER);
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
            CommandContext ctx = cloneContext();
            ctx.getExecutionContext().setJobRequired(false);
            runInternalAction(ActionType.RefreshHostDevices, new VdsActionParameters(getVdsId()), ctx);
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
            StoragePool pool = storagePoolDao.get(getVds().getStoragePoolId());
            if (pool != null && pool.getStatus() == StoragePoolStatus.NotOperational) {
                pool.setStatus(StoragePoolStatus.NonResponsive);
                storagePoolDao.updateStatus(pool.getId(), pool.getStatus());
                storagePoolStatusHandler.poolStatusChanged(pool.getId(), pool.getStatus());
            }
        }
    }

    private void setNonOperational(NonOperationalReason reason, Map<String, String> customLogValues) {
        SetNonOperationalVdsParameters tempVar =
                new SetNonOperationalVdsParameters(getVds().getId(), reason, customLogValues);
        runInternalAction(ActionType.SetNonOperationalVds,
                tempVar,
                ExecutionHandler.createInternalJobContext(getContext()));
    }

    private boolean isHeHostBeingMovedToForeignCluster(VDS vds) {
        return hostedEngineHelper.isVmManaged() && vds.isHostedEngineDeployed()
                && (!hostedEngineHelper.getClusterId().equals(vds.getClusterId()));
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
            CommandContext ctx = cloneContext();
            ctx.getExecutionContext().setJobRequired(false);
            ActionReturnValue retVal = runInternalAction(ActionType.ConnectHostToStoragePoolServers, params, ctx);
            if (retVal.getSucceeded()) {
                EventResult connectResult = connectHostToPool();
                if (connectResult != null) {
                    returnValue = connectResult.isSuccess();
                    problematicDomains = (List<StorageDomainStatic>) connectResult.getResultData();
                }
                connectPoolSucceeded = returnValue;
            } else {
                connectPoolSucceeded = false;
            }

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
        return eventQueue.submitEventSync(new Event(getStoragePool().getId(),
                        null,
                        vds.getId(),
                        EventType.VDSCONNECTTOPOOL,
                        "Trying to connect host " + vds.getHostName() + " with id " + vds.getId()
                                + " to the pool " + getStoragePool().getId()),
                () -> runConnectHostToPoolEvent(getStoragePool().getId(), vds));
    }

    private EventResult runConnectHostToPoolEvent(final Guid storagePoolId, final VDS vds) {
        EventResult result = new EventResult(true, EventType.VDSCONNECTTOPOOL);
        StoragePool storagePool = storagePoolDao.get(storagePoolId);
        StorageDomain masterDomain = storageDomainDao.getStorageDomains(storagePoolId, StorageDomainType.Master).get(0);
        List<StoragePoolIsoMap> storagePoolIsoMap = storagePoolIsoMapDao.getAllForStoragePool(storagePoolId);
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
                auditLogDirector.log(this,
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
                List<Guid> problematicDomainsIds = fetchDomainsReportedAsProblematic(getVds().getDomains(), storagePool);
                for (Guid domainId : problematicDomainsIds) {
                    StorageDomainStatic domainInfo = storageDomainStaticDao.get(domainId);
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

    public List<Guid> fetchDomainsReportedAsProblematic(List<VDSDomainsData> vdsDomainsData, StoragePool storagePool) {
        IrsProxy proxy = irsProxyManager.getProxy(storagePool.getId());
        if (proxy != null) {
            return proxy.obtainDomainsReportedAsProblematic(vdsDomainsData);
        }
        return Collections.emptyList();
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
            } else if (haMaintenanceFailed) {
                type = AuditLogType.VDS_ACTIVATE_MANUAL_HA;
            }

            // PM alerts
            // Check first if PM is enabled on the cluster level
            if (getVds().isFencingEnabled()) {
                if (getVds().isPmEnabled()) {
                    if (!vdsProxyFound) {
                        this.addCustomValue("Reason",
                                MessageBundler.getMessage(AuditLogType.VDS_ALERT_FENCE_NO_PROXY_HOST));
                        auditLogDirector.log(this, AuditLogType.VDS_ALERT_FENCE_TEST_FAILED);
                    } else if (!fenceSucceeded) {
                        this.addCustomValue("Reason", fenceStatusResult.getMessage());
                        auditLogDirector.log(this, AuditLogType.VDS_ALERT_FENCE_TEST_FAILED);
                    }
                } else {
                    auditLogDirector.log(this, AuditLogType.VDS_ALERT_FENCE_IS_NOT_CONFIGURED);
                }
            }
        }

        return type;
    }

    private void initHostKdumpDetectionStatus() {
        // host is UP, remove kdump status
        vdsKdumpStatusDao.remove(getVdsId());

        if (getVds().isPmEnabled() &&
                getVds().isPmKdumpDetection() &&
                getVds().getKdumpStatus() != KdumpStatus.ENABLED) {
            auditLogDirector.log(this, AuditLogType.KDUMP_DETECTION_NOT_CONFIGURED_ON_VDS);
        }
    }

}

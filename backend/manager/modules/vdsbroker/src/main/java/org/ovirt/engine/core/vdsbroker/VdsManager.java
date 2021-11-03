package org.ovirt.engine.core.vdsbroker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.SELinuxMode;
import org.ovirt.engine.core.common.businessentities.V2VJobInfo;
import org.ovirt.engine.core.common.businessentities.V2VJobInfo.JobStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSDomainsData;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.BrokerCommandCallback;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.SupportedHostFeatureDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.VdsStatisticsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSErrorException;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsProxy;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsProxyManager;
import org.ovirt.engine.core.vdsbroker.kubevirt.PrometheusUrlResolver;
import org.ovirt.engine.core.vdsbroker.monitoring.HostConnectionRefresherInterface;
import org.ovirt.engine.core.vdsbroker.monitoring.HostMonitoring;
import org.ovirt.engine.core.vdsbroker.monitoring.HostMonitoringInterface;
import org.ovirt.engine.core.vdsbroker.monitoring.MonitoringStrategy;
import org.ovirt.engine.core.vdsbroker.monitoring.MonitoringStrategyFactory;
import org.ovirt.engine.core.vdsbroker.monitoring.RefresherFactory;
import org.ovirt.engine.core.vdsbroker.monitoring.VmStatsRefresher;
import org.ovirt.engine.core.vdsbroker.monitoring.kubevirt.KubevirtNodesMonitoring;
import org.ovirt.engine.core.vdsbroker.vdsbroker.HostNetworkTopologyPersister;
import org.ovirt.engine.core.vdsbroker.vdsbroker.IVdsServer;
import org.ovirt.engine.core.vdsbroker.vdsbroker.NullVdsServer;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSNetworkException;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSRecoveringException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VdsManager {
    private static Logger log = LoggerFactory.getLogger(VdsManager.class);
    private static Map<Guid, ScheduledFuture> recoveringJobIdMap = new ConcurrentHashMap<>();

    private final ResourceManager resourceManager;

    @Inject
    private LockManager lockManager;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private MonitoringStrategyFactory monitoringStrategyFactory;

    @Inject
    private RefresherFactory refresherFactory;

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService executor;

    @Inject
    private VdsDao vdsDao;

    @Inject
    private ClusterDao clusterDao;

    @Inject
    private VdsDynamicDao vdsDynamicDao;

    @Inject
    private ProviderDao providerDao;

    @Inject
    private VmDynamicDao vmDynamicDao;

    @Inject
    private VmDao vmDao;

    @Inject
    private VdsStatisticsDao vdsStatisticsDao;

    @Inject
    private VdsNumaNodeDao vdsNumaNodeDao;

    @Inject
    private SupportedHostFeatureDao hostFeatureDao;

    @Inject
    private InterfaceDao interfaceDao;

    @Inject
    private NetworkDao networkDao;

    @Inject
    private HostNetworkTopologyPersister hostNetworkTopologyPersister;

    @Inject
    private Instance<IrsProxyManager> irsProxyManager;

    @Inject
    private PrometheusUrlResolver prometheusUrlResolver;

    private final AtomicInteger unrespondedAttempts;
    private final Guid vdsId;
    private final VdsMonitor vdsMonitor = new VdsMonitor();
    private VDS cachedVds;
    private long lastUpdate;
    private long updateStartTime;
    private long nextMaintenanceAttemptTime;
    private List<ScheduledFuture> registeredJobs;
    private volatile boolean isSetNonOperationalExecuted;
    private MonitoringStrategy monitoringStrategy;
    private EngineLock monitoringLock;
    private volatile boolean initialized;
    private IVdsServer vdsProxy;
    private volatile boolean beforeFirstRefresh = true;
    private volatile HostMonitoringInterface hostMonitoring;
    private volatile boolean monitoringNeeded;
    private Map<Guid, VMStatus> lastVmsList = Collections.emptyMap();
    private final Map<Guid, V2VJobInfo> vmIdToV2VJob = new ConcurrentHashMap<>();
    private VmStatsRefresher vmsRefresher;
    protected AtomicInteger refreshIteration;
    private int autoRestartUnknownVmsIteration;
    private ArrayList<VDSDomainsData> domains;

    private final ReentrantLock autoStartVmsWithLeasesLock;
    protected final int NUMBER_HOST_REFRESHES_BEFORE_SAVE;
    private HostConnectionRefresherInterface hostRefresher;
    private volatile boolean inServerRebootTimeout;

    VdsManager(VDS vds, ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        NUMBER_HOST_REFRESHES_BEFORE_SAVE = Config.<Integer> getValue(ConfigValues.NumberVmRefreshesBeforeSave);
        refreshIteration = new AtomicInteger(NUMBER_HOST_REFRESHES_BEFORE_SAVE - 1);
        log.info("Entered VdsManager constructor");
        cachedVds = vds;
        vdsId = vds.getId();
        unrespondedAttempts = new AtomicInteger();
        autoStartVmsWithLeasesLock = new ReentrantLock();
    }

    @PostConstruct
    private void init() {
        monitoringStrategy = monitoringStrategyFactory.getMonitoringStrategyForVds(cachedVds);
        monitoringLock = new EngineLock(Collections.singletonMap(vdsId.toString(),
                new Pair<>(LockingGroup.VDS_INIT.name(), "")), null);
        registeredJobs = new ArrayList<>();
        handlePreviousStatus();
        handleSecureSetup();
        initVdsBroker();
    }

    public void handleSecureSetup() {
        // if ssl is on and no certificate file
        if (Config.<Boolean> getValue(ConfigValues.EncryptHostCommunication)
                && !EngineEncryptionUtils.haveKey()) {
            if (cachedVds.getStatus() != VDSStatus.Maintenance && cachedVds.getStatus() != VDSStatus.InstallFailed) {
                setStatus(VDSStatus.NonResponsive, cachedVds);
                updateDynamicData(cachedVds.getDynamicData());
            }
            log.error("Could not find VDC Certificate file.");
            AuditLogable logable = createAuditLogableForHost(cachedVds);
            auditLogDirector.log(logable, AuditLogType.CERTIFICATE_FILE_NOT_FOUND);
        }
    }

    public void handlePreviousStatus() {
        if (cachedVds.getStatus() == VDSStatus.PreparingForMaintenance) {
            cachedVds.setPreviousStatus(cachedVds.getStatus());
        } else {
            cachedVds.setPreviousStatus(VDSStatus.Up);
        }
    }

    public void scheduleJobs() {
        long refreshRate = Config.<Long> getValue(ConfigValues.VdsRefreshRate) * 1000;

        registeredJobs.add(executor.scheduleWithFixedDelay(
                this::refresh,
                refreshRate,
                refreshRate,
                TimeUnit.MILLISECONDS));

        vmsRefresher = getRefresherFactory().createVmStatsRefresher(this, resourceManager);
        vmsRefresher.startMonitoring();

        hostRefresher = getRefresherFactory().createHostConnectionRefresher(this, resourceManager);
        hostRefresher.start();
    }

    private RefresherFactory getRefresherFactory() {
        return refresherFactory;
    }

    private void initVdsBroker() {
        log.info("Initialize vdsBroker '{}:{}'", cachedVds.getHostName(), cachedVds.getPort());
        if (cachedVds.isManaged()) {
            vdsProxy = createVdsServer();
        } else {
            vdsProxy = new NullVdsServer();
        }
    }

    private IVdsServer createVdsServer() {
        // Get the values of the timeouts:
        int clientTimeOut = Config.<Integer> getValue(ConfigValues.vdsTimeout) * 1000;
        int connectionTimeOut = Config.<Integer> getValue(ConfigValues.vdsConnectionTimeout) * 1000;
        int heartbeat = Config.<Integer> getValue(ConfigValues.vdsHeartbeatInSeconds) * 1000;
        int clientRetries = Config.<Integer> getValue(ConfigValues.vdsRetries);
        return TransportFactory.createVdsServer(
                cachedVds.getHostName(),
                cachedVds.getPort(),
                clientTimeOut,
                connectionTimeOut,
                clientRetries,
                heartbeat,
                resourceManager.getExecutor());
    }

    public void refresh() {
        try {
            refreshImpl();
        } catch (Throwable t) {
            log.error("Timer update runtime info failed. Exception: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception:", t);
        }
    }

    public void refreshImpl() {
        boolean releaseLock = true;
        log.debug("Before acquiring monitor lock for scheduled host refresh");
        if (lockManager.acquireLock(monitoringLock).isAcquired()) {
            try {
                setIsSetNonOperationalExecuted(false);
                synchronized (this) {
                    refreshCachedVds();
                    setMonitoringNeeded();
                    if (cachedVds == null) {
                        log.error("VdsManager::refreshVdsRunTimeInfo - onTimer is NULL for {}('{}')",
                                getVdsName(), getVdsId());
                        return;
                    }

                    try {
                        updateIteration();
                        if (isMonitoringNeeded()) {
                            setStartTime();
                            releaseLock = false;
                            log.debug("[{}] About to create/activate host monitoring.", cachedVds.getHostName());
                            hostMonitoring = createHostMonitoring();
                            hostMonitoring.refresh();
                        }
                    } catch (VDSNetworkException e) {
                        logNetworkException(e);
                        releaseLock = true;
                    } catch (VDSRecoveringException ex) {
                        handleVdsRecoveringException(ex);
                        releaseLock = true;
                    } catch (RuntimeException ex) {
                        logFailureMessage(ex);
                        releaseLock = true;
                    }
                }
            } catch (Throwable t) {
                releaseLock = true;
                throw t;
            } finally {
                if (releaseLock) {
                    lockManager.releaseLock(monitoringLock);
                }
            }
        }
    }

    private HostMonitoringInterface createHostMonitoring() {
        switch (cachedVds.getVdsType()) {
        case KubevirtNode:
            return new KubevirtNodesMonitoring(this, providerDao, prometheusUrlResolver);
        default:
            return new HostMonitoring(this,
                    cachedVds,
                    monitoringStrategy,
                    resourceManager,
                    clusterDao,
                    vdsDynamicDao,
                    interfaceDao,
                    vdsNumaNodeDao,
                    networkDao,
                    auditLogDirector);
        }
    }

    public void afterRefreshTreatment(boolean succeeded) {
        final String hostName = cachedVds != null ? cachedVds.getHostName() : "n/a";
        if (!succeeded) {
            log.debug("[{}] Host monitoring refresh not succeeded. Releasing monitoring lock", hostName);
            lockManager.releaseLock(monitoringLock);
            return;
        }

        try {
            Guid storagePoolId = null;
            ArrayList<VDSDomainsData> domainsList = null;
            synchronized (this) {
                unrespondedAttempts.set(0);
                setLastUpdate();

                try {
                    hostMonitoring.afterRefreshTreatment();

                    // Get cachedVds data for updating domains list, ignoring cachedVds which is down, since it's not
                    // connected
                    // to
                    // the storage anymore (so there is no sense in updating the domains list in that case).
                    if (cachedVds != null && cachedVds.getStatus() != VDSStatus.Maintenance) {
                        storagePoolId = cachedVds.getStoragePoolId();
                        domainsList = cachedVds.getDomains();
                    }

                    hostMonitoring = null;


                    log.debug("[{}] Host monitoring completed", hostName);
                } catch (IRSErrorException ex) {
                    logAfterRefreshFailureMessage(ex);
                    if (log.isDebugEnabled()) {
                        logException(ex);
                    }
                } catch (RuntimeException ex) {
                    logAfterRefreshFailureMessage(ex);
                    logException(ex);
                }
            }
            // Now update the status of domains, this code should not be in
            // synchronized part of code
            if (domainsList != null) {
                updateVdsDomainsData(cachedVds, storagePoolId, domainsList);
                setDomains(domainsList);
            }
        } catch (Exception e) {
            log.error("Timer update runtime info failed. Exception: {}", ExceptionUtils.getRootCauseMessage(e));
            log.debug("Exception:", e);
        } finally {
            lockManager.releaseLock(monitoringLock);
        }
    }

    /**
     * process received domain monitoring information from a given vds if necessary (according to it's status
     * and if it's a virtualization node).
     */
    private void updateVdsDomainsData(VDS vds, Guid storagePoolId, ArrayList<VDSDomainsData> vdsDomainData) {
        IrsProxy proxy = irsProxyManager.get().getProxy(storagePoolId);
        if (proxy != null) {
            proxy.updateVdsDomainsData(vds, vdsDomainData);
        }
    }

    private void refreshCachedVds() {
        cachedVds = vdsDao.get(getVdsId());
    }

    /**
     * @return a safe copy of the internal VDS. mutating it must not affect internal.
     */
    public VDS getCopyVds() {
        return cachedVds.clone();
    }

    public VDSStatus getStatus() {
        return cachedVds.getStatus();
    }

    public String getVdsName() {
        return cachedVds.getName();
    }

    public Guid getClusterId() {
        return cachedVds.getClusterId();
    }

    public VDSType getVdsType() {
        return cachedVds.getVdsType();
    }

    private void logFailureMessage(RuntimeException ex) {
        log.warn(
                "Failed to refresh VDS , vds = '{}' : '{}', error = '{}', continuing.",
                cachedVds.getName(),
                cachedVds.getId(),
                ex.getMessage());
        log.error("Exception", ex);
    }

    private void logException(final RuntimeException ex) {
        log.error("ResourceManager::refreshVdsRunTimeInfo {}", ExceptionUtils.getRootCauseMessage(ex));
        log.debug("Exception", ex);
    }

    private void logAfterRefreshFailureMessage(RuntimeException ex) {
        log.warn(
                "Failed to AfterRefreshTreatment VDS, continuing: {}",
                ex.getMessage());
        log.debug("Exception", ex);
    }

    private void setMonitoringNeeded() {
        monitoringNeeded = monitoringStrategy.isMonitoringNeeded(cachedVds) &&
                cachedVds.getStatus().isEligibleForHostMonitoring();

        log.debug("[{}] Setting monitoring needed: {}, cached vds status {}",
                cachedVds.getHostName(),
                monitoringNeeded,
                cachedVds.getStatus());
    }

    public boolean isMonitoringNeeded() {
        return monitoringNeeded;
    }

    private void handleVdsRecoveringException(VDSRecoveringException ex) {
        if (cachedVds.getStatus() != VDSStatus.Initializing && cachedVds.getStatus() != VDSStatus.NonOperational) {
            setStatus(VDSStatus.Initializing, cachedVds);
            vdsDynamicDao.updateStatus(cachedVds.getId(), VDSStatus.Initializing);
            AuditLogable logable = createAuditLogableForHost(cachedVds);
            logable.addCustomValue("ErrorMessage", ex.getMessage());
            logable.updateCallStackFromThrowable(ex);
            auditLogDirector.log(logable, AuditLogType.VDS_INITIALIZING);
            log.warn(
                    "Failed to refresh VDS, continuing, vds='{}'({}): {}",
                    cachedVds.getName(),
                    cachedVds.getId(),
                    ex.getMessage());
            log.debug("Exception", ex);
            final long VDS_RECOVERY_TIMEOUT_IN_MINUTES = Config.<Long>getValue(ConfigValues.VdsRecoveryTimeoutInMinutes);
            ScheduledFuture scheduled = executor.schedule(
                    this::handleVdsRecovering,
                    VDS_RECOVERY_TIMEOUT_IN_MINUTES,
                    TimeUnit.MINUTES);
            recoveringJobIdMap.put(cachedVds.getId(), scheduled);
        }
    }

    public void handleVdsRecovering() {
        recoveringJobIdMap.remove(getVdsId());
        VDS vds = vdsDao.get(getVdsId());
        if (vds.getStatus() == VDSStatus.Initializing) {
            try {
                resourceManager.getEventListener().vdsNonOperational(vds.getId(),
                        NonOperationalReason.TIMEOUT_RECOVERING_FROM_CRASH,
                        true,
                        Guid.Empty);
                setIsSetNonOperationalExecuted(true);
            } catch (RuntimeException exp) {
                log.error(
                            "HandleVdsRecoveringException::Error in recovery timer treatment, vds='{}'({}): {}",
                            vds.getName(),
                            vds.getId(),
                            exp.getMessage());
                log.debug("Exception", exp);
            }
        }
    }

    /**
     * Save dynamic data to cache and DB.
     */
    public void updateDynamicData(VdsDynamic dynamicData) {
        vdsDynamicDao.updateIfNeeded(dynamicData);
        cachedVds.setDynamicData(dynamicData);
    }

    public void updatePartialDynamicData(NonOperationalReason nonOperationalReason, String maintenanceReason) {
        cachedVds.getDynamicData().setNonOperationalReason(nonOperationalReason);
        cachedVds.getDynamicData().setMaintenanceReason(maintenanceReason);
        vdsDynamicDao.updateStatusAndReasons(cachedVds.getDynamicData());
    }

    public void updateUpdateAvailable(boolean updatesAvailable) {
        cachedVds.getDynamicData().setUpdateAvailable(updatesAvailable);
        vdsDynamicDao.updateUpdateAvailable(cachedVds.getId(), updatesAvailable);
    }

    /**
     * Save statistics data to cache and DB.
     */
    public void updateStatisticsData(VdsStatistics statisticsData) {
        vdsStatisticsDao.update(statisticsData);
        cachedVds.setStatisticsData(statisticsData);
    }

    /**
     * Publish the current pending resource summary. This method also refreshes the committed
     * memory for the host to make the operation atomic.
     *
     * This method assumes that the current state of all VMs is properly saved to database before
     * the recomputation is attempted.
     *
     * @param pendingMemory - scheduled memory in MiB
     * @param pendingCpuCount - scheduled number of CPUs
     */
    public void updatePendingData(int pendingMemory, int pendingCpuCount) {
        synchronized (this) {
            cachedVds.setPendingVcpusCount(pendingCpuCount);
            cachedVds.setPendingVmemSize(pendingMemory);
            List<VmDynamic> vmsOnVds = getVmDynamicDao().getAllRunningForVds(getVdsId());
            Map<Guid, VMStatus> vmIdToStatus = vmsOnVds.stream().collect(Collectors.toMap(VmDynamic::getId, VmDynamic::getStatus));
            HostMonitoring.refreshCommitedMemory(cachedVds, vmIdToStatus, resourceManager);
            updateDynamicData(cachedVds.getDynamicData());
        }
    }

    /**
     * Save or update numa data to DB
     */
    public void updateNumaData(final VDS vds) {
        if (vds.getNumaNodeList() == null || vds.getNumaNodeList().isEmpty()) {
            return;
        }

        final List<VdsNumaNode> numaNodesToSave = new ArrayList<>();
        final List<VdsNumaNode> numaNodesToUpdate = new ArrayList<>();
        final List<Guid> numaNodesToRemove = new ArrayList<>();

        List<VdsNumaNode> dbVdsNumaNodes = vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(vds.getId());
        for (VdsNumaNode node : vds.getNumaNodeList()) {
            VdsNumaNode searchNode = dbVdsNumaNodes.stream()
                    .filter(n -> n.getIndex() == node.getIndex())
                    .findAny().orElse(null);

            if (searchNode != null) {
                node.setId(searchNode.getId());
                numaNodesToUpdate.add(node);
                dbVdsNumaNodes.remove(searchNode);
            } else {
                node.setId(Guid.newGuid());
                numaNodesToSave.add(node);
            }
        }
        for (VdsNumaNode node : dbVdsNumaNodes) {
            numaNodesToRemove.add(node.getId());
        }

        //The database operation should be in one transaction
        TransactionSupport.executeInScope(TransactionScopeOption.Required,
                () -> {
                    if (!numaNodesToRemove.isEmpty()) {
                        vdsNumaNodeDao.massRemoveNumaNodeByNumaNodeId(numaNodesToRemove);
                    }
                    if (!numaNodesToUpdate.isEmpty()) {
                        vdsNumaNodeDao.massUpdateNumaNode(numaNodesToUpdate);
                    }
                    if (!numaNodesToSave.isEmpty()) {
                        vdsNumaNodeDao.massSaveNumaNode(numaNodesToSave, vds.getId());
                    }
                    return null;
                });
    }

    public void refreshHostSync(VDS vds) {
        VDSReturnValue caps = resourceManager.runVdsCommand(VDSCommandType.GetCapabilities,
                new VdsIdAndVdsVDSCommandParametersBase(vds));
        handleRefreshCapabilitiesResponse(vds, caps);
    }

    private void logRefreshCapabilitiesFailure(Throwable t) {
        log.error("Unable to RefreshCapabilities: {}", ExceptionUtils.getRootCauseMessage(t));
        log.debug("Exception", t);
    }

    private void handleRefreshCapabilitiesResponse(VDS vds, VDSReturnValue caps) {
        try {
            invokeGetHardwareInfo(vds, caps);
            processRefreshCapabilitiesResponse(new AtomicBoolean(), vds, vds.clone(), caps);
        } catch (Throwable t) {
            logRefreshCapabilitiesFailure(t);
            throw t;
        } finally {
            if (vds != null) {
                updateDynamicData(vds.getDynamicData());
                updateNumaData(vds);

                // Update VDS after testing special hardware capabilities
                monitoringStrategy.processHardwareCapabilities(vds);

                // Always check VdsVersion
                resourceManager.getEventListener().handleVdsVersion(vds.getId());

                // Check FIPS compatibility
                resourceManager.getEventListener().handleVdsFips(vds.getId());
            }
        }
    }

    public void invokeGetHardwareInfo(VDS vds, VDSReturnValue caps) {
        if (caps.getSucceeded()) {
            getHardwareInfo(vds);
        }
    }

    public void getHardwareInfo(VDS vds) {
        // Verify version capabilities
        Set<Version> hostVersions = vds.getSupportedClusterVersionsSet();
        Version clusterCompatibility = vds.getClusterCompatibilityVersion();
        // Verify that this VDS also supports the specific cluster level. Otherwise getHardwareInfo
        // API won't exist for the host and an exception will be raised by VDSM.
        if (hostVersions != null && hostVersions.contains(clusterCompatibility)) {
            resourceManager.runVdsCommand(VDSCommandType.GetHardwareInfoAsync,
                    new VdsIdAndVdsVDSCommandParametersBase(vds).withCallback(new HardwareInfoCallback(vds)));
        }
    }

    class HardwareInfoCallback implements BrokerCommandCallback {

        private VDS vds;

        HardwareInfoCallback(VDS vds) {
            this.vds = vds;
        }

        @Override
        public void onResponse(Map<String, Object> response) {
            try {
                VDSReturnValue ret = (VDSReturnValue) response.get("result");
                if (!ret.getSucceeded()) {
                    AuditLogable logable = createAuditLogableForHost(vds);
                    logable.updateCallStackFromThrowable(ret.getExceptionObject());
                    auditLogDirector.log(logable, AuditLogType.VDS_FAILED_TO_GET_HOST_HARDWARE_INFO);
                }
            } catch (Throwable t) {
                onFailure(t);
            }
        }

        @Override
        public void onFailure(Throwable t) {
            log.error("Unable to GetHardwareInfo: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        }
    }

    public void setStatus(VDSStatus status, VDS vds) {
        synchronized (this) {

            // non-responsive event during moving host to maintenance should be ignored
            if (isNetworkExceptionDuringMaintenance(status)) {
                return;
            }

            if (vds == null) {
                vds = vdsDao.get(getVdsId());
            }
            if (vds.getStatus() != status) {
                if (status == VDSStatus.PreparingForMaintenance) {
                    calculateNextMaintenanceAttemptTime();
                }
                vds.setPreviousStatus(vds.getStatus());
                if (this.cachedVds != null) {
                    this.cachedVds.setPreviousStatus(vds.getStatus());
                 }
            }
            // update to new status
            vds.setStatus(status);
            if (this.cachedVds != null) {
                this.cachedVds.setStatus(status);
            }

            if (vds.getPreviousStatus() == VDSStatus.Up && status != VDSStatus.Up) {
                // Don't use the now stale data collected while the host was up
                invalidateVdsCachedData();
            }
            switch (status) {
            case NonOperational:
                if (this.cachedVds != null) {
                    this.cachedVds.setNonOperationalReason(vds.getNonOperationalReason());
                }
                if (vds.getVmCount() > 0) {
                    break;
                }
            case NonResponsive:
            case Down:
            case Maintenance:
                vds.setCpuSys(0.0);
                vds.setCpuUser(0.0);
                vds.setCpuIdle(0.0);
                vds.setCpuLoad(0.0);
                vds.setUsageCpuPercent(0);
                vds.setUsageMemPercent(0);
                vds.setUsageNetworkPercent(0);
                if (this.cachedVds != null) {
                    this.cachedVds.setCpuSys(0.0);
                    this.cachedVds.setCpuUser(0.0);
                    this.cachedVds.setCpuIdle(0.0);
                    this.cachedVds.setCpuLoad(0.0);
                    this.cachedVds.setUsageCpuPercent(0);
                    this.cachedVds.setUsageMemPercent(0);
                    this.cachedVds.setUsageNetworkPercent(0);
                }
                break;
            case Up:
                vds.setInFenceFlow(false);
                break;
            default:
                break;
            }
        }
    }

    private void invalidateVdsCachedData() {
        if (getDomains() != null) {
            log.info("Clearing domains data for host {}", cachedVds.getName());
            setDomains(null);
        }
    }

    private boolean isNetworkExceptionDuringMaintenance(VDSStatus status) {
        return status == VDSStatus.NonResponsive
                    && this.cachedVds != null
                    && this.cachedVds.getStatus() == VDSStatus.Maintenance;
    }

    /**
     */
    public void succeededToRunVm(Guid vmId) {
        unrespondedAttempts.set(0);
        resourceManager.succededToRunVm(vmId, getVdsId());
    }

    public void refreshCapabilities(VDS vds, BrokerCommandCallback callback) {
        log.debug("monitoring: refresh '{}' capabilities", vds);
        resourceManager.runVdsCommand(VDSCommandType.GetCapabilitiesAsync,
                new VdsIdAndVdsVDSCommandParametersBase(vds).withCallback(callback));
    }

    public VDSStatus processRefreshCapabilitiesResponse(AtomicBoolean processHardwareCapsNeeded,
            VDS vds,
            VDS oldVDS,
            VDSReturnValue caps) {
        if (caps.getSucceeded()) {
            // For gluster nodes, SELinux needs to be in enforcing mode,
            // hence warning in case of permissive as well.
            if (vds.getSELinuxEnforceMode() == null || vds.getSELinuxEnforceMode().equals(SELinuxMode.DISABLED)
                    || (vds.getClusterSupportsGlusterService()
                            && vds.getSELinuxEnforceMode().equals(SELinuxMode.PERMISSIVE))) {
                AuditLogable auditLogable = createAuditLogableForHost(vds);
                auditLogable.addCustomValue("Mode",
                        vds.getSELinuxEnforceMode() == null ? "UNKNOWN" : vds.getSELinuxEnforceMode().name());
                auditLogDirector.log(auditLogable, AuditLogType.VDS_NO_SELINUX_ENFORCEMENT);
                if (vds.getSELinuxEnforceMode() != null) {
                    log.warn("Host '{}' is running with SELinux in '{}' mode", vds.getName(), vds.getSELinuxEnforceMode());
                } else {
                    log.warn("Host '{}' does not report SELinux enforcement information.", vds.getName());
                }
            }

            VDSStatus returnStatus = vds.getStatus();
            NonOperationalReason nonOperationalReason =
                    getHostNetworkTopologyPersister().persistAndEnforceNetworkCompliance(vds);

            if (nonOperationalReason != NonOperationalReason.NONE) {
                setIsSetNonOperationalExecuted(true);

                if (returnStatus != VDSStatus.NonOperational) {
                    log.debug(
                            "monitoring: vds '{}' networks do not match its cluster networks, vds will be moved to NonOperational",
                            vds);
                    vds.setStatus(VDSStatus.NonOperational);
                    vds.setNonOperationalReason(nonOperationalReason);
                }
            }

            // We process the software capabilities.
            VDSStatus oldStatus = vds.getStatus();
            if (oldStatus != VDSStatus.Up) {
                // persist to db the host's cpu_flags.
                // TODO this needs to be revisited - either all the logic is in-memory or based on db
                vdsDynamicDao.updateCpuFlags(vds.getId(), vds.getCpuFlags());
                processHostFeaturesReported(vds);
                monitoringStrategy.processHardwareCapabilities(vds);
            }
            monitoringStrategy.processSoftwareCapabilities(vds);

            returnStatus = vds.getStatus();

            if (returnStatus != oldStatus && returnStatus == VDSStatus.NonOperational) {
                setIsSetNonOperationalExecuted(true);
            }

            processHardwareCapsNeeded.set(monitoringStrategy.processHardwareCapabilitiesNeeded(oldVDS, vds));

            return returnStatus;
        } else if (caps.getExceptionObject() != null) {
            throw caps.getExceptionObject();
        } else {
            log.error("refreshCapabilities:GetCapabilitiesVDSCommand failed with no exception!");
            throw new RuntimeException(caps.getExceptionString());
        }
    }

    private AuditLogable createAuditLogableForHost(VDS vds) {
        AuditLogable logable = new AuditLogableImpl();
        logable.setVdsId(vds.getId());
        logable.setVdsName(vds.getName());
        return logable;
    }

    private void processHostFeaturesReported(VDS host) {
        Set<String> supportedHostFeatures = hostFeatureDao.getSupportedHostFeaturesByHostId(host.getId());
        Set<String> featuresReturnedByVdsCaps = new HashSet<>(host.getAdditionalFeatures());
        host.getAdditionalFeatures().removeAll(supportedHostFeatures);
        if (!host.getAdditionalFeatures().isEmpty()) {
            hostFeatureDao.addAllSupportedHostFeature(host.getId(), host.getAdditionalFeatures());
        }
        supportedHostFeatures.removeAll(featuresReturnedByVdsCaps);
        if (!supportedHostFeatures.isEmpty()) {
            hostFeatureDao.removeAllSupportedHostFeature(host.getId(), supportedHostFeatures);
        }
    }

    private HostNetworkTopologyPersister getHostNetworkTopologyPersister() {
        return hostNetworkTopologyPersister;
    }

    private long calcTimeoutToFence(int vmCount, VdsSpmStatus spmStatus) {
        int spmIndicator = spmStatus == VdsSpmStatus.None ? 0 : 1;
        int secToFence = (int) (
                // delay time can be fracture number, casting it to int should be enough
                Config.<Integer> getValue(ConfigValues.TimeoutToResetVdsInSeconds)
                        + Config.<Double> getValue(ConfigValues.DelayResetForSpmInSeconds) * spmIndicator
                        + Config.<Double> getValue(ConfigValues.DelayResetPerVmInSeconds) * vmCount);

        return TimeUnit.SECONDS.toMillis(secToFence);
    }

    /**
     * Handle network exception
     *
     * @param ex exception to handle
     */
    public void handleNetworkException(VDSNetworkException ex) {
        boolean saveToDb = true;
        if (isInServerRebootTimeout()) {
            log.warn("Ignoring communication error for host '{}', because reboot timeout hasn't passed: {}",
                    cachedVds.getHostName(),
                    ex.getMessage());
            log.debug("Exception", ex);
            return;
        }
        if (cachedVds.getStatus() != VDSStatus.Down) {
            unrespondedAttempts.incrementAndGet();
            if (isHostInGracePeriod(false)) {
                if (cachedVds.getStatus() != VDSStatus.Connecting
                        && cachedVds.getStatus() != VDSStatus.PreparingForMaintenance
                        && cachedVds.getStatus() != VDSStatus.NonResponsive) {
                    setStatus(VDSStatus.Connecting, cachedVds);
                    logChangeStatusToConnecting();
                } else {
                    saveToDb = false;
                }
            } else {
                if (cachedVds.getStatus() == VDSStatus.Maintenance) {
                    saveToDb = false;
                } else {
                    List<VM> vmsRunningOnVds = vmDao.getMonitoredVmsRunningByVds(getVdsId());
                    if (cachedVds.getStatus() != VDSStatus.NonResponsive) {
                        setStatus(VDSStatus.NonResponsive, cachedVds);
                        moveVmsToUnknown(vmsRunningOnVds);
                        // we want to try to restart VMs with lease ~20 sec after they switch to unknown
                        int skippedIterationsBeforeFirstTry = Config.<Integer>getValue(ConfigValues.NumberVdsRefreshesBeforeTryToStartUnknownVms);
                        autoRestartUnknownVmsIteration = 0 - skippedIterationsBeforeFirstTry;
                        logHostFailToRespond(ex);
                        resourceManager.getEventListener().vdsNotResponding(cachedVds);
                    } else {
                        saveToDb = false;
                    }
                    restartVmsWithLeaseIfNeeded(vmsRunningOnVds);
                }
            }
        }
        if (saveToDb) {
            updateDynamicData(cachedVds.getDynamicData());
            updateStatisticsData(cachedVds.getStatisticsData());
        }
    }

    private void restartVmsWithLeaseIfNeeded(List<VM> vms) {
        if (vms.isEmpty() || !autoStartVmsWithLeasesLock.tryLock()) {
            return;
        }

        try {
            int skippedIterationsBeforeRetry = Config.<Integer>getValue(ConfigValues.NumberVdsRefreshesBeforeRetryToStartUnknownVms);
            autoRestartUnknownVmsIteration++;
            // we don't want to restart VMs with lease too frequently
            if (autoRestartUnknownVmsIteration >= 0 &&
                    autoRestartUnknownVmsIteration % (skippedIterationsBeforeRetry + 1) == 0) {
                var vmIdsToRestart = vms.stream()
                        .filter(vm -> vm.getLeaseStorageDomainId() != null)
                        .sorted(Comparator.comparing(VM::getPriority).reversed())
                        .map(VM::getId)
                        .collect(Collectors.toList());
                resourceManager.getEventListener().restartVmsWithLease(vmIdsToRestart, getVdsId());
            }
        } finally {
            autoStartVmsWithLeasesLock.unlock();
        }
    }

    /**
     * Checks if host is in grace period from last successful communication to fencing attempt
     *
     * @param sshSoftFencingExecuted
     *            if SSH Soft Fencing was already executed we need to raise default timeout to determine if SSH Soft
     *            Fencing was successful and host became Up
     * @return <code>true</code> if host is still in grace period, otherwise <code>false</code>
     */
    public boolean isHostInGracePeriod(boolean sshSoftFencingExecuted) {
        long timeoutToFence = calcTimeoutToFence(cachedVds.getVmCount(), cachedVds.getSpmStatus());
        int unrespondedAttemptsBarrier = Config.<Integer>getValue(ConfigValues.VDSAttemptsToResetCount);

        if (sshSoftFencingExecuted) {
            // SSH Soft Fencing has already been executed, increase timeout to see if host is OK
            timeoutToFence = timeoutToFence * 2;
            unrespondedAttemptsBarrier = unrespondedAttemptsBarrier * 2;
        }
        // return when either attempts reached or timeout passed, the sooner takes
        if (unrespondedAttempts.get() > unrespondedAttemptsBarrier) {
            // too many unresponded attempts
            return false;
        } else if ((lastUpdate + timeoutToFence) > System.currentTimeMillis()) {
            // timeout since last successful communication attempt passed
            return false;
        }
        return true;
    }

    private void logHostFailToRespond(VDSNetworkException ex) {
        long timeoutToFence = calcTimeoutToFence(cachedVds.getVmCount(), cachedVds.getSpmStatus());
        log.info(
                "Server failed to respond, vds_id='{}', vds_name='{}', vm_count={}, " +
                        "spm_status='{}', non-responsive_timeout (seconds)={}, error: {}",
                cachedVds.getId(), cachedVds.getName(), cachedVds.getVmCount(), cachedVds.getSpmStatus(),
                TimeUnit.MILLISECONDS.toSeconds(timeoutToFence), ex.getMessage());

        AuditLogable logable = createAuditLogableForHost(cachedVds);
        logable.updateCallStackFromThrowable(ex);
        if (ex.getCause() instanceof java.net.UnknownHostException){
            auditLogDirector.log(logable, AuditLogType.VDS_UNKNOWN_HOST);
        } else {
            auditLogDirector.log(logable, AuditLogType.VDS_FAILURE);
        }
    }

    private void logChangeStatusToConnecting() {
        long timeoutToFence = calcTimeoutToFence(cachedVds.getVmCount(), cachedVds.getSpmStatus());
        String msg;
        AuditLogType auditLogType;

        if (cachedVds.isPmEnabled()) {
            msg = "Host '{}' is not responding. It will stay in Connecting state for a grace period " +
                    "of {} seconds and after that an attempt to fence the host will be issued.";
            auditLogType = AuditLogType.VDS_HOST_NOT_RESPONDING_CONNECTING;
            log.warn(msg, cachedVds.getName(), TimeUnit.MILLISECONDS.toSeconds(timeoutToFence));
        } else {
            msg = "Host '{}' is not responding.";
            auditLogType = AuditLogType.VDS_HOST_NOT_RESPONDING;
            log.warn(msg, cachedVds.getName());
        }
        AuditLogable logable = createAuditLogableForHost(cachedVds);
        logable.addCustomValue("Seconds", Long.toString(TimeUnit.MILLISECONDS.toSeconds(timeoutToFence)));
        auditLogDirector.log(logable, auditLogType);
    }

    public void dispose() {
        log.info("vdsManager::disposing");

        for (ScheduledFuture job : registeredJobs) {
            job.cancel(true);
        }

        if (vmsRefresher != null) {
            vmsRefresher.stopMonitoring();
        }

        if (hostRefresher != null) {
            hostRefresher.stop();
        }

        vdsProxy.close();
    }

    /**
     * Log the network exception depending on the VDS status.
     *
     * @param e
     *            The exception to log.
     */
    private void logNetworkException(VDSNetworkException e) {
        switch (cachedVds.getStatus()) {
        case Down:
            break;
        case NonResponsive:
            log.debug(
                    "Failed to refresh VDS, network error, continuing, vds='{}'({}): {}",
                    cachedVds.getName(),
                    cachedVds.getId(),
                    e.getMessage());
            break;
        default:
            log.warn(
                    "Failed to refresh VDS, network error, continuing, vds='{}'({}): {}",
                    cachedVds.getName(),
                    cachedVds.getId(),
                    e.getMessage());
        }
        log.debug("Exception", e);
    }

    public void setIsSetNonOperationalExecuted(boolean isExecuted) {
        this.isSetNonOperationalExecuted = isExecuted;
    }

    public boolean isSetNonOperationalExecuted() {
        return isSetNonOperationalExecuted;
    }

    private void setStartTime() {
        updateStartTime = System.currentTimeMillis();
    }

    /**
     * Return time of last successful host monitoring communication
     */
    public long getLastUpdate() {
        return lastUpdate;
    }

    private void setLastUpdate() {
        lastUpdate = System.currentTimeMillis();
    }

    /**
     * @return elapsed time in milliseconds it took to update the Host run-time info. 0 means the updater never ran.
     */
    public long getLastUpdateElapsed() {
        return lastUpdate - updateStartTime;
    }

    /**
     * @return VdsMonitor a class with means for lock and conditions for signaling
     */
    public VdsMonitor getVdsMonitor() {
        return vdsMonitor;
    }

    public void calculateNextMaintenanceAttemptTime() {
        this.nextMaintenanceAttemptTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(
                Config.<Integer> getValue(ConfigValues.HostPreparingForMaintenanceIdleTime), TimeUnit.SECONDS);
    }

    public boolean isTimeToRetryMaintenance() {
        return System.currentTimeMillis() > nextMaintenanceAttemptTime;
    }

    private void moveVmsToUnknown(List<VM> vms) {
        if (vms.isEmpty()) {
            return;
        }

        List<Guid> vmIds = vms.stream().map(VM::getId).collect(Collectors.toList());
        vmIds.forEach(resourceManager::removeAsyncRunningVm);
        getVmDynamicDao().updateVmsToUnknown(vmIds);

        vmIds.forEach(vmId -> {
            // log VM transition to unknown status
            AuditLogable logable = new AuditLogableImpl();
            logable.setVmId(vmId);
            logable.setVmName(resourceManager.getVmManager(vmId).getName());
            auditLogDirector.log(logable, AuditLogType.VM_SET_TO_UNKNOWN_STATUS);
        });
    }

    private VmDynamicDao getVmDynamicDao() {
        return vmDynamicDao;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean value) {
        initialized = value;
    }

    public IVdsServer getVdsProxy() {
        return vdsProxy;
    }

    public Guid getVdsId() {
        return vdsId;
    }

    public void cancelRecoveryJob() {
        ScheduledFuture scheduled = recoveringJobIdMap.remove(vdsId);
        if (scheduled != null) {
            log.info("Cancelling the recovery from crash timer for VDS '{}' because vds started initializing", vdsId);
            try {
                scheduled.cancel(true);
            } catch (Exception e) {
                log.warn("Failed deleting cancelRecoveryJob: {} for VDS '{}'", e.getMessage(), vdsId);
                log.debug("Exception", e);
            }
        }
    }

    public Version getCompatibilityVersion() {
        return cachedVds.getClusterCompatibilityVersion();
    }

    public String getVdsHostname() {
        return cachedVds.getHostName();
    }

    private void updateIteration() {
        int monitoringIteration = 1;
        if (isTimeToRefreshStatistics()) {
            refreshIteration.set(monitoringIteration);
        } else {
            monitoringIteration = refreshIteration.incrementAndGet();
        }
        log.debug("[{}] Monitoring iteration updated to {}", cachedVds.getHostName(), monitoringIteration);
    }

    public boolean isTimeToRefreshStatistics() {
        final int currentIteration = refreshIteration.get();
        log.debug("[{}] Checking current monitoring iteration: {}", cachedVds.getHostName(), currentIteration);
        return currentIteration == NUMBER_HOST_REFRESHES_BEFORE_SAVE;
    }

    public boolean getbeforeFirstRefresh() {
        return beforeFirstRefresh;
    }

    public void setbeforeFirstRefresh(boolean value) {
        beforeFirstRefresh = value;
    }

    public Map<Guid, VMStatus> getLastVmsList() {
        return lastVmsList;
    }

    public AuditLogDirector getAuditLogDirector() {
        return auditLogDirector;
    }

    /**
     * This method is not thread safe
     */
    public void setLastVmsList(Map<Guid, VMStatus> lastVmsList) {
        this.lastVmsList = lastVmsList;
    }

    public void addVmsToLastVmsList(Map<Guid, VMStatus> additionalVmsList) {
        try {
            this.lastVmsList.putAll(additionalVmsList);
        } catch (UnsupportedOperationException e) {
            // lastVmsList is an emptyMap collection.
            // It can happen if the event happens before polling on engine start.
            this.lastVmsList = additionalVmsList;
        }
    }

    public boolean isInServerRebootTimeout() {
        return inServerRebootTimeout;
    }

    public void setInServerRebootTimeout(boolean inServerRebootTimeout) {
        this.inServerRebootTimeout = inServerRebootTimeout;
    }

    public ArrayList<VDSDomainsData> getDomains() {
        return domains;
    }

    public void setDomains(ArrayList<VDSDomainsData> domains) {
        if (domains != null && this.domains == null) {
            log.info("Received first domain report for host {}", cachedVds.getName());
        }
        this.domains = domains;
    }

    public void vmsMonitoringInitFinished() {
        if (!isInitialized()) {
            log.info("VMs initialization finished for Host: '{}:{}'", cachedVds.getName(), cachedVds.getId());
            resourceManager.handleVmsFinishedInitOnVds(cachedVds.getId());
            setInitialized(true);
        }
    }

    public V2VJobInfo getV2VJobInfoForVm(Guid vmId) {
        return vmIdToV2VJob.get(vmId);
    }

    public V2VJobInfo removeV2VJobInfoForVm(Guid vmId) {
        synchronized (vmIdToV2VJob) {
            return vmIdToV2VJob.remove(vmId);
        }
    }

    public void addV2VJobInfoForVm(Guid vmId, JobStatus jobStatus) {
        vmIdToV2VJob.put(vmId, new V2VJobInfo(vmId, jobStatus));
    }

    /**
     * Update the status for V2V jobs according to the latest reports from VDSM
     * @param v2vJobInfos - jobs we got from VDSM
     */
    public void updateV2VJobInfos(List<V2VJobInfo> v2vJobInfos) {
        // Set the status of jobs that we expect to get from VDSM but
        // didn't arrive in the latest report to non-exist
        for (V2VJobInfo existingJobInfo : vmIdToV2VJob.values()) {
            if (existingJobInfo.isMonitored() && !v2vJobInfos.contains(existingJobInfo)) {
                existingJobInfo.setStatus(JobStatus.NOT_EXIST);
            }
        }

        if (v2vJobInfos.isEmpty()) {
            return;
        }

        // We don't want that by mistake a job that we tried to remove
        // will be added again in case VDSM reports it at the same time
        synchronized (vmIdToV2VJob) {
            for (V2VJobInfo jobInfo : v2vJobInfos) {
                if (vmIdToV2VJob.containsKey(jobInfo.getId())) {
                    vmIdToV2VJob.put(jobInfo.getId(), jobInfo);
                }
            }
        }
    }
}

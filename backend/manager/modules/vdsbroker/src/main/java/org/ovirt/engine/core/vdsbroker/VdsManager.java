package org.ovirt.engine.core.vdsbroker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.SELinuxMode;
import org.ovirt.engine.core.common.businessentities.V2VJobInfo;
import org.ovirt.engine.core.common.businessentities.V2VJobInfo.JobStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSDomainsData;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.SupportedHostFeatureDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.NumaUtils;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManagerFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSErrorException;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.monitoring.HostMonitoring;
import org.ovirt.engine.core.vdsbroker.monitoring.MonitoringStrategy;
import org.ovirt.engine.core.vdsbroker.monitoring.MonitoringStrategyFactory;
import org.ovirt.engine.core.vdsbroker.monitoring.RefresherFactory;
import org.ovirt.engine.core.vdsbroker.monitoring.VmStatsRefresher;
import org.ovirt.engine.core.vdsbroker.vdsbroker.HostNetworkTopologyPersister;
import org.ovirt.engine.core.vdsbroker.vdsbroker.IVdsServer;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSNetworkException;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSRecoveringException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VdsManager {
    private static Logger log = LoggerFactory.getLogger(VdsManager.class);
    private static Map<Guid, String> recoveringJobIdMap = new ConcurrentHashMap<>();
    private final Object lockObj = new Object();
    private final AtomicInteger failedToRunVmAttempts;
    private final AtomicInteger unrespondedAttempts;
    private final Guid vdsId;
    private final VdsMonitor vdsMonitor = new VdsMonitor();
    private VDS cachedVds;
    private final AuditLogDirector auditLogDirector;
    private long lastUpdate;
    private long updateStartTime;
    private long nextMaintenanceAttemptTime;
    private List<String> registeredJobs;
    private boolean isSetNonOperationalExecuted;
    private MonitoringStrategy monitoringStrategy;
    private EngineLock monitoringLock;
    private boolean initialized;
    private IVdsServer vdsProxy;
    private boolean beforeFirstRefresh = true;
    private HostMonitoring hostMonitoring;
    private boolean monitoringNeeded;
    private List<VM> lastVmsList = Collections.emptyList();
    private final ResourceManager resourceManager;
    private final DbFacade dbFacade;
    private Map<Guid, V2VJobInfo> vmIdToV2VJob = new ConcurrentHashMap<>();
    private VmStatsRefresher vmsRefresher;
    protected int refreshIteration;

    protected final int HOST_REFRESH_RATE;
    protected final int NUMBER_HOST_REFRESHES_BEFORE_SAVE;
    private HostConnectionRefresher hostRefresher;

    public VdsManager(VDS vds,
            AuditLogDirector auditLogDirector,
            ResourceManager resourceManager,
            DbFacade dbFacade,
            MonitoringStrategyFactory monitoringStrategyFactory) {
        HOST_REFRESH_RATE = Config.<Integer> getValue(ConfigValues.VdsRefreshRate) * 1000;
        NUMBER_HOST_REFRESHES_BEFORE_SAVE = Config.<Integer> getValue(ConfigValues.NumberVmRefreshesBeforeSave);
        refreshIteration = NUMBER_HOST_REFRESHES_BEFORE_SAVE - 1;
        this.resourceManager = resourceManager;
        this.dbFacade = dbFacade;
        this.auditLogDirector = auditLogDirector;
        log.info("Entered VdsManager constructor");
        cachedVds = vds;
        vdsId = vds.getId();
        monitoringStrategy = monitoringStrategyFactory.getMonitoringStrategyForVds(vds);
        unrespondedAttempts = new AtomicInteger();
        failedToRunVmAttempts = new AtomicInteger();
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
            AuditLogableBase logable = new AuditLogableBase(vdsId);
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
        SchedulerUtil sched = getSchedulUtil();
        int refreshRate = Config.<Integer> getValue(ConfigValues.VdsRefreshRate) * 1000;

        registeredJobs.add(sched.scheduleAFixedDelayJob(
                this,
                "onTimer",
                new Class[0],
                new Object[0],
                refreshRate,
                refreshRate,
                TimeUnit.MILLISECONDS));

        double availableUpdatesRefreshRate = Config.<Double> getValue(ConfigValues.HostPackagesUpdateTimeInHours);
        final int HOURS_TO_MINUTES = 60;
        long rateInMinutes = Math.round(availableUpdatesRefreshRate * HOURS_TO_MINUTES);

        registeredJobs.add(sched.scheduleAFixedDelayJob(
                this,
                "availableUpdates",
                new Class[0],
                new Object[0],
                RandomUtils.nextInt(HOURS_TO_MINUTES) + 1,
                rateInMinutes,
                TimeUnit.MINUTES));

        vmsRefresher = getRefresherFactory().create(this);
        vmsRefresher.startMonitoring();

        hostRefresher = new HostConnectionRefresher(this, resourceManager);
        hostRefresher.start();
    }

    private RefresherFactory getRefresherFactory() {
        return Injector.get(RefresherFactory.class);
    }

    private SchedulerUtil getSchedulUtil() {
        return Injector.get(SchedulerUtilQuartzImpl.class);
    }

    private void initVdsBroker() {
        log.info("Initialize vdsBroker '{}:{}'", cachedVds.getHostName(), cachedVds.getPort());

        // Get the values of the timeouts:
        int clientTimeOut = Config.<Integer> getValue(ConfigValues.vdsTimeout) * 1000;
        int connectionTimeOut = Config.<Integer> getValue(ConfigValues.vdsConnectionTimeout) * 1000;
        int heartbeat = Config.<Integer> getValue(ConfigValues.vdsHeartbeatInSeconds) * 1000;
        int clientRetries = Config.<Integer> getValue(ConfigValues.vdsRetries);
        vdsProxy = TransportFactory.createVdsServer(
                cachedVds.getProtocol(),
                cachedVds.getHostName(),
                cachedVds.getPort(),
                clientTimeOut,
                connectionTimeOut,
                clientRetries,
                heartbeat);
    }

    @OnTimerMethodAnnotation("onTimer")
    public void onTimer() {
        if (LockManagerFactory.getLockManager().acquireLock(monitoringLock).getFirst()) {
            try {
                setIsSetNonOperationalExecuted(false);
                Guid storagePoolId = null;
                ArrayList<VDSDomainsData> domainsList = null;
                synchronized (getLockObj()) {
                    refreshCachedVds();
                    if (cachedVds == null) {
                        log.error("VdsManager::refreshVdsRunTimeInfo - onTimer is NULL for '{}'",
                                getVdsId());
                        return;
                    }

                    try {
                        updateIteration();
                        if (isMonitoringNeeded()) {
                            setStartTime();
                            hostMonitoring =
                                    new HostMonitoring(this,
                                            cachedVds,
                                            monitoringStrategy,
                                            resourceManager,
                                            dbFacade,
                                            auditLogDirector);
                            hostMonitoring.refresh();
                            unrespondedAttempts.set(0);
                            setLastUpdate();
                        }
                    } catch (VDSNetworkException e) {
                        logNetworkException(e);
                    } catch (VDSRecoveringException ex) {
                        handleVdsRecoveringException(ex);
                    } catch (RuntimeException ex) {
                        logFailureMessage(ex);
                    }
                    try {
                        if (hostMonitoring != null) {
                            hostMonitoring.afterRefreshTreatment();

                            // Get cachedVds data for updating domains list, ignoring cachedVds which is down, since it's not
                            // connected
                            // to
                            // the storage anymore (so there is no sense in updating the domains list in that case).
                            if (cachedVds != null && cachedVds.getStatus() != VDSStatus.Maintenance) {
                                storagePoolId = cachedVds.getStoragePoolId();
                                domainsList = cachedVds.getDomains();
                            }
                        }

                        hostMonitoring = null;
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
                    IrsBrokerCommand.updateVdsDomainsData(cachedVds, storagePoolId, domainsList);
                }
            } catch (Exception e) {
                log.error("Timer update runtime info failed. Exception:", e);
            } finally {
                LockManagerFactory.getLockManager().releaseLock(monitoringLock);
            }
        }
    }

    private void refreshCachedVds() {
        cachedVds = dbFacade.getVdsDao().get(getVdsId());
        setMonitoringNeeded();
    }

    @OnTimerMethodAnnotation("availableUpdates")
    public void availableUpdates() {
        if (cachedVds.getStatus() != VDSStatus.Maintenance
                && cachedVds.getStatus() != VDSStatus.Up
                && cachedVds.getStatus() != VDSStatus.NonOperational) {
            log.warn("Check for available updates is skipped for host '{}' due to unsupported host status '{}' ",
                    cachedVds.getName(),
                    cachedVds.getStatus());
            return;
        }

        boolean updateAvailable;
        try {
            updateAvailable = resourceManager.isUpdateAvailable(cachedVds);
        } catch (Exception e) {
            log.error("Failed to check if updates are available for host '{}'", cachedVds.getName());
            AuditLogableBase auditLog = new AuditLogableBase();
            auditLog.setVds(cachedVds);
            auditLog.addCustomValue("Message", StringUtils.defaultString(e.getMessage(), e.getCause().toString()));
            auditLogDirector.log(auditLog, AuditLogType.HOST_AVAILABLE_UPDATES_FAILED);
            return;
        }

        synchronized (getLockObj()) {
            if (updateAvailable != cachedVds.isUpdateAvailable()) {
                cachedVds.getDynamicData().setUpdateAvailable(updateAvailable);
                dbFacade.getVdsDynamicDao().updateUpdateAvailable(cachedVds.getId(), updateAvailable);
            }
        }
    }

    /**
     * @return a safe copy of the internal VDS. mutating it must not affect internal.
     */
    public VDS getCopyVds() {
        return cachedVds.clone();
    }
    public String getVdsName() {
        return cachedVds.getName();
    }

    public Guid getClusterId() {
        return cachedVds.getClusterId();
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
        log.error("ResourceManager::refreshVdsRunTimeInfo", ex);
    }

    private void logAfterRefreshFailureMessage(RuntimeException ex) {
        log.warn(
                "Failed to AfterRefreshTreatment VDS, continuing: {}",
                ex.getMessage());
        log.debug("Exception", ex);
    }

    private void setMonitoringNeeded() {
        monitoringNeeded = monitoringStrategy.isMonitoringNeeded(cachedVds) &&
                cachedVds.getStatus() != VDSStatus.Installing &&
                cachedVds.getStatus() != VDSStatus.InstallFailed &&
                cachedVds.getStatus() != VDSStatus.Reboot &&
                cachedVds.getStatus() != VDSStatus.Maintenance &&
                cachedVds.getStatus() != VDSStatus.PendingApproval &&
                cachedVds.getStatus() != VDSStatus.InstallingOS &&
                cachedVds.getStatus() != VDSStatus.Down &&
                cachedVds.getStatus() != VDSStatus.Kdumping;
    }

    public boolean isMonitoringNeeded() {
        return monitoringNeeded;
    }

    private void handleVdsRecoveringException(VDSRecoveringException ex) {
        if (cachedVds.getStatus() != VDSStatus.Initializing && cachedVds.getStatus() != VDSStatus.NonOperational) {
            setStatus(VDSStatus.Initializing, cachedVds);
            dbFacade.getVdsDynamicDao().updateStatus(cachedVds.getId(), VDSStatus.Initializing);
            AuditLogableBase logable = new AuditLogableBase(cachedVds.getId());
            logable.addCustomValue("ErrorMessage", ex.getMessage());
            logable.updateCallStackFromThrowable(ex);
            auditLogDirector.log(logable, AuditLogType.VDS_INITIALIZING);
            log.warn(
                    "Failed to refresh VDS, continuing, vds='{}'({}): {}",
                    cachedVds.getName(),
                    cachedVds.getId(),
                    ex.getMessage());
            log.debug("Exception", ex);
            final int VDS_RECOVERY_TIMEOUT_IN_MINUTES = Config.<Integer> getValue(ConfigValues.VdsRecoveryTimeoutInMinutes);
            String jobId = getSchedulUtil().scheduleAOneTimeJob(this, "onTimerHandleVdsRecovering", new Class[0],
                    new Object[0], VDS_RECOVERY_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
            recoveringJobIdMap.put(cachedVds.getId(), jobId);
        }
    }

    @OnTimerMethodAnnotation("onTimerHandleVdsRecovering")
    public void onTimerHandleVdsRecovering() {
        recoveringJobIdMap.remove(getVdsId());
        VDS vds = dbFacade.getVdsDao().get(getVdsId());
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
        dbFacade.getVdsDynamicDao().updateIfNeeded(dynamicData);
        cachedVds.setDynamicData(dynamicData);
    }

    public void updatePartialDynamicData(NonOperationalReason nonOperationalReason, String maintenanceReason) {
        cachedVds.getDynamicData().setNonOperationalReason(nonOperationalReason);
        cachedVds.getDynamicData().setMaintenanceReason(maintenanceReason);
        dbFacade.getVdsDynamicDao().updateStatusAndReasons(cachedVds.getDynamicData());
    }

    /**
     * Save statistics data to cache and DB.
     */
    public void updateStatisticsData(VdsStatistics statisticsData) {
        dbFacade.getVdsStatisticsDao().update(statisticsData);
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
        synchronized (getLockObj()) {
            cachedVds.setPendingVcpusCount(pendingCpuCount);
            cachedVds.setPendingVmemSize(pendingMemory);
            HostMonitoring.refreshCommitedMemory(cachedVds, dbFacade.getVmDao().getAllRunningForVds(getVdsId()));
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

        List<VdsNumaNode> dbVdsNumaNodes = dbFacade.getVdsNumaNodeDao().getAllVdsNumaNodeByVdsId(vds.getId());
        for (VdsNumaNode node : vds.getNumaNodeList()) {
            VdsNumaNode searchNode = NumaUtils.getVdsNumaNodeByIndex(dbVdsNumaNodes, node.getIndex());
            if (searchNode != null) {
                node.setId(searchNode.getId());
                numaNodesToUpdate.add(node);
                dbVdsNumaNodes.remove(searchNode);
            }
            else {
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
                        dbFacade.getVdsNumaNodeDao().massRemoveNumaNodeByNumaNodeId(numaNodesToRemove);
                    }
                    if (!numaNodesToUpdate.isEmpty()) {
                        dbFacade.getVdsNumaNodeDao().massUpdateNumaNode(numaNodesToUpdate);
                    }
                    if (!numaNodesToSave.isEmpty()) {
                        dbFacade.getVdsNumaNodeDao().massSaveNumaNode(numaNodesToSave, vds.getId(), null);
                    }
                    return null;
                });
    }

    public void refreshHost(VDS vds) {
        try {
            refreshCapabilities(new AtomicBoolean(), vds);
        } finally {
            if (vds != null) {
                updateDynamicData(vds.getDynamicData());
                updateNumaData(vds);

                // Update VDS after testing special hardware capabilities
                monitoringStrategy.processHardwareCapabilities(vds);

                // Always check VdsVersion
                resourceManager.getEventListener().handleVdsVersion(vds.getId());
            }
        }
    }

    public void refreshHost() {
        refreshHost(cachedVds);
    }

    public void setStatus(VDSStatus status, VDS vds) {
        synchronized (getLockObj()) {

            // non-responsive event during moving host to maintenance should be ignored
            if (isNetworkExceptionDuringMaintenance(status)) {
                return;
            }

            if (vds == null) {
                vds = dbFacade.getVdsDao().get(getVdsId());
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
                vds.setCpuSys(Double.valueOf(0));
                vds.setCpuUser(Double.valueOf(0));
                vds.setCpuIdle(Double.valueOf(0));
                vds.setCpuLoad(Double.valueOf(0));
                vds.setUsageCpuPercent(0);
                vds.setUsageMemPercent(0);
                vds.setUsageNetworkPercent(0);
                if (this.cachedVds != null) {
                    this.cachedVds.setCpuSys(Double.valueOf(0));
                    this.cachedVds.setCpuUser(Double.valueOf(0));
                    this.cachedVds.setCpuIdle(Double.valueOf(0));
                    this.cachedVds.setCpuLoad(Double.valueOf(0));
                    this.cachedVds.setUsageCpuPercent(0);
                    this.cachedVds.setUsageMemPercent(0);
                    this.cachedVds.setUsageNetworkPercent(0);
                }
            default:
                break;
            }
        }
    }

    private boolean isNetworkExceptionDuringMaintenance(VDSStatus status) {
        return status == VDSStatus.NonResponsive
                    && this.cachedVds != null
                    && this.cachedVds.getStatus() == VDSStatus.Maintenance;
    }

    /**
     * This scheduled method allows this cachedVds to recover from
     * Error status.
     */
    @OnTimerMethodAnnotation("recoverFromError")
    public void recoverFromError() {
        VDS vds = dbFacade.getVdsDao().get(getVdsId());

        /**
         * Move cachedVds to Up status from error
         */
        if (vds != null && vds.getStatus() == VDSStatus.Error) {
            setStatus(VDSStatus.Up, vds);
            dbFacade.getVdsDynamicDao().updateStatus(getVdsId(), VDSStatus.Up);
            log.info("Settings host '{}' to up after {} failed attempts to run a VM",
                    vds.getName(),
                    failedToRunVmAttempts);
            failedToRunVmAttempts.set(0);
        }
    }

    /**
     * This callback method notifies this cachedVds that an attempt to run a vm on it
     * failed. above a certain threshold such hosts are marked as
     * VDSStatus.Error.
     */
    public void failedToRunVm(VDS vds) {
        if (failedToRunVmAttempts.get() < Config.<Integer> getValue(ConfigValues.NumberOfFailedRunsOnVds)
                && failedToRunVmAttempts.incrementAndGet() >= Config
                        .<Integer> getValue(ConfigValues.NumberOfFailedRunsOnVds)) {
            //Only one thread at a time can enter here
            resourceManager.runVdsCommand(VDSCommandType.SetVdsStatus,
                    new SetVdsStatusVDSCommandParameters(vds.getId(), VDSStatus.Error));

            SchedulerUtil sched = getSchedulUtil();
            sched.scheduleAOneTimeJob(
                    this,
                    "recoverFromError",
                    new Class[0],
                    new Object[0],
                    Config.<Integer>getValue(ConfigValues.TimeToReduceFailedRunOnVdsInMinutes),
                    TimeUnit.MINUTES);
            auditLogDirector.log(
                    new AuditLogableBase(vds.getId()).addCustomValue(
                            "Time",
                            Config.<Integer> getValue(ConfigValues.TimeToReduceFailedRunOnVdsInMinutes).toString()),
                    AuditLogType.VDS_FAILED_TO_RUN_VMS);
            log.info("Vds '{}' moved to Error mode after {} attempts. Time: {}", vds.getName(),
                    failedToRunVmAttempts, new Date());
        }
    }

    /**
     */
    public void succeededToRunVm(Guid vmId) {
        unrespondedAttempts.set(0);
        resourceManager.succededToRunVm(vmId, getVdsId());
    }

    public VDSStatus refreshCapabilities(AtomicBoolean processHardwareCapsNeeded, VDS vds) {
        log.debug("monitoring: refresh '{}' capabilities", vds);
        VDS oldVDS = vds.clone();
        VDSReturnValue caps =
                resourceManager.runVdsCommand(VDSCommandType.GetCapabilities,
                        new VdsIdAndVdsVDSCommandParametersBase(vds));
        if (caps.getSucceeded()) {
            // Verify version capabilities
            HashSet<Version> hostVersions = null;
            Version clusterCompatibility = vds.getClusterCompatibilityVersion();
            if (// Verify that this VDS also
                // supports the specific cluster level. Otherwise getHardwareInfo API won't exist for the
                // host and an exception will be raised by VDSM.
                (hostVersions = vds.getSupportedClusterVersionsSet()) != null &&
                hostVersions.contains(clusterCompatibility)) {
                VDSReturnValue ret = resourceManager.runVdsCommand(VDSCommandType.GetHardwareInfo,
                        new VdsIdAndVdsVDSCommandParametersBase(vds));
                if (!ret.getSucceeded()) {
                    AuditLogableBase logable = new AuditLogableBase(vds.getId());
                    logable.updateCallStackFromThrowable(ret.getExceptionObject());
                    auditLogDirector.log(logable, AuditLogType.VDS_FAILED_TO_GET_HOST_HARDWARE_INFO);
                }
            }
            // For gluster nodes, SELinux needs to be in enforcing mode,
            // hence warning in case of permissive as well.
            if (vds.getSELinuxEnforceMode() == null || vds.getSELinuxEnforceMode().equals(SELinuxMode.DISABLED)
                    || (vds.getClusterSupportsGlusterService()
                            && vds.getSELinuxEnforceMode().equals(SELinuxMode.PERMISSIVE))) {
                auditLogDirector.log(new AuditLogableBase(vds.getId()).addCustomValue("Mode",
                        vds.getSELinuxEnforceMode() == null ? "UNKNOWN" : vds.getSELinuxEnforceMode().name()),
                        AuditLogType.VDS_NO_SELINUX_ENFORCEMENT);
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
                dbFacade.getVdsDynamicDao().updateCpuFlags(vds.getId(), vds.getCpuFlags());
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

    private void processHostFeaturesReported(VDS host) {
        SupportedHostFeatureDao hostFeatureDao = DbFacade.getInstance().getSupportedHostFeatureDao();
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
        return Injector.get(HostNetworkTopologyPersister.class);
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
        if (cachedVds.getStatus() != VDSStatus.Down) {
            long timeoutToFence = calcTimeoutToFence(cachedVds.getVmCount(), cachedVds.getSpmStatus());
            if (isHostInGracePeriod(false)) {
                if (cachedVds.getStatus() != VDSStatus.Connecting
                        && cachedVds.getStatus() != VDSStatus.PreparingForMaintenance
                        && cachedVds.getStatus() != VDSStatus.NonResponsive) {
                    setStatus(VDSStatus.Connecting, cachedVds);
                    logChangeStatusToConnecting(timeoutToFence);
                } else {
                    saveToDb = false;
                }
                unrespondedAttempts.incrementAndGet();
            } else {
                if (cachedVds.getStatus() == VDSStatus.Maintenance) {
                    saveToDb = false;
                } else {
                    if (cachedVds.getStatus() != VDSStatus.NonResponsive) {
                        setStatus(VDSStatus.NonResponsive, cachedVds);
                        moveVMsToUnknown();
                        logHostFailToRespond(ex, timeoutToFence);
                        resourceManager.getEventListener().vdsNotResponding(cachedVds);
                    } else {
                        setStatus(VDSStatus.NonResponsive, cachedVds);
                    }
                }
            }
        }
        if (saveToDb) {
            updateDynamicData(cachedVds.getDynamicData());
            updateStatisticsData(cachedVds.getStatisticsData());
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

        return unrespondedAttempts.get() < unrespondedAttemptsBarrier
                || (lastUpdate + timeoutToFence) > System.currentTimeMillis();
    }

    private void logHostFailToRespond(VDSNetworkException ex, long timeoutToFence) {
        log.info(
                "Server failed to respond, vds_id='{}', vds_name='{}', vm_count={}, " +
                        "spm_status='{}', non-responsive_timeout (seconds)={}, error: {}",
                cachedVds.getId(), cachedVds.getName(), cachedVds.getVmCount(), cachedVds.getSpmStatus(),
                TimeUnit.MILLISECONDS.toSeconds(timeoutToFence), ex.getMessage());

        AuditLogableBase logable;
        logable = new AuditLogableBase(cachedVds.getId());
        logable.updateCallStackFromThrowable(ex);
        if (ex.getCause() instanceof java.net.UnknownHostException){
            auditLogDirector.log(logable, AuditLogType.VDS_UNKNOWN_HOST);
        } else {
            auditLogDirector.log(logable, AuditLogType.VDS_FAILURE);
        }
    }

    private void logChangeStatusToConnecting(long timeoutToFence) {
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
        AuditLogableBase logable = new AuditLogableBase();
        logable.setVdsId(cachedVds.getId());
        logable.addCustomValue("Seconds", Long.toString(TimeUnit.MILLISECONDS.toSeconds(timeoutToFence)));
        auditLogDirector.log(logable, auditLogType);
    }

    public void dispose() {
        log.info("vdsManager::disposing");
        for (String jobId : registeredJobs) {
            getSchedulUtil().deleteJob(jobId);
        }

        vmsRefresher.stopMonitoring();
        hostRefresher.stop();
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

    private void moveVMsToUnknown() {
        List<VM> vms = getVmsToMoveToUnknown();
        for (VM vm : vms) {
            destroyVmOnDestination(vm);
            resourceManager.removeAsyncRunningVm(vm.getId());
        }

        List<Guid> vmIds = vms.stream().map(VM::getId).collect(Collectors.toList());
        getVmDynamicDao().updateVmsToUnknown(vmIds);

        vmIds.forEach(vmId -> {
            // log VM transition to unknown status
            AuditLogableBase logable = new AuditLogableBase();
            logable.setVmId(vmId);
            auditLogDirector.log(logable, AuditLogType.VM_SET_TO_UNKNOWN_STATUS);
        });
    }

    private VmDynamicDao getVmDynamicDao() {
        return dbFacade.getInstance().getVmDynamicDao();
    }

    private void destroyVmOnDestination(final VM vm) {
        if (vm.getStatus() != VMStatus.MigratingFrom || vm.getMigratingToVds() == null) {
            return;
        }
        // avoid nested locks by doing this in a separate thread
        ThreadPoolUtil.execute(() -> {
            VDSReturnValue returnValue = null;
            returnValue =
                    resourceManager.runVdsCommand(VDSCommandType.DestroyVm,
                            new DestroyVmVDSCommandParameters(vm.getMigratingToVds(), vm.getId(), true, false, 0));
            if (returnValue != null && returnValue.getSucceeded()) {
                log.info("Stopped migrating VM: '{}' on VDS: '{}'", vm.getName(), vm.getMigratingToVds());
            }
            else {
                log.info("Could not stop migrating VM: '{}' on VDS: '{}'", vm.getName(),
                        vm.getMigratingToVds());
            }
        });
    }

    private List<VM> getVmsToMoveToUnknown() {
        List<VM> vmList = dbFacade.getVmDao().getAllRunningForVds(getVdsId());
        List<VM> migratingVms = dbFacade.getVmDao().getAllMigratingToHost(getVdsId());
        for (VM incomingVm : migratingVms) {
            if (incomingVm.getStatus() == VMStatus.MigratingTo) {
                // this VM is finished the migration handover and is running on this host now
                // and should be treated as well.
                vmList.add(incomingVm);
            }
        }
        return vmList;
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
        String jobId = recoveringJobIdMap.remove(vdsId);
        if (jobId != null) {
            log.info("Cancelling the recovery from crash timer for VDS '{}' because vds started initializing", vdsId);
            try {
                Injector.get(SchedulerUtilQuartzImpl.class).deleteJob(jobId);
            } catch (Exception e) {
                log.warn("Failed deleting job '{}' at cancelRecoveryJob: {}", jobId, e.getMessage());
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
        if (refreshIteration == NUMBER_HOST_REFRESHES_BEFORE_SAVE) {
            refreshIteration = 1;
        } else {
            refreshIteration++;
        }
    }

    public boolean isTimeToRefreshStatistics() {
        return refreshIteration == NUMBER_HOST_REFRESHES_BEFORE_SAVE;
    }

    public Object getLockObj() {
        return lockObj;
    }

    public boolean getbeforeFirstRefresh() {
        return beforeFirstRefresh;
    }

    public void setbeforeFirstRefresh(boolean value) {
        beforeFirstRefresh = value;
    }

    public List<VM> getLastVmsList() {
        return lastVmsList;
    }

    /**
     * This method is not thread safe
     */
    public void setLastVmsList(List<VM> lastVmsList) {
        this.lastVmsList = lastVmsList;
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

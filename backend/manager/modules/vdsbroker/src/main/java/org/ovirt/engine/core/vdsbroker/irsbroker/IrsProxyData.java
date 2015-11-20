package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.SpmStatus;
import org.ovirt.engine.core.common.businessentities.SpmStatusResult;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSDomainsData;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.businessentities.VdsSpmIdMap;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.eventqueue.Event;
import org.ovirt.engine.core.common.eventqueue.EventQueue;
import org.ovirt.engine.core.common.eventqueue.EventResult;
import org.ovirt.engine.core.common.eventqueue.EventType;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.DisconnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetStoragePoolInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SpmStartVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SpmStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SpmStopVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManagerFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.TransportFactory;
import org.ovirt.engine.core.vdsbroker.storage.StoragePoolDomainHelper;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSNetworkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IrsProxyData {

    private static final Logger log = LoggerFactory.getLogger(IrsProxyData.class);

    // TODO the syncObj initial purpose was to lock the IrsBroker creation
    // but eventually because the IRS is single threaded and suppose to have
    // quite a load of requests,
    // In order to avoid unexpected behavior we have used the syncObj to
    // lock each request to the IRS
    // and by that we caused a serialization of requests to the IRS.
    // This lock should be removed as soon as the IrsBroker is turned
    // multi threaded
    private static final boolean FAIR_LOCK_TYPE = true;
    private final ReentrantLock syncLock = new ReentrantLock(FAIR_LOCK_TYPE);


    private final String storagePoolRefreshJobId;
    private final String domainRecoverOnHostJobId;
    private final HashSet<Guid> triedVdssList = new HashSet<>();
    private Guid currentVdsId;

    private static Set<VDSStatus> vdsConnectedToPoolStatuses;

    static {
        vdsConnectedToPoolStatuses = EnumSet.copyOf(StoragePoolDomainHelper.vdsDomainsActiveMonitoringStatus);
        vdsConnectedToPoolStatuses.addAll(StoragePoolDomainHelper.vdsDomainsMaintenanceMonitoringStatus);
        vdsConnectedToPoolStatuses.add(VDSStatus.NonResponsive);
        vdsConnectedToPoolStatuses.add(VDSStatus.PreparingForMaintenance);
    }

    private Guid preferredHostId;

    public Guid getCurrentVdsId() {
        return getIrsProxy() != null ? currentVdsId : Guid.Empty;
    }

    public void setCurrentVdsId(Guid value) {
        currentVdsId = Guid.Empty.equals(value) ? null : value;
    }

    private String privatemCurrentIrsHost;
    private IIrsServer irsProxy;
    private Guid fencedIrs;

    private IIrsServer getmIrsProxy() {
        return irsProxy;
    }

    private int privatemIrsPort;

    private int getmIrsPort() {
        return privatemIrsPort;
    }

    private void setmIrsPort(int value) {
        privatemIrsPort = value;
    }

    private VdsProtocol privateProtocol;

    private VdsProtocol getProtocol() {
        return this.privateProtocol;
    }

    private void setProtocol(VdsProtocol value) {
        this.privateProtocol = value;
    }

    public Guid getFencedIrs() {
        return fencedIrs;
    }

    public void setFencedIrs(Guid fencedIrs) {
        this.fencedIrs = fencedIrs;
    }

    private Guid _storagePoolId = Guid.Empty;

    public IrsProxyData(Guid storagePoolId) {
        _storagePoolId = storagePoolId;
        int storagePoolRefreshTime = Config.<Integer> getValue(ConfigValues.StoragePoolRefreshTimeInSeconds);
        storagePoolRefreshJobId = getSchedulUtil().scheduleAFixedDelayJob(this,
                "updatingTimerElapsed", new Class[0], new Object[0], storagePoolRefreshTime,
                storagePoolRefreshTime, TimeUnit.SECONDS);
        domainRecoverOnHostJobId =
                getSchedulUtil().scheduleAFixedDelayJob(this,
                        "hostsStorageConnectionsAndPoolMetadataRefresh",
                        new Class[0],
                        new Object[0],
                        Config.<Integer>getValue(ConfigValues.HostStorageConnectionAndPoolRefreshTimeInSeconds),
                        storagePoolRefreshTime,
                        TimeUnit.SECONDS);
    }

    protected SchedulerUtil getSchedulUtil() {
        return Injector.get(SchedulerUtilQuartzImpl.class);
    }

    private void updateStoragePoolStatus(Guid poolId, StoragePoolStatus status, AuditLogType auditLogType, EngineError error) {
        ResourceManager
                .getInstance()
                .getEventListener()
                .storagePoolStatusChange(poolId, status,
                        auditLogType,
                        error);
    }

    @OnTimerMethodAnnotation("updatingTimerElapsed")
    public void updatingTimerElapsed() {
        runInControlledConcurrency(() -> {
            try {
                if (!_disposed) {
                    StoragePool storagePool = DbFacade.getInstance().getStoragePoolDao()
                            .get(_storagePoolId);

                    if (storagePool != null) {
                        boolean poolStatusDeterminedByHostsStatus = FeatureSupported.dataCenterWithoutSpm(storagePool.getCompatibilityVersion());
                        // when there are no hosts in status up, it means that there shouldn't be domain monitoring
                        // so all the domains need to move to "unknown" status as otherwise their status won't change.
                        if (DbFacade.getInstance()
                                .getVdsDao()
                                .getAllForStoragePoolAndStatuses(_storagePoolId, StoragePoolDomainHelper.vdsDomainsActiveMonitoringStatus)
                                .isEmpty()) {
                            StoragePoolDomainHelper.updateApplicablePoolDomainsStatuses(_storagePoolId,
                                    StoragePoolDomainHelper.storageDomainMonitoredStatus,
                                    StorageDomainStatus.Unknown, "no reporting hosts");
                            // TODO: need to check if it's fine to skip the update when the status is already NonResponsive (as domains status maybe be not updated.
                            if (poolStatusDeterminedByHostsStatus && storagePool.getStatus() != StoragePoolStatus.NonResponsive) {
                                updateStoragePoolStatus(storagePool.getId(), StoragePoolStatus.NonResponsive,
                                        AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_NON_RESPONSIVE_NO_REPORTING_HOSTS,
                                        EngineError.ENGINE);
                            }
                        }  else if (poolStatusDeterminedByHostsStatus && storagePool.getStatus() != StoragePoolStatus.Up) {
                                updateStoragePoolStatus(storagePool.getId(), StoragePoolStatus.Up,
                                        AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_UP_REPORTING_HOSTS,
                                        null);
                        }

                        if (storagePool.getStatus() == StoragePoolStatus.Up ||
                                storagePool.getStatus() == StoragePoolStatus.NonResponsive ||
                                storagePool.getStatus() == StoragePoolStatus.Contend) {
                            if (!poolStatusDeterminedByHostsStatus) {
                                proceedStoragePoolStats(storagePool);
                            } else {
                                List<StorageDomain> storageDomains = DbFacade.getInstance().getStorageDomainDao()
                                        .getAllForStoragePool(storagePool.getId());
                                domainsInMaintenanceCheck(storageDomains, storagePool);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
            }
        });
    }

    public void runInControlledConcurrency(Runnable codeblock) {
        try {
            if (syncLock.isLocked() && !syncLock.isHeldByCurrentThread()) {
                log.debug("Waiting on other task to finish ({} additional threads are queued)",
                        syncLock.getQueueLength());
            }
            syncLock.lock();
            codeblock.run();
        } finally {
            syncLock.unlock();
        }
    }
    private int _errorAttempts;

    private static Collection<Guid> getVdsConnectedToPool(Guid storagePoolId) {
        // Note - this method is used as it returns only hosts from VIRT supported clusters
        // (we use the domain monitoring results only from those clusters hosts).
        // every change to it should be inspected carefully.
        return DbFacade.getInstance().getVdsDao()
                .getAllForStoragePoolAndStatuses(storagePoolId, vdsConnectedToPoolStatuses).stream().map(VDS::getId)
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    private void proceedStoragePoolStats(StoragePool storagePool) {
        // ugly patch because vdsm doesnt check if host is spm on spm
        // operations
        VDSReturnValue result = null;
        Guid curVdsId = currentVdsId;
        if (curVdsId != null) {
            result = ResourceManager.getInstance().runVdsCommand(VDSCommandType.SpmStatus,
                    new SpmStatusVDSCommandParameters(curVdsId, _storagePoolId));
        }

        if (result == null
                || !result.getSucceeded()
                || (result.getSucceeded() && ((SpmStatusResult) result.getReturnValue()).getSpmStatus() != SpmStatus.SPM)) {
            // update pool status to problematic until fence will happen
            if (storagePool.getStatus() != StoragePoolStatus.NonResponsive
                    && storagePool.getStatus() != StoragePoolStatus.NotOperational) {
                if (result != null && result.getVdsError() != null) {
                    updateStoragePoolStatus(_storagePoolId, StoragePoolStatus.NonResponsive,
                                    AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_PROBLEMATIC_WITH_ERROR,
                                    result.getVdsError().getCode());
                } else {
                    updateStoragePoolStatus(_storagePoolId, StoragePoolStatus.NonResponsive,
                                    AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_PROBLEMATIC,
                                    EngineError.ENGINE);
                }
            }

            // if spm status didn't work or not spm and NOT NETWORK
            // PROBLEM
            // then cause failover with attempts
            if (result != null && !(result.getExceptionObject() instanceof VDSNetworkException)) {
                HashMap<Guid, AsyncTaskStatus> tasksList =
                        (HashMap<Guid, AsyncTaskStatus>) ResourceManager
                                .getInstance()
                                .runVdsCommand(VDSCommandType.HSMGetAllTasksStatuses,
                                        new VdsIdVDSCommandParametersBase(curVdsId)).getReturnValue();
                boolean allTasksFinished = true;
                if (tasksList != null) {
                    for (AsyncTaskStatus taskStatus : tasksList.values()) {
                        if (AsyncTaskStatusEnum.finished != taskStatus.getStatus()) {
                            allTasksFinished = false;
                            break;
                        }
                    }
                }
                if ((tasksList == null) || allTasksFinished) {
                    nullifyInternalProxies();
                } else {
                    if (_errorAttempts < Config.<Integer> getValue(ConfigValues.SPMFailOverAttempts)) {
                        _errorAttempts++;
                        log.warn("failed getting spm status for pool '{}' ({}), attempt number: {}",
                                _storagePoolId, storagePool.getName(), _errorAttempts);
                    } else {
                        nullifyInternalProxies();
                        _errorAttempts = 0;
                    }
                }
            }
        } else if (result.getSucceeded()
                && ((SpmStatusResult) result.getReturnValue()).getSpmStatus() == SpmStatus.SPM
                && (storagePool.getStatus() == StoragePoolStatus.NonResponsive || storagePool.getStatus() == StoragePoolStatus.Contend)) {
            // if recovered from network exception set back to up
            DbFacade.getInstance().getStoragePoolDao().updateStatus(storagePool.getId(), StoragePoolStatus.Up);
            storagePool.setStatus(StoragePoolStatus.Up);
            ResourceManager.getInstance().getEventListener()
                    .storagePoolStatusChanged(storagePool.getId(), storagePool.getStatus());
        }
        List<StorageDomain> domainsInDb = DbFacade.getInstance().getStorageDomainDao()
                .getAllForStoragePool(_storagePoolId);
        GetStoragePoolInfoVDSCommandParameters tempVar = new GetStoragePoolInfoVDSCommandParameters(
                _storagePoolId);
        tempVar.setIgnoreFailoverLimit(true);
        VDSReturnValue storagePoolInfoResult = ResourceManager.getInstance().runVdsCommand(
                VDSCommandType.GetStoragePoolInfo, tempVar);
        if (storagePoolInfoResult.getSucceeded()) {
            KeyValuePairCompat<StoragePool, List<StorageDomain>> data =
                    (KeyValuePairCompat<StoragePool, List<StorageDomain>>) storagePoolInfoResult
                            .getReturnValue();
            int masterVersion = data.getKey().getMasterDomainVersion();
            HashSet<Guid> domainsInVds = new HashSet<>();
            for (StorageDomain domainData : data.getValue()) {
                domainsInVds.add(domainData.getId());
                proceedStorageDomain(domainData, masterVersion, storagePool);
            }
            for (final StorageDomain domainInDb : domainsInDb) {
                if (domainInDb.getStorageDomainType() != StorageDomainType.Master
                        && domainInDb.getStatus() != StorageDomainStatus.Locked
                        && !domainInDb.getStorageType().isCinderDomain()
                        && !domainsInVds.contains(domainInDb.getId())) {
                    // domain not attached to pool anymore
                    DbFacade.getInstance()
                            .getStoragePoolIsoMapDao()
                            .remove(new StoragePoolIsoMapId(domainInDb.getId(),
                                    _storagePoolId));
                }

            }
        }

        domainsInMaintenanceCheck(domainsInDb, storagePool);
    }

    private void domainsInMaintenanceCheck(List<StorageDomain> storageDomains, StoragePool pool) {
        for (StorageDomain domainInDb : storageDomains) {
            if (domainInDb.getStatus() == StorageDomainStatus.PreparingForMaintenance) {
                queueDomainMaintenanceCheck(domainInDb, pool);
            }
        }
    }

    public void queueDomainMaintenanceCheck(final StorageDomain domain, final StoragePool pool) {
        getEventQueue()
                .submitEventAsync(new Event(_storagePoolId, domain.getId(), null, EventType.DOMAINFAILOVER, ""),
                        () -> {
                            Collection<Guid> vdsConnectedToPool = getVdsConnectedToPool(_storagePoolId);
                            Set<Guid> vdsDomInMaintenance = _domainsInMaintenance.get(domain.getId());
                            if (vdsConnectedToPool.isEmpty() ||
                                    (vdsDomInMaintenance != null &&
                                            vdsDomInMaintenance.containsAll(vdsConnectedToPool))) {
                                log.info("Moving domain '{}' to maintenance", domain.getId());
                                DbFacade.getInstance().getStoragePoolIsoMapDao().updateStatus(
                                        domain.getStoragePoolIsoMapData().getId(),
                                        StorageDomainStatus.Maintenance);
                                AuditLogableBase auditLogableBase = new AuditLogableBase();
                                auditLogableBase.addCustomValue("StorageDomainName", domain.getName());
                                auditLogableBase.addCustomValue("StoragePoolName", pool.getName());
                                new AuditLogDirector().log(auditLogableBase, AuditLogType.STORAGE_DOMAIN_MOVED_TO_MAINTENANCE);
                            }
                            return null;
                        });
    }

    private EventQueue getEventQueue() {
        return Injector.get(EventQueue.class);
    }

    public Guid getPreferredHostId() {
        return preferredHostId;
    }

    public void setPreferredHostId(Guid preferredHostId) {
        this.preferredHostId = preferredHostId;
    }

    private void proceedStorageDomain(StorageDomain domainFromVdsm, int dataMasterVersion, StoragePool storagePool) {
        StorageDomain storage_domain = DbFacade.getInstance().getStorageDomainDao().getForStoragePool(domainFromVdsm.getId(), _storagePoolId);

        if (storage_domain != null) {
            StorageDomainStatic domainFromDb = storage_domain.getStorageStaticData();
            StoragePoolIsoMap domainPoolMapFromDb = storage_domain.getStoragePoolIsoMapData();
            // If the domain is master in the DB
            if (domainFromDb.getStorageDomainType() == StorageDomainType.Master && domainPoolMapFromDb != null
                    && domainPoolMapFromDb.getStatus() != StorageDomainStatus.Locked) {
                // and the domain is not master in the VDSM
                if (!((domainFromVdsm.getStorageDomainType() == StorageDomainType.Master) || (domainFromVdsm.getStorageDomainType() == StorageDomainType.Unknown))) {
                    reconstructMasterDomainNotInSync(domainFromVdsm.getStoragePoolId(),
                            domainFromDb.getId(),
                            "Mismatch between master in DB and VDSM",
                            MessageFormat.format("Master domain is not in sync between DB and VDSM. "
                                    + "Domain {0} marked as master in DB and not in the storage",
                                    domainFromDb.getStorageName()));
                } // or master in DB and VDSM but there is a version
                  // mismatch
                else if (dataMasterVersion != storagePool.getMasterDomainVersion()) {
                    reconstructMasterDomainNotInSync(domainFromVdsm.getStoragePoolId(),
                            domainFromDb.getId(),
                            "Mismatch between master version in DB and VDSM",
                            MessageFormat.format("Master domain version is not in sync between DB and VDSM. "
                                    + "Domain {0} marked as master, but the version in DB: {1} and in VDSM: {2}",
                                    domainFromDb.getStorageName(),
                                    storagePool.getMasterDomainVersion(),
                                    dataMasterVersion));
                }
            }
            boolean statusChanged = false;
            if (domainPoolMapFromDb == null) {
                domainFromVdsm.setStoragePoolId(_storagePoolId);
                DbFacade.getInstance().getStoragePoolIsoMapDao().save(domainFromVdsm.getStoragePoolIsoMapData());
                statusChanged = true;
            } else if (!domainPoolMapFromDb.getStatus().isStorageDomainInProcess()
                    && domainPoolMapFromDb.getStatus() != domainFromVdsm.getStatus()) {
                if (domainPoolMapFromDb.getStatus() != StorageDomainStatus.Inactive
                        && domainFromVdsm.getStatus() != StorageDomainStatus.Inactive) {
                    DbFacade.getInstance().getStoragePoolIsoMapDao().update(domainFromVdsm.getStoragePoolIsoMapData());
                    statusChanged = true;
                }
                if (domainFromVdsm.getStatus() != null && domainFromVdsm.getStatus() == StorageDomainStatus.Inactive
                        && domainFromDb.getStorageDomainType() == StorageDomainType.Master) {
                    StoragePool pool = DbFacade.getInstance().getStoragePoolDao()
                            .get(domainPoolMapFromDb.getStoragePoolId());
                    if (pool != null) {
                        DbFacade.getInstance().getStoragePoolDao().updateStatus(pool.getId(),
                                StoragePoolStatus.Maintenance);
                        pool.setStatus(StoragePoolStatus.Maintenance);
                        ResourceManager.getInstance().getEventListener()
                                .storagePoolStatusChanged(pool.getId(), StoragePoolStatus.Maintenance);
                    }
                }
            }

            // For block domains, synchronize LUN details comprising the storage domain with the DB
            if (statusChanged && domainFromVdsm.getStatus() == StorageDomainStatus.Active && storage_domain.getStorageType().isBlockDomain()) {
                ResourceManager.getInstance().getEventListener().syncLunsInfoForBlockStorageDomain(
                        domainFromVdsm.getId(), getCurrentVdsId());
            }

            // if status didn't change and still not active no need to
            // update dynamic data
            if (statusChanged
                    || (domainPoolMapFromDb.getStatus() != StorageDomainStatus.Inactive && domainFromVdsm.getStatus() == StorageDomainStatus.Active)) {
                DbFacade.getInstance().getStorageDomainDynamicDao().update(domainFromVdsm.getStorageDynamicData());
                if (domainFromVdsm.getAvailableDiskSize() != null && domainFromVdsm.getUsedDiskSize() != null) {
                    double freePercent = domainFromVdsm.getStorageDynamicData().getfreeDiskPercent();
                    AuditLogType type = AuditLogType.UNASSIGNED;
                    Integer freeDiskInGB = domainFromVdsm.getStorageDynamicData().getAvailableDiskSize();
                    if (freeDiskInGB != null) {
                        if (freePercent < domainFromDb.getWarningLowSpaceIndicator()) {
                            type = AuditLogType.IRS_DISK_SPACE_LOW;
                        }
                        if (freeDiskInGB < domainFromDb.getCriticalSpaceActionBlocker()) {
                        // Note, if both conditions are met, only IRS_DISK_SPACE_LOW_ERROR will be shown
                            type = AuditLogType.IRS_DISK_SPACE_LOW_ERROR;
                        }
                    }

                    if (type != AuditLogType.UNASSIGNED) {
                        AuditLogableBase logable = new AuditLogableBase();
                        logable.setStorageDomain(domainFromVdsm);
                        logable.setStoragePoolId(_storagePoolId);
                        logable.addCustomValue("DiskSpace", domainFromVdsm.getAvailableDiskSize().toString());
                        domainFromVdsm.setStorageName(domainFromDb.getStorageName());
                        new AuditLogDirector().log(logable, type);

                    }
                }

                Set<EngineError> alerts = domainFromVdsm.getAlerts();
                if (alerts != null && !alerts.isEmpty()) {

                    AuditLogableBase logable = new AuditLogableBase();
                    logable.setStorageDomain(domainFromVdsm);
                    domainFromVdsm.setStorageName(domainFromDb.getStorageName());
                    logable.setStoragePoolId(_storagePoolId);

                    for (EngineError alert : alerts) {
                        switch (alert) {
                        case VG_METADATA_CRITICALLY_FULL:
                            new AuditLogDirector().log(logable, AuditLogType.STORAGE_ALERT_VG_METADATA_CRITICALLY_FULL);
                            break;
                        case SMALL_VG_METADATA:
                            new AuditLogDirector().log(logable, AuditLogType.STORAGE_ALERT_SMALL_VG_METADATA);
                            break;
                        default:
                            log.error("Unrecognized alert for domain {}(id = {}): {}",
                                    domainFromVdsm.getStorageName(),
                                    domainFromVdsm.getId(),
                                    alert);
                            break;
                        }
                    }
                }
            }

        } else {
            log.debug("The domain with id '{}' was not found in DB", domainFromVdsm.getId());
        }
    }

    /**
     * Reconstructs the master domain when the old domain is not in sync.
     *
     * @param storagePoolId
     *            The storage pool id.
     * @param masterDomainId
     *            The master domain id.
     * @param exceptionMessage
     *            The message of the exception to throw.
     * @param logMessage
     *            The log message to write in the log.
     */
    private void reconstructMasterDomainNotInSync(final Guid storagePoolId,
            final Guid masterDomainId,
            final String exceptionMessage,
            final String logMessage) {

        getEventQueue().submitEventSync(new Event(_storagePoolId,
                masterDomainId, null, EventType.RECONSTRUCT, "Reconstruct caused by failure to execute spm command"),
                () -> {
                    log.warn(logMessage);

                    AuditLogableBase logable = new AuditLogableBase(currentVdsId);
                    logable.setStorageDomainId(masterDomainId);
                    new AuditLogDirector().log(logable, AuditLogType.SYSTEM_MASTER_DOMAIN_NOT_IN_SYNC);

                    return ResourceManager.getInstance()
                            .getEventListener()
                            .masterDomainNotOperational(masterDomainId, storagePoolId, false, true);

                });
        throw new IRSNoMasterDomainException(exceptionMessage);
    }

    public HashSet<Guid> getTriedVdssList() {
        return triedVdssList;
    }

    public void init(VDS vds) {
        currentVdsId = vds.getId();
        setmIrsPort(vds.getPort());
        privatemCurrentIrsHost = vds.getHostName();
        setProtocol(vds.getProtocol());
    }

    public boolean failover() {
        Guid vdsId = currentVdsId;
        nullifyInternalProxies();
        boolean performFailover = false;
        if (vdsId != null) {
            try {
                VDSReturnValue statusResult = ResourceManager.getInstance().runVdsCommand(VDSCommandType.SpmStatus,
                        new SpmStatusVDSCommandParameters(vdsId, _storagePoolId));
                if (statusResult != null
                        && statusResult.getSucceeded()
                        && (((SpmStatusResult) statusResult.getReturnValue()).getSpmStatus() == SpmStatus.SPM || ((SpmStatusResult) statusResult
                                .getReturnValue()).getSpmStatus() == SpmStatus.Contend)) {
                    performFailover = ResourceManager
                            .getInstance()
                            .runVdsCommand(VDSCommandType.SpmStop,
                                    new SpmStopVDSCommandParameters(vdsId, _storagePoolId)).getSucceeded();
                } else {
                    performFailover = true;
                }
            } catch (Exception ex) {
                // try to failover to another host if failed to get spm
                // status or stop spm
                // (in case currentVdsId has wrong id for some reason)
                log.error("Could not get spm status on host '{}' for spmStop: {}", vdsId, ex.getMessage());
                log.debug("Exception", ex);
                performFailover = true;
            }
        }

        if (performFailover) {
            log.info("Irs placed on server '{}' failed. Proceed Failover", vdsId);
            triedVdssList.add(vdsId);
            return true;
        } else {
            log.error("IRS failover failed - can't allocate vds server");
            return false;
        }
    }

    public IIrsServer getIrsProxy() {
        if (getmIrsProxy() == null) {
            final StoragePool storagePool = DbFacade.getInstance().getStoragePoolDao().get(_storagePoolId);
            // don't try to start spm on uninitialized pool
            if (storagePool.getStatus() != StoragePoolStatus.Uninitialized) {
                String host =
                        TransactionSupport.executeInScope(TransactionScopeOption.Suppress,
                                () -> gethostFromVds());

                if (host != null) {
                    // Get the values of the timeouts:
                    int clientTimeOut = Config.<Integer> getValue(ConfigValues.vdsTimeout) * 1000;
                    int connectionTimeOut = Config.<Integer> getValue(ConfigValues.vdsConnectionTimeout) * 1000;
                    int heartbeat = Config.<Integer> getValue(ConfigValues.vdsHeartbeatInSeconds) * 1000;
                    int clientRetries = Config.<Integer> getValue(ConfigValues.vdsRetries);
                    irsProxy = TransportFactory.createIrsServer(getProtocol(),
                                    host,
                                    getmIrsPort(),
                                    clientTimeOut,
                                    connectionTimeOut,
                                    clientRetries,
                                    heartbeat);
                    runStoragePoolUpEvent(storagePool);
                }
            }
        }
        return getmIrsProxy();
    }

    private void runStoragePoolUpEvent(final StoragePool storagePool) {
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isMasterDomainUp())  {
                        ResourceManager.getInstance()
                                .getEventListener()
                                .storagePoolUpEvent(storagePool);
                    }
                } catch (RuntimeException exp) {
                    log.error("Error in StoragePoolUpEvent: {}", exp.getMessage());
                    log.debug("Exception", exp);
                }

            }

            private boolean isMasterDomainUp() {
                return DbFacade.getInstance().getStorageDomainDao().
                        getStorageDomains(_storagePoolId, StorageDomainType.Master).stream()
                        .anyMatch(d -> d.getStatus() == StorageDomainStatus.Active || d.getStatus() == StorageDomainStatus.Unknown);
            }
        });
    }

    /**
     * Returns True if there are other vdss in pool
     */
    public boolean getHasVdssForSpmSelection() {
        return getPrioritizedVdsInPool().size() > 0;
    }

    private void connectStoragePool(VDS vds, StoragePool storagePool) {
        Guid masterDomainId = DbFacade.getInstance().getStorageDomainDao()
                .getMasterStorageDomainIdForPool(_storagePoolId);
        List<StoragePoolIsoMap> storagePoolIsoMap = DbFacade.getInstance()
                .getStoragePoolIsoMapDao().getAllForStoragePool(_storagePoolId);
        VDSReturnValue connectResult = ResourceManager.getInstance().runVdsCommand(
                VDSCommandType.ConnectStoragePool,
                new ConnectStoragePoolVDSCommandParameters(vds, storagePool,
                        masterDomainId, storagePoolIsoMap));
        if (!connectResult.getSucceeded()
                && connectResult.getExceptionObject() instanceof IRSNoMasterDomainException) {
            throw connectResult.getExceptionObject();
        } else if (!connectResult.getSucceeded()) {
            // if connect to pool fails throw exception for
            // failover
            throw new IRSNonOperationalException("Could not connect host to Data Center(Storage issue)");
        }
    }

    private String gethostFromVds() {
        String returnValue = null;
        Guid curVdsId = (currentVdsId != null) ? currentVdsId : Guid.Empty;
        StoragePool storagePool = DbFacade.getInstance().getStoragePoolDao().get(_storagePoolId);

        if (storagePool == null) {
            log.info("hostFromVds::Finished elect spm, storage pool '{}' was removed", _storagePoolId);
            return null;
        }

        List<VDS> prioritizedVdsInPool = getPrioritizedVdsInPool();
        currentVdsId = null;

        // If VDS is in initialize status, wait for it to be up (or until
        // configurable timeout is reached)
        waitForVdsIfIsInitializing(curVdsId);
        // update pool status to problematic while selecting spm
        StoragePoolStatus prevStatus = storagePool.getStatus();
        if (prevStatus != StoragePoolStatus.NonResponsive) {
            try {
                ResourceManager
                        .getInstance()
                        .getEventListener()
                        .storagePoolStatusChange(_storagePoolId, StoragePoolStatus.NonResponsive,
                                AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_PROBLEMATIC_SEARCHING_NEW_SPM,
                                EngineError.ENGINE, TransactionScopeOption.RequiresNew);
            } catch (RuntimeException ex) {
                throw new IRSStoragePoolStatusException(ex);
            }
        }
        VDS selectedVds = null;
        SpmStatusResult spmStatus = null;

        if (prioritizedVdsInPool != null && prioritizedVdsInPool.size() > 0) {
            selectedVds = prioritizedVdsInPool.get(0);
        } else if (!Guid.Empty.equals(curVdsId) && !getTriedVdssList().contains(curVdsId)) {
            selectedVds = DbFacade.getInstance().getVdsDao().get(curVdsId);
            if (selectedVds.getStatus() != VDSStatus.Up
                    || selectedVds.getVdsSpmPriority() == BusinessEntitiesDefinitions.HOST_MIN_SPM_PRIORITY) {
                selectedVds = null;
            }
        }

        if (selectedVds != null) {
            // Stores origin host id in case and will be needed to disconnect from storage pool
            Guid selectedVdsId = selectedVds.getId();
            Integer selectedVdsSpmId = selectedVds.getVdsSpmId();
            triedVdssList.add(selectedVdsId);
            connectStoragePool(selectedVds, storagePool);

            VDSReturnValue returnValueFromVds = ResourceManager.getInstance().runVdsCommand(
                    VDSCommandType.SpmStatus,
                    new SpmStatusVDSCommandParameters(selectedVds.getId(), _storagePoolId));
            spmStatus = (SpmStatusResult) returnValueFromVds.getReturnValue();
            boolean ignoreSpmStatusResult = returnValueFromVds.getVdsError() != null
                    && returnValueFromVds.getVdsError().getCode() == EngineError.InquireNotSupportedError;
            if (spmStatus != null || ignoreSpmStatusResult) {
                currentVdsId = selectedVds.getId();
                boolean performedPoolConnect = false;
                log.info(
                        "hostFromVds::selectedVds - '{}', spmStatus '{}', storage pool '{}', storage pool version '{}'",
                        selectedVds.getName(),
                        spmStatus != null ? spmStatus.getSpmStatus() : "unknown",
                        storagePool.getName(),
                        storagePool.getCompatibilityVersion());
                if (ignoreSpmStatusResult) {
                    spmStatus = startSpm(storagePool, selectedVds, DEFAULT_PREV_ID, DEFAULT_LVER, DEFAULT_PREV_ID);
                } else {
                    if (spmStatus.getSpmStatus() == SpmStatus.Unknown_Pool) {
                        connectStoragePool(selectedVds, storagePool);
                        performedPoolConnect = true;
                        // refresh spmStatus result
                        spmStatus = (SpmStatusResult) ResourceManager
                                .getInstance()
                                .runVdsCommand(VDSCommandType.SpmStatus,
                                        new SpmStatusVDSCommandParameters(selectedVds.getId(), _storagePoolId))
                                .getReturnValue();
                        log.info(
                                "hostFromVds::Connected host to pool - selectedVds - {}, spmStatus {}, storage pool {}",
                                selectedVds.getName(),
                                spmStatus.getSpmStatus(),
                                storagePool.getName());
                    }

                    RefObject<VDS> tempRefObject = new RefObject<>(selectedVds);
                    spmStatus =
                            handleSpmStatusResult(curVdsId,
                                    prioritizedVdsInPool,
                                    storagePool,
                                    tempRefObject,
                                    spmStatus);
                    selectedVds = tempRefObject.argvalue;
                }

                if (selectedVds != null) {
                    RefObject<VDS> tempRefObject2 = new RefObject<>(selectedVds);
                    RefObject<SpmStatusResult> tempRefObject3 = new RefObject<>(spmStatus);
                    returnValue = handleSelectedVdsForSPM(storagePool, tempRefObject2, tempRefObject3, prevStatus);
                    selectedVds = tempRefObject2.argvalue;
                    spmStatus = tempRefObject3.argvalue;
                } else {
                    currentVdsId = null;
                }
                if (performedPoolConnect && selectedVds == null) {
                    // if could not start spm on this host and connected to
                    // pool here
                    // then disconnect
                    ResourceManager.getInstance().runVdsCommand(
                            VDSCommandType.DisconnectStoragePool,
                            new DisconnectStoragePoolVDSCommandParameters(selectedVdsId, _storagePoolId,
                                    selectedVdsSpmId));
                }
            } else {

                log.info("hostFromVds::selectedVds - '{}', spmStatus returned null!",
                        selectedVds.getName());
                if (returnValueFromVds.getExceptionObject() instanceof IRSNoMasterDomainException) {
                    throw returnValueFromVds.getExceptionObject();
                }
            }
        }
        return returnValue;
    }

    private List<VDS> getPrioritizedVdsInPool() {
        Guid curVdsId = (currentVdsId != null) ? currentVdsId : Guid.Empty;
        // Gets a list of the hosts in the storagePool, that are "UP", ordered
        // by vds_spm_priority (not including -1) and secondly ordered by RANDOM(), to
        // deal with the case that there are several hosts with the same priority.
        List<VDS> allVds = DbFacade.getInstance().getVdsDao().getListForSpmSelection(_storagePoolId);
        List<VDS> vdsRelevantForSpmSelection = new ArrayList<>();
        Guid preferredHost = IrsBrokerCommand.getIrsProxyData(_storagePoolId).getPreferredHostId();
        IrsBrokerCommand.getIrsProxyData(_storagePoolId).setPreferredHostId(null);

        for (VDS vds : allVds) {
            if (!triedVdssList.contains(vds.getId()) && !vds.getId().equals(curVdsId)) {
                if (vds.getId().equals(preferredHost)) {
                    vdsRelevantForSpmSelection.add(0, vds);
                }
                else {
                    vdsRelevantForSpmSelection.add(vds);
                }
            }
        }

        return vdsRelevantForSpmSelection;
    }

    private String handleSelectedVdsForSPM(StoragePool storagePool, RefObject<VDS> selectedVds,
                                           RefObject<SpmStatusResult> spmStatus, StoragePoolStatus prevStatus) {
        String returnValue = null;
        if (spmStatus.argvalue == null || spmStatus.argvalue.getSpmStatus() != SpmStatus.SPM) {
            movePoolToProblematicInDB(storagePool);

            selectedVds.argvalue = null;
            log.info(
                    "spm start treatment ended and status is not SPM!!! status: '{}' - setting selectedVds to null!",
                    spmStatus.argvalue != null ? spmStatus.argvalue.getSpmStatus() : null);
        } else {
            init(selectedVds.argvalue);
            storagePool.setLVER(spmStatus.argvalue.getSpmLVER());
            storagePool.setSpmVdsId(selectedVds.argvalue.getId());
            // if were problemtaic or not operational and succeeded to find
            // host move pool to up
            if (prevStatus != StoragePoolStatus.NotOperational && prevStatus != StoragePoolStatus.NonResponsive) {
                storagePool.setStatus(prevStatus);
            } else {
                storagePool.setStatus(StoragePoolStatus.Up);
            }
            DbFacade.getInstance().getStoragePoolDao().update(storagePool);
            ResourceManager.getInstance()
                    .getEventListener()
                    .storagePoolStatusChanged(storagePool.getId(), storagePool.getStatus());

            setFencedIrs(null);
            returnValue = selectedVds.argvalue.getHostName();
            log.info("Initialize Irs proxy from vds: {}", returnValue);
            AuditLogableBase logable = new AuditLogableBase(selectedVds.argvalue.getId());
            logable.addCustomValue("ServerIp", returnValue);
            new AuditLogDirector().log(logable, AuditLogType.IRS_HOSTED_ON_VDS);
        }
        return returnValue;
    }

    /**
     * Waits for VDS if is initializing.
     */
    private void waitForVdsIfIsInitializing(Guid curVdsId) {
        if (!Guid.Empty.equals(curVdsId)) {
            VDS vds = DbFacade.getInstance().getVdsDao().get(curVdsId);
            String vdsName = vds.getName();
            if (vds.getStatus() == VDSStatus.Initializing) {
                final int DELAY = 5;// 5 Sec
                int total = 0;
                Integer maxSecToWait = Config.getValue(ConfigValues.WaitForVdsInitInSec);
                while (total <= maxSecToWait
                        && DbFacade.getInstance().getVdsDynamicDao().get(curVdsId).getStatus() == VDSStatus.Initializing) {
                    try {
                        Thread.sleep(DELAY * 1000);
                    } catch (InterruptedException e) {
                        log.error("Interrupt exception {}", e.getMessage());
                        log.debug("Exception", e);
                        // exit the while block
                        break;
                    }
                    total += DELAY;
                    log.info("Waiting to Host '{}' to finish initialization for {} Sec.", vdsName, total);
                }
            }
        }
    }

    private void movePoolToProblematicInDB(StoragePool storagePool) {
        updateStoragePoolStatus(storagePool.getId(), StoragePoolStatus.NonResponsive,
                        AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_PROBLEMATIC, EngineError.ENGINE);

        storagePool.setSpmVdsId(null);
        DbFacade.getInstance().getStoragePoolDao().update(storagePool);
    }

    private boolean wasVdsManuallyFenced(int spmId) {
        VdsSpmIdMap map = DbFacade.getInstance().getVdsSpmIdMapDao().get(
                _storagePoolId, spmId);
        return map != null && map.getId().equals(getFencedIrs());
    }

    private static final String DEFAULT_LVER = "-1";
    private static final int DEFAULT_PREV_ID = -1;

    private SpmStatusResult handleSpmStatusResult(Guid curVdsId,
                                                  List<VDS> vdsByPool,
                                                  final StoragePool storagePool,
                                                  RefObject<VDS> selectedVds,
                                                  SpmStatusResult spmStatus) {
        if (spmStatus.getSpmStatus() == SpmStatus.Free) {
            int vdsSpmIdToFence = -1;
            boolean startSpm = true;
            if (spmStatus.getSpmId() != -1 && !wasVdsManuallyFenced(spmStatus.getSpmId())) {
                int spmId = spmStatus.getSpmId();
                Guid spmVdsId = Guid.Empty;
                VDS spmVds = null;
                if (selectedVds.argvalue.getVdsSpmId() == spmId) {
                    spmVdsId = selectedVds.argvalue.getId();
                } else {
                    for (VDS tempVds : vdsByPool) {
                        if (tempVds.getVdsSpmId() == spmId) {
                            log.info("Found spm host '{}', host name: '{}', according to spmId: '{}'.",
                                    tempVds.getId(),
                                    tempVds.getName(),
                                    spmId);
                            spmVds = tempVds;
                            break;
                        }
                    }
                    // if the host which is marked as SPM by the storage is
                    // non operational we want to find it as well
                    if (spmVds == null) {
                        List<VDS> nonOperationalVds =
                                DbFacade.getInstance()
                                        .getVdsDao()
                                        .getAllForStoragePoolAndStatus(_storagePoolId, VDSStatus.NonOperational);
                        for (VDS tempVds : nonOperationalVds) {
                            if (tempVds.getVdsSpmId() == spmId) {
                                spmVds = tempVds;
                                break;
                            }
                        }
                    }

                    if (spmVds != null) {
                        spmVdsId = spmVds.getId();
                    } else if (!curVdsId.equals(Guid.Empty)) {
                        VDS currentVds = DbFacade.getInstance().getVdsDao().get(curVdsId);
                        if (currentVds != null && currentVds.getStatus() == VDSStatus.Up
                                && currentVds.getVdsSpmId() != null && currentVds.getVdsSpmId().equals(spmId)) {
                            spmVdsId = curVdsId;
                            spmVds = currentVds;
                        }
                    }
                }

                try {
                    if (!spmVdsId.equals(Guid.Empty)) {
                        SpmStatusResult destSpmStatus = (SpmStatusResult) ResourceManager
                                .getInstance()
                                .runVdsCommand(VDSCommandType.SpmStatus,
                                        new SpmStatusVDSCommandParameters(spmVdsId, _storagePoolId))
                                .getReturnValue();
                        log.info("SpmStatus on vds '{}': '{}'", spmVdsId, destSpmStatus == null ? "NULL"
                                : destSpmStatus.getSpmStatus());

                        // intentionally unreachable code
                        if (destSpmStatus != null && destSpmStatus.getSpmStatus() == SpmStatus.SPM) {
                            if (!spmVdsId.equals(selectedVds.argvalue.getId()) && spmVds != null
                                    && spmVds.getStatus() == VDSStatus.Up) {
                                selectedVds.argvalue = spmVds;
                                startSpm = false;
                                log.info("Using old spm server: '{}', no start needed", spmVds.getName());
                                return destSpmStatus;
                            }
                            // VDS is non-operational and SPM
                            else {
                                log.warn("Host reports to be SPM '{}', but is not up.", spmVdsId);
                                vdsSpmIdToFence = spmStatus.getSpmId();
                            }
                        }
                        // if the host which is marked as SPM in the storage
                        // replies
                        // to SPMStatus with Unknown_Pool we don't need to
                        // fence it simply assume
                        // it is not SPM and continue.
                        else if (destSpmStatus == null
                                || (destSpmStatus.getSpmStatus() != SpmStatus.Free && destSpmStatus.getSpmStatus() != SpmStatus.Unknown_Pool)) {
                            vdsSpmIdToFence = spmStatus.getSpmId();
                        }
                    } else {
                        log.error(
                                "SPM Init: could not find reported vds or not up - pool: '{}' vds_spm_id: '{}'",
                                storagePool.getName(), spmStatus.getSpmId());
                        vdsSpmIdToFence = spmStatus.getSpmId();
                    }
                } catch (Exception ex) {
                    vdsSpmIdToFence = spmStatus.getSpmId();
                }
            }
            if (startSpm) {
                VdsSpmIdMap map = DbFacade.getInstance().getVdsSpmIdMapDao().get(
                        _storagePoolId, vdsSpmIdToFence);
                if (map != null) {
                    VDS vdsToFenceObject = DbFacade.getInstance().getVdsDao().get(map.getId());
                    if (vdsToFenceObject != null) {
                        log.info("SPM selection - vds seems as spm '{}'", vdsToFenceObject.getName());
                        if (vdsToFenceObject.getStatus() == VDSStatus.NonResponsive) {
                            log.warn("spm vds is non responsive, stopping spm selection.");
                            selectedVds.argvalue = null;
                            return spmStatus;
                        } else {
                            // try to stop spm
                            VDSReturnValue spmStopReturnValue = ResourceManager.getInstance().runVdsCommand(
                                    VDSCommandType.SpmStop,
                                    new SpmStopVDSCommandParameters(vdsToFenceObject.getId(), _storagePoolId));
                            // if spm stop succeeded no need to fence,
                            // continue with spm selection
                            if (spmStopReturnValue != null && spmStopReturnValue.getSucceeded()) {
                                log.info("spm stop succeeded, continuing with spm selection");
                            }
                            // if spm stop failed for any reason we stop spm
                            // selection
                            else {
                                log.warn("spm stop on spm failed, stopping spm selection!");
                                selectedVds.argvalue = null;
                                return spmStatus;
                            }
                        }
                    }
                }
                spmStatus = startSpm(storagePool, selectedVds.argvalue, spmStatus.getSpmId(), spmStatus.getSpmLVER(), vdsSpmIdToFence);
            }
        }
        return spmStatus;
    }


    private SpmStatusResult startSpm(final StoragePool storagePool, VDS selectedVds, int prevId, String lver, int vdsSpmIdToFence) {

        storagePool.setStatus(StoragePoolStatus.Contend);
        storagePool.setSpmVdsId(selectedVds.getId());

        TransactionSupport.executeInNewTransaction(() -> {
            DbFacade.getInstance().getStoragePoolDao().update(storagePool);
            return null;
        });

        log.info("starting spm on vds '{}', storage pool '{}', prevId '{}', LVER '{}'",
                selectedVds.getName(), storagePool.getName(), prevId,
                lver);
        SpmStatusResult spmStatus = (SpmStatusResult) ResourceManager
                .getInstance()
                .runVdsCommand(
                        VDSCommandType.SpmStart,
                        new SpmStartVDSCommandParameters(selectedVds.getId(),
                                _storagePoolId,
                                prevId,
                                lver,
                                storagePool
                                        .getRecoveryMode(),
                                vdsSpmIdToFence != -1,
                                storagePool.getStoragePoolFormatType())).getReturnValue();
        if (spmStatus == null || spmStatus.getSpmStatus() != SpmStatus.SPM) {
            ResourceManager
                    .getInstance()
                    .getEventListener()
                    .storagePoolStatusChange(storagePool.getId(),
                            StoragePoolStatus.NonResponsive,
                            AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_PROBLEMATIC,
                            EngineError.ENGINE,
                            TransactionScopeOption.RequiresNew);
            if (spmStatus != null) {
                 TransactionSupport.executeInNewTransaction(() -> {
                     StoragePool pool =
                             DbFacade.getInstance().getStoragePoolDao().get(storagePool.getId());
                     pool.setSpmVdsId(null);
                     DbFacade.getInstance().getStoragePoolDao().update(pool);
                     return null;
                 });
            }
            throw new IrsSpmStartFailedException();
        }

        return spmStatus;
    }

    public String getIsoDirectory() {
        String tempVar = privatemCurrentIrsHost;
        return String.format("\\\\%1$s\\CD", tempVar != null ? tempVar : gethostFromVds());
    }

    public void resetIrs() {
        nullifyInternalProxies();
        StoragePool storagePool = DbFacade.getInstance().getStoragePoolDao().get(_storagePoolId);
        if (storagePool != null) {
            storagePool.setSpmVdsId(null);
            DbFacade.getInstance().getStoragePoolDao().update(storagePool);
        }
    }

    private void nullifyInternalProxies() {
        if (irsProxy != null) {
            irsProxy.close();
        }
        privatemCurrentIrsHost = null;
        irsProxy = null;
        currentVdsId = null;
    }

    private final Map<Guid, HashSet<Guid>> _domainsInProblem = new ConcurrentHashMap<>();
    private final Map<Guid, HashSet<Guid>> _domainsInMaintenance = new ConcurrentHashMap<>();
    private final Map<Guid, Guid> vdsReportsOnUnseenDomain = new ConcurrentHashMap<>();
    private final Map<Guid, Guid> vdsHandeledReportsOnUnseenDomains = new ConcurrentHashMap<>();
    private final Map<Guid, String> _timers = new HashMap<>();

    public void updateVdsDomainsData(VDS vds,
                                     final ArrayList<VDSDomainsData> data) {
        if (!shouldProcessVdsDomainReport(vds)) {
            return;
        }

        StoragePool storagePool =
                DbFacade.getInstance().getStoragePoolDao().get(_storagePoolId);
        if (storagePool != null
                && (storagePool.getStatus() == StoragePoolStatus.Up
                || storagePool.getStatus() == StoragePoolStatus.NonResponsive)) {

            Guid vdsId = vds.getId();
            String vdsName = vds.getName();
            try {
                Set<Guid> monitoredDomains = new HashSet<>();
                for (VDSDomainsData tempData : data) {
                    monitoredDomains.add(tempData.getDomainId());
                }

                Map<Guid, DomainMonitoringResult> domainsProblematicReportInfo = Collections.emptyMap();

                if (StoragePoolDomainHelper.vdsDomainsActiveMonitoringStatus.contains(vds.getStatus())) {
                    domainsProblematicReportInfo =
                            handleMonitoredDomainsForHost(vdsId,
                                    vdsName,
                                    data,
                                    monitoredDomains,
                                    storagePool);
                }

                Set<Guid> domainsInMaintenance = Collections.emptySet();
                if (StoragePoolDomainHelper.vdsDomainsMaintenanceMonitoringStatus.contains(vds.getStatus())) {
                    domainsInMaintenance = handleDomainsInMaintenanceForHost(monitoredDomains);
                }

                updateDomainInProblem(vdsId, vdsName, domainsProblematicReportInfo, domainsInMaintenance);
            } catch (RuntimeException ex) {
                log.error("error in updateVdsDomainsData: {}", ex.getMessage());
                log.debug("Exception", ex);
            }
        }
    }

    private static boolean shouldProcessVdsDomainReport(VDS vds) {
        // NOTE - if this condition is ever updated, every place that acts upon the reporting
        // should be updated as well, only hosts the we collect the report from should be affected
        // from it.
        return vds.getClusterSupportsVirtService() &&
                (StoragePoolDomainHelper.vdsDomainsActiveMonitoringStatus.contains(vds.getStatus()) ||
                StoragePoolDomainHelper.vdsDomainsMaintenanceMonitoringStatus.contains(vds.getStatus()));
    }

    /**
     * The methods inspects which domains status can be changed to Maintenance according to the host
     * domains report.
     * @param monitoredDomains domains that the host monitors
     * @return domains that are not monitored by the host and are in
     * @link StorageDomainStatus#Maintenance or @link StorageDomainStatus#PreparingForMaintenance.
     */
    private Set<Guid> handleDomainsInMaintenanceForHost(Collection<Guid> monitoredDomains) {
        Set<Guid>  domainsInMaintenance = new HashSet<>();
        Set<Guid> maintInPool = new HashSet<>(
                DbFacade.getInstance().getStorageDomainStaticDao().getAllIds(
                        _storagePoolId, StorageDomainStatus.Maintenance));
        maintInPool.addAll(DbFacade.getInstance().getStorageDomainStaticDao().getAllIds(
                _storagePoolId, StorageDomainStatus.PreparingForMaintenance));

        for (Guid tempDomainId : maintInPool) {
            if (!monitoredDomains.contains(tempDomainId)) {
                domainsInMaintenance.add(tempDomainId);
            }
        }

        return domainsInMaintenance;
    }

    /**
     * Provides handling for the domains that are monitored by the given host.
     * @return map between the domain id and the reason for domains that
     * the host reporting is problematic for.
     */
    private Map<Guid, DomainMonitoringResult> handleMonitoredDomainsForHost(final Guid vdsId, final String vdsName,
            final ArrayList<VDSDomainsData> data, Collection<Guid> monitoredDomains, StoragePool storagePool) {
        Map<Guid, DomainMonitoringResult> domainsProblematicReportInfo = new HashMap<>();
        // build a list of all domains in pool
        // which are in status Active or Unknown
        Set<Guid> activeDomainsInPool = new HashSet<>(
                DbFacade.getInstance().getStorageDomainStaticDao().getAllIds(
                        _storagePoolId, StorageDomainStatus.Active));
        Set<Guid> unknownDomainsInPool = new HashSet<>(DbFacade.getInstance().getStorageDomainStaticDao().getAllIds(
                _storagePoolId, StorageDomainStatus.Unknown));
        Set<Guid> inActiveDomainsInPool =
                new HashSet<>(DbFacade.getInstance()
                        .getStorageDomainStaticDao()
                        .getAllIds(_storagePoolId, StorageDomainStatus.Inactive));

        // build a list of all the domains in
        // pool (activeDomainsInPool and unknownDomainsInPool) that are not
        // visible by the host.
        for (Guid tempDomainId : activeDomainsInPool) {
            if (!monitoredDomains.contains(tempDomainId)) {
                domainsProblematicReportInfo.put(tempDomainId, DomainMonitoringResult.NOT_REPORTED);
            }
        }

        for (Guid tempDomainId : unknownDomainsInPool) {
            if (!monitoredDomains.contains(tempDomainId)) {
                domainsProblematicReportInfo.put(tempDomainId, DomainMonitoringResult.NOT_REPORTED);
            }
        }

        // build a list of domains that the host
        // reports as in problem (code!=0) or (code==0
        // && lastChecl >
        // ConfigValues.MaxStorageVdsTimeoutCheckSec)
        // and are contained in the Active or
        // Unknown domains in pool
        for (VDSDomainsData tempData : data) {
            if (activeDomainsInPool.contains(tempData.getDomainId()) || unknownDomainsInPool.contains(tempData.getDomainId())) {
                DomainMonitoringResult domainMonitoringResult = analyzeDomainReport(tempData, false);
                if (domainMonitoringResult.invalidAndActual()) {
                    domainsProblematicReportInfo.put(tempData.getDomainId(), domainMonitoringResult);
                } else if (domainMonitoringResult.actual() && tempData.getDelay() > Config.<Double> getValue(ConfigValues.MaxStorageVdsDelayCheckSec)) {
                    logDelayedDomain(vdsId, tempData);
                }
            }

            else if ((inActiveDomainsInPool.contains(tempData.getDomainId()) ||
                    // in data centers with spm, unknown domains are moving to Active status according to the pool metadata.
                    (FeatureSupported.dataCenterWithoutSpm(storagePool.getCompatibilityVersion()) && unknownDomainsInPool.contains(tempData.getDomainId())))
                    && analyzeDomainReport(tempData, false).validAndActual()) {
                log.warn("Storage Domain '{}' was reported by Host '{}' as Active in Pool '{}', moving to active status",
                        getDomainIdTuple(tempData.getDomainId()),
                        vdsName,
                        _storagePoolId);
                StoragePoolIsoMap map =
                        DbFacade.getInstance()
                                .getStoragePoolIsoMapDao()
                                .get(new StoragePoolIsoMapId(tempData.getDomainId(), _storagePoolId));
                map.setStatus(StorageDomainStatus.Active);
                DbFacade.getInstance().getStoragePoolIsoMapDao().update(map);

                // For block domains, synchronize LUN details comprising the storage domain with the DB
                StorageDomain storageDomain = DbFacade.getInstance().getStorageDomainDao().get(tempData.getDomainId());
                if (storageDomain.getStorageType().isBlockDomain()) {
                    ResourceManager.getInstance().getEventListener().syncLunsInfoForBlockStorageDomain(
                            storageDomain.getId(), vdsId);
                }
            }
        }

        return domainsProblematicReportInfo;
    }

    private void updateDomainInProblem(final Guid vdsId, final String vdsName, final Map<Guid, DomainMonitoringResult> domainsInProblem,
                                       final Set<Guid> domainsInMaintenance) {
        getEventQueue().submitEventSync(new Event(_storagePoolId,
                null, vdsId, EventType.DOMAINMONITORING, ""),
                () -> {
                    EventResult result = new EventResult(true, EventType.DOMAINMONITORING);
                    updateProblematicVdsData(vdsId, vdsName, domainsInProblem);
                    updateMaintenanceVdsData(vdsId, vdsName, domainsInMaintenance);
                    return result;
                });
    }

    private void logDelayedDomain(final Guid vdsId, VDSDomainsData tempData) {
        AuditLogableBase logable = new AuditLogableBase();
        logable.setVdsId(vdsId);
        logable.setStorageDomainId(tempData.getDomainId());
        logable.addCustomValue("Delay",
                Double.toString(tempData.getDelay()));
        new AuditLogDirector().log(logable,
                AuditLogType.VDS_DOMAIN_DELAY_INTERVAL);
    }

    protected List<Guid> obtainDomainsReportedAsProblematic(List<VDSDomainsData> vdsDomainsData) {
        List<Guid> domainsInProblem = new LinkedList<>();
        Set<Guid> domainsInPool = new HashSet<>(
                DbFacade.getInstance().getStorageDomainStaticDao().getAllIds(
                        _storagePoolId, StorageDomainStatus.Active));
        domainsInPool.addAll(DbFacade.getInstance().getStorageDomainStaticDao().getAllIds(
                _storagePoolId, StorageDomainStatus.Unknown));
        List<Guid> domainWhichWereSeen = new ArrayList<>();
        for (VDSDomainsData vdsDomainData : vdsDomainsData) {
            if (domainsInPool.contains(vdsDomainData.getDomainId())) {
                if (analyzeDomainReport(vdsDomainData, true).invalidAndActual()) {
                    domainsInProblem.add(vdsDomainData.getDomainId());
                }
                domainWhichWereSeen.add(vdsDomainData.getDomainId());
            }
        }
        domainsInPool.removeAll(domainWhichWereSeen);
        if (domainsInPool.size() > 0) {
            for (Guid domainId : domainsInPool) {
                log.error("Domain '{}' is not seen by Host", domainId);
            }
            domainsInProblem.addAll(domainsInPool);
        }
        return domainsInProblem;
    }

    private enum DomainMonitoringResult {
        PROBLEMATIC(Boolean.FALSE), STORAGE_ACCCESS_ERROR(Boolean.FALSE), OK(Boolean.TRUE), NOT_REPORTED(Boolean.FALSE), NOT_ACTUAL(null);

        private Boolean valid;

        private DomainMonitoringResult(Boolean valid) {
            this.valid = valid;
        }

        public boolean validAndActual() {
            return actual() && valid;
        }

        public boolean invalidAndActual() {
            return actual() && !valid;
        }

        public boolean actual() {
            return this != NOT_ACTUAL;
        }
    }

    private DomainMonitoringResult analyzeDomainReport(VDSDomainsData tempData, boolean isLog) {
        if (!tempData.isActual()) {
            log.warn("Domain '{}' report isn't an actual report",
                    getDomainIdTuple(tempData.getDomainId()));
            return DomainMonitoringResult.NOT_ACTUAL;
        }

        if (tempData.getCode() != 0) {
            if (isLog) {
                log.error("Domain '{}' was reported with error code '{}'",
                        getDomainIdTuple(tempData.getDomainId()),
                        tempData.getCode());
            }

            if (tempData.getCode() == EngineError.StorageDomainDoesNotExist.getValue()
                    || tempData.getCode() == EngineError.StorageException.getValue()) {
                return DomainMonitoringResult.STORAGE_ACCCESS_ERROR;
            }

            return DomainMonitoringResult.PROBLEMATIC;
        }
        if (tempData.getLastCheck() > Config
                .<Double> getValue(ConfigValues.MaxStorageVdsTimeoutCheckSec)) {
            if (isLog) {
                log.error("Domain '{}' check timeout '{}' is too big",
                        getDomainIdTuple(tempData.getDomainId()),
                        tempData.getLastCheck());
            }
            return DomainMonitoringResult.PROBLEMATIC;
        }

        return DomainMonitoringResult.OK;
    }

    private Guid clearVdsReportInfoOnUnseenDomain(Guid vdsId) {
        return vdsReportsOnUnseenDomain.remove(vdsId);
    }

    private void updateMaintenanceVdsData(final Guid vdsId, final String vdsName, Set<Guid> domainsInMaintenance) {
        for (Guid domainId : domainsInMaintenance) {
            Set<Guid> vdsSet = _domainsInMaintenance.get(domainId);
            if (vdsSet == null) {
                log.info("Adding domain '{}' to the domains in maintenance cache", domainId);
                _domainsInMaintenance.put(domainId, new HashSet<>(Arrays.asList(vdsId)));
            } else {
                vdsSet.add(vdsId);
            }
        }
        Set<Guid> maintenanceDomainsByHost = new HashSet<>(_domainsInMaintenance.keySet());
        maintenanceDomainsByHost.removeAll(domainsInMaintenance);
        for (Guid domainId : maintenanceDomainsByHost) {
            Set<Guid> vdsForDomain = _domainsInMaintenance.get(domainId);
            if (vdsForDomain != null && vdsForDomain.contains(vdsId)) {
                vdsForDomain.remove(vdsId);
                if (vdsForDomain.isEmpty()) {
                    log.info("Removing domain '{}' from the domains in maintenance cache", domainId);
                    _domainsInMaintenance.remove(domainId);
                }
            }
        }
    }

    private void updateProblematicVdsData(final Guid vdsId, final String vdsName, Map<Guid, DomainMonitoringResult> problematicDomains) {
        // for all problematic domains
        // update cache of _domainsInProblem
        // and _vdssInProblem and add a new
        // timer for new domains in problem
        boolean newDomainUnreachableByHost = false;
        List<Guid> domainsUnreachableByHost = new LinkedList<>();
        for (Map.Entry<Guid, DomainMonitoringResult> entry : problematicDomains.entrySet()) {
            Guid domainId = entry.getKey();
            DomainMonitoringResult domainMonitoringResult = entry.getValue();
            HashSet<Guid> hostsReportedDomainAsProblematic = _domainsInProblem.get(domainId);
            boolean domainNotFound = domainMonitoringResult == DomainMonitoringResult.STORAGE_ACCCESS_ERROR;
            if (domainNotFound) {
                domainsUnreachableByHost.add(domainId);
            }
            if (hostsReportedDomainAsProblematic != null) {
                if (!hostsReportedDomainAsProblematic.contains(vdsId) && domainNotFound) {
                    newDomainUnreachableByHost = true;
                }
                // existing domains in problem
                updateDomainInProblemData(domainId, vdsId, vdsName, domainMonitoringResult);
            } else {
                if (domainNotFound) {
                    newDomainUnreachableByHost = true;
                }
                // new domains in problems
                addDomainInProblemData(domainId, vdsId, vdsName, domainMonitoringResult);
            }
        }

        if (domainsUnreachableByHost.isEmpty()) {
            Guid clearedReport = clearVdsReportInfoOnUnseenDomain(vdsId);
            if (clearedReport != null) {
                log.info("Host '{}' no longer storage access problem to any relevant domain " +
                                " clearing it's report (report id: '{}')",
                        vdsId,
                        clearedReport);
            }
        } else if (newDomainUnreachableByHost) {
            Guid newReportId = Guid.newGuid();
            log.info("Host '{}' has reported new storage access problem to the following domains '{}'" +
                    " marking it for storage connections and pool metadata refresh (report id: '{}')",
                    vdsId,
                    StringUtils.join(domainsUnreachableByHost, ","),
                    newReportId);
            vdsReportsOnUnseenDomain.put(vdsId, newReportId);
        }

        Set<Guid> notReportedDomainsByHost = new HashSet<>(_domainsInProblem.keySet());
        notReportedDomainsByHost.removeAll(problematicDomains.keySet());
        for (Guid domainId : notReportedDomainsByHost) {
            Set<Guid> vdsForDomain = _domainsInProblem.get(domainId);
            if (vdsForDomain != null && vdsForDomain.contains(vdsId)) {
                domainRecoveredFromProblem(domainId, vdsId, vdsName);
            }
        }
    }

    private void domainRecoveredFromProblem(Guid domainId, Guid vdsId, String vdsName) {
        String domainIdTuple = getDomainIdTuple(domainId);
        log.info("Domain '{}' recovered from problem. vds: '{}'", domainIdTuple, vdsName);
        _domainsInProblem.get(domainId).remove(vdsId);
        if (_domainsInProblem.get(domainId).size() == 0) {
            log.info("Domain '{}' has recovered from problem. No active host in the DC is reporting it as" +
                    " problematic, so clearing the domain recovery timer.", domainIdTuple);
            _domainsInProblem.remove(domainId);
            clearTimer(domainId);
        }
    }

    private void addDomainInProblemData(Guid domainId, Guid vdsId, String vdsName,
                                        DomainMonitoringResult domainMonitoringResult) {
        _domainsInProblem.put(domainId, new HashSet<>(Arrays.asList(vdsId)));
        log.warn("domain '{}' in problem '{}'. vds: '{}'", getDomainIdTuple(domainId), domainMonitoringResult,
                vdsName);
        Class[] inputType = new Class[] { Guid.class };
        Object[] inputParams = new Object[] { domainId };
        String jobId = getSchedulUtil().scheduleAOneTimeJob(this, "onTimer", inputType,
                inputParams, Config.<Integer>getValue(ConfigValues.StorageDomainFailureTimeoutInMinutes),
                TimeUnit.MINUTES);
        clearTimer(domainId);
        _timers.put(domainId, jobId);
    }

    @OnTimerMethodAnnotation("onTimer")
    public void onTimer(final Guid domainId) {
        getEventQueue().submitEventAsync(new Event(_storagePoolId,
                domainId, null, EventType.DOMAINFAILOVER, ""),
                () -> {
                    EventResult result = null;
                    if (_domainsInProblem.containsKey(domainId)) {
                        log.info("starting processDomainRecovery for domain '{}'.", getDomainIdTuple(domainId));
                        result = processDomainRecovery(domainId);
                    }
                    _timers.remove(domainId);
                    return result;
                });
    }


    private Map<Guid, Guid> procceedReportsThreatmenet() {
        if (vdsReportsOnUnseenDomain.isEmpty()) {
            if (!vdsHandeledReportsOnUnseenDomains.isEmpty()) {
                log.info("No hosts has reported storage access problem to domains, clearing the handled hosts reports map");
                vdsHandeledReportsOnUnseenDomains.clear();
            }

            return Collections.emptyMap();
        }

        Map<Guid, Guid> reportsToHandle = new HashMap<>();
        reportsToHandle.putAll(vdsReportsOnUnseenDomain);

        for (Map.Entry<Guid, Guid> entry : vdsHandeledReportsOnUnseenDomains.entrySet()) {
            Guid vdsId = entry.getKey();
            Guid currentReportId = reportsToHandle.get(vdsId);
            if (currentReportId == null) {
                log.info("Host '{}' has no longer storage access problem to domains, clearing it from the handled hosts reports map",
                        vdsId);
                vdsHandeledReportsOnUnseenDomains.remove(vdsId);
            } else {
                Guid handledReportId = entry.getValue();
                if (currentReportId.equals(handledReportId)) {
                    log.debug("Host '{}' storage connections and pool metadata were already refreshed for report '{}', skipping it",
                            vdsId,
                            handledReportId);
                    reportsToHandle.remove(vdsId);
                }
            }
        }

        return reportsToHandle;
    }

    @OnTimerMethodAnnotation("hostsStorageConnectionsAndPoolMetadataRefresh")
    public void hostsStorageConnectionsAndPoolMetadataRefresh() {
        Map<Guid, Guid> reportsToHandle = procceedReportsThreatmenet();

        if (reportsToHandle.isEmpty()) {
            return;
        }

        List<Callable<Void>> connectStorageTasks = new ArrayList<>();
        final List<Callable<Void>> refreshStoragePoolTasks = new ArrayList<>();
        final StoragePool storagePool = DbFacade.getInstance().getStoragePoolDao().get(_storagePoolId);
        final Guid masterDomainId =
                DbFacade.getInstance().getStorageDomainDao().getMasterStorageDomainIdForPool(_storagePoolId);
        final  List<StoragePoolIsoMap> storagePoolIsoMap = DbFacade.getInstance()
                .getStoragePoolIsoMapDao().getAllForStoragePool(_storagePoolId);

        Map<String, Pair<String, String>> acquiredLocks = new HashMap<>();
        try {
            for (Map.Entry<Guid, Guid> entry : reportsToHandle.entrySet()) {
                Guid vdsId = entry.getKey();
                Guid currentReportId = entry.getValue();

                vdsHandeledReportsOnUnseenDomains.put(vdsId, currentReportId);
                Map<String, Pair<String, String>> lockMap = Collections.singletonMap(vdsId.toString(),
                        new Pair<>(LockingGroup.VDS_POOL_AND_STORAGE_CONNECTIONS.toString(),
                                EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED.toString()));
                EngineLock engineLock = new EngineLock(lockMap, null);
                if (!LockManagerFactory.getLockManager()
                        .acquireLock(engineLock)
                        .getFirst()) {
                    log.info("Failed to acquire lock to refresh storage connection and pool metadata for host '{}', skipping it",
                            vdsId);
                    continue;
                }

                final VDS vds = DbFacade.getInstance().getVdsDao().get(entry.getKey());
                if (vds.getStatus() != VDSStatus.Up) {
                    log.info("Skipping storage connection and pool metadata information for host '{}' as it's no longer in status UP",
                            vdsId);
                    LockManagerFactory.getLockManager().releaseLock(engineLock);
                    continue;
                }

                acquiredLocks.putAll(lockMap);

                connectStorageTasks.add(() -> {
                    ResourceManager.getInstance()
                            .getEventListener().connectHostToDomainsInActiveOrUnknownStatus(vds);
                    return null;
                });

                refreshStoragePoolTasks.add(() -> {
                    StoragePoolDomainHelper.refreshHostPoolMetadata(vds, storagePool, masterDomainId, storagePoolIsoMap);
                    return null;
                });
            }

            final Set<String> handledHosts = acquiredLocks.keySet();
            log.info("Running storage connections refresh for hosts '{}'", handledHosts);
            ThreadPoolUtil.invokeAll(connectStorageTasks);

            log.info("Submitting to the event queue pool refresh for hosts '{}'", handledHosts);
            getEventQueue().submitEventSync(new Event(_storagePoolId,
                            null,
                            null,
                            EventType.POOLREFRESH,
                            ""),
                    () -> {
                        log.info("Running storage pool metadata refresh for hosts '{}'", handledHosts);
                        ThreadPoolUtil.invokeAll(refreshStoragePoolTasks);
                        return new EventResult(true, EventType.POOLREFRESH);
                    });
        } finally {
            if (!acquiredLocks.isEmpty()) {
                LockManagerFactory.getLockManager().releaseLock(new EngineLock(acquiredLocks, null));
            }
        }
    }

    private void updateDomainInProblemData(Guid domainId, Guid vdsId, String vdsName,
                                           DomainMonitoringResult domainMonitoringResult) {
        log.debug("domain '{}' still in problem '{}'. vds: '{}'", getDomainIdTuple(domainId),
                domainMonitoringResult, vdsName);
        _domainsInProblem.get(domainId).add(vdsId);
    }

    private EventResult processDomainRecovery(final Guid domainId) {
        EventResult result = null;
        // build a list of all the hosts in status UP in
        // Pool.
        List<Guid> vdssInPool = new ArrayList<>();
        // Note - this method is used as it returns only hosts from VIRT supported clusters
        // (we use the domain monitoring results only from those clusters hosts).
        // every change to it should be inspected carefully.
        List<VDS> allVds = DbFacade.getInstance().getVdsDao().getAllForStoragePoolAndStatus(_storagePoolId, null);
        Map<Guid, VDS> vdsMap = new HashMap<>();
        for (VDS tempVDS : allVds) {
            vdsMap.put(tempVDS.getId(), tempVDS);
            if (tempVDS.getStatus() == VDSStatus.Up) {
                vdssInPool.add(tempVDS.getId());
            }
        }

        // build a list of all the hosts that did not report
        // on this domain as in problem.
        // Mark the above list as hosts we suspect are in
        // problem.
        Set<Guid> hostsThatReportedDomainAsInProblem = _domainsInProblem.get(domainId);
        List<Guid> vdssInProblem = new ArrayList<>();
        for (Guid tempVDSId : vdssInPool) {
            if (!hostsThatReportedDomainAsInProblem.contains(tempVDSId)) {
                vdssInProblem.add(tempVDSId);
            }
        }

        // If not All the hosts in status UP reported on
        // this domain as in problem. We assume the problem
        // is with the hosts
        // that did report on a problem with this domain.
        // (and not a problem with the domain itself).
        StorageDomainStatic storageDomain = DbFacade.getInstance().getStorageDomainStaticDao().get(domainId);
        String domainIdTuple = getDomainIdTuple(domainId);
        List<Guid> nonOpVdss = new ArrayList<>();
        if (vdssInProblem.size() > 0) {
            if (storageDomain.getStorageDomainType() != StorageDomainType.ImportExport
                    && storageDomain.getStorageDomainType() != StorageDomainType.ISO) {
                // The domain is of type DATA and was
                // reported as in problem.
                // Moving all the hosts which reported on
                // this domain as in problem to non
                // operational.
                for (final Guid vdsId : _domainsInProblem.get(domainId)) {
                    VDS vds = vdsMap.get(vdsId);
                    if (vds == null) {
                        log.warn(
                                "vds '{}' reported domain '{}' - as in problem but cannot find vds in db!!",
                                vdsId,
                                domainIdTuple);
                    } else if (vds.getStatus() == VDSStatus.Up) {
                        log.warn(
                                "vds '{}' reported domain '{}' as in problem, attempting to move the vds to status NonOperational",
                                vds.getName(),
                                domainIdTuple);

                        final Map<String, String> customLogValues = Collections.singletonMap("StorageDomainNames", storageDomain.getName());
                        ThreadPoolUtil.execute(() -> ResourceManager
                                .getInstance()
                                .getEventListener()
                                .vdsNonOperational(vdsId, NonOperationalReason.STORAGE_DOMAIN_UNREACHABLE,
                                        true, domainId, customLogValues));

                        nonOpVdss.add(vdsId);
                    } else {
                        log.warn(
                                "vds '{}' reported domain '{}' as in problem, vds is in status '{}', no need to move to nonoperational",
                                vds.getName(),
                                domainIdTuple,
                                vds.getStatus());
                    }
                }
            } else {
                log.warn(
                        "Storage domain '{}' is not visible to one or more hosts. " +
                                "Since the domain's type is '{}', hosts status will not be changed to non-operational",
                        domainIdTuple,
                        storageDomain.getStorageDomainType());
            }
            result = new EventResult(true, EventType.VDSSTOARGEPROBLEMS);
        } else { // Because all the hosts in status UP
                 // reported on this domain as in problem
                 // we assume the problem is with the
                 // Domain.
            if (storageDomain.getStorageDomainType() != StorageDomainType.Master) {
                log.error("Domain '{}' was reported by all hosts in status UP as problematic. Moving the domain to NonOperational.",
                        domainIdTuple);
                result = ResourceManager.getInstance()
                        .getEventListener().storageDomainNotOperational(domainId, _storagePoolId);
            } else {
                log.warn("Domain '{}' was reported by all hosts in status UP as problematic. Not moving the domain to NonOperational because it is being reconstructed now.",
                        domainIdTuple);
                result = ResourceManager.getInstance()
                        .getEventListener().masterDomainNotOperational(domainId, _storagePoolId, false, false);
            }
        }

        // clear from cache of _domainsInProblem
        clearDomainFromCache(domainId, nonOpVdss);
        return result;
    }

    /**
     * clears the time for the given domain
     *
     * @param domainId
     *            - the domain to clean the timer for
     */
    private void clearTimer(Guid domainId) {
        String jobId = _timers.remove(domainId);
        if (jobId != null) {
            getSchedulUtil().deleteJob(jobId);
        }
    }

    /**
     * clears the problematic domain from the vdss that reported on this domain as problematic and from the domains
     * in problem
     *
     * @param domainId
     *            - the domain to clear cache for.
     * @param nonOpVdss - passed vdss that non operational
     */
    private void clearDomainFromCache(Guid domainId, List<Guid> nonOpVdss) {
        if (domainId != null) {
            _domainsInProblem.remove(domainId);
        }
        removeVdsAsProblematic(nonOpVdss);
        removeVdsFromDomainMaintenance(nonOpVdss);
        removeVdsFromUnseenDomainsReport(nonOpVdss);
    }

    private void removeVdsAsProblematic(List<Guid> nonOpVdss) {
        Iterator<Map.Entry<Guid, HashSet<Guid>>> iterDomainsInProblem = _domainsInProblem.entrySet().iterator();
        while (iterDomainsInProblem.hasNext()) {
            Map.Entry<Guid, HashSet<Guid>> entry = iterDomainsInProblem.next();
            entry.getValue().removeAll(nonOpVdss);
            if (entry.getValue().isEmpty()) {
                iterDomainsInProblem.remove();
                clearTimer(entry.getKey());
                log.info("Domain '{}' has recovered from problem. No active host in the DC is reporting it as poblematic, so clearing the domain recovery timer.",
                        getDomainIdTuple(entry.getKey()));
            }

        }
    }

    private void removeVdsFromUnseenDomainsReport(List<Guid> nonOpVdss) {
        log.info("Removing host(s) '{}' from hosts unseen domain report cache", nonOpVdss);
        for(Guid id : nonOpVdss) {
            clearVdsReportInfoOnUnseenDomain(id);
        }
    }

    private void removeVdsFromDomainMaintenance(List<Guid> nonOpVdss) {
        log.info("Removing vds '{}' from the domain in maintenance cache", nonOpVdss);
        Iterator<Map.Entry<Guid, HashSet<Guid>>> iterDomainsInProblem = _domainsInMaintenance.entrySet().iterator();
        while (iterDomainsInProblem.hasNext()) {
            Map.Entry<Guid, HashSet<Guid>> entry = iterDomainsInProblem.next();
            entry.getValue().removeAll(nonOpVdss);
            if (entry.getValue().isEmpty()) {
                iterDomainsInProblem.remove();
            }
        }
    }

    /**
     * deletes all the jobs for the domains in the pool and clears the problematic entities caches.
     */
    public void clearCache() {
        log.info("clearing cache for problematic entities in pool '{}'.", _storagePoolId);
        // clear lists
        _timers.clear();
        _domainsInProblem.clear();
    }

    public void clearPoolTimers() {
        log.info("clear domain error-timers for pool '{}'.", _storagePoolId);
        for (String jobId : _timers.values()) {
            try {
                getSchedulUtil().deleteJob(jobId);
            } catch (Exception e) {
                log.warn("failed deleting job '{}'.", jobId);
            }
        }
    }

    /**
     * Remove a VDS entry from the cache, clearing the problematic domains for this VDS and their times if they
     * need to be cleaned. This is for a case when the VDS is switched to maintenance by the user, since we
     * need to clear it's cache data and timers, or else the cache will contain stale data (since the VDS is not
     * active anymore, it won't be connected to any of the domains).<br>
     *
     * @param vdsId The ID of the VDS to remove from the cache.
     * @param vdsName The name of the VDS (for logging).
     */
    public void clearVdsFromCache(Guid vdsId, String vdsName) {
        log.info("Clearing cache of pool: '{}' for problematic entities of VDS: '{}'.",
                _storagePoolId, vdsName);

        clearDomainFromCache(null, Arrays.asList(vdsId));
    }

    private boolean _disposed;

    public void dispose() {
        runInControlledConcurrency(() -> {
            log.info("IrsProxyData::disposing");
            resetIrs();
            getSchedulUtil().deleteJob(storagePoolRefreshJobId);
            getSchedulUtil().deleteJob(domainRecoverOnHostJobId);
            _disposed = true;
        });
    }

    private static String getDomainIdTuple(Guid domainId) {
        StorageDomainStatic storage_domain = DbFacade.getInstance().getStorageDomainStaticDao().get(domainId);
        if (storage_domain != null) {
            return domainId + ":" + storage_domain.getStorageName();
        } else {
            return domainId.toString();
        }
    }
}

package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
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
import org.ovirt.engine.core.common.businessentities.vds_spm_id_map;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
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
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManagerFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.jsonrpc.JsonRpcIIrsServer;
import org.ovirt.engine.core.vdsbroker.jsonrpc.TransportFactory;
import org.ovirt.engine.core.vdsbroker.storage.StoragePoolDomainHelper;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSNetworkException;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcUtils;
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
    public Object syncObj = new Object();

    private final String storagePoolRefreshJobId;
    private final String domainRecoverOnHostJobId;
    private final HashSet<Guid> mTriedVdssList = new HashSet<Guid>();
    private Guid mCurrentVdsId;

    private Guid preferredHostId;

    public Guid getCurrentVdsId() {
        return getIrsProxy() != null ? mCurrentVdsId : Guid.Empty;
    }

    public void setCurrentVdsId(Guid value) {
        mCurrentVdsId = (Guid.Empty.equals(value)) ? null : value;
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
        storagePoolRefreshJobId = SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(this,
                "_updatingTimer_Elapsed", new Class[0], new Object[0], storagePoolRefreshTime,
                storagePoolRefreshTime, TimeUnit.SECONDS);
        domainRecoverOnHostJobId =
                SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(this,
                        "hostsStorageConnectionsAndPoolMetadataRefresh",
                        new Class[0],
                        new Object[0],
                        Config.<Integer> getValue(ConfigValues.HostStorageConnectionAndPoolRefreshTimeInSeconds),
                        storagePoolRefreshTime,
                        TimeUnit.SECONDS);
    }

    @OnTimerMethodAnnotation("_updatingTimer_Elapsed")
    public void _updatingTimer_Elapsed() {
        try {
            synchronized (syncObj) {
                if (!_disposed) {
                    StoragePool storagePool = DbFacade.getInstance().getStoragePoolDao()
                            .get(_storagePoolId);

                    if (storagePool != null) {
                        // when there are no hosts in status up, it means that there shouldn't be domain monitoring
                        // so all the domains need to move to "unknown" status as otherwise their status won't change.
                        if (DbFacade.getInstance()
                                .getVdsDao()
                                .getAllForStoragePoolAndStatus(_storagePoolId, IrsBrokerCommand.reportingVdsStatus)
                                .isEmpty()) {
                            StoragePoolDomainHelper.updateApplicablePoolDomainsStatuses(_storagePoolId,
                                    StoragePoolDomainHelper.storageDomainMonitoredStatus,
                                    StorageDomainStatus.Unknown, "no reporting hosts");
                        }

                        if (storagePool.getStatus() == StoragePoolStatus.Up
                                || storagePool.getStatus() == StoragePoolStatus.NonResponsive || storagePool
                                        .getStatus() == StoragePoolStatus.Contend) {
                            proceedStoragePoolStats(storagePool);
                        }
                    }

                }
            }
        } catch (Exception ex) {
        }
    }

    private int _errorAttempts;

    private static Set<Guid> getVdsConnectedToPool(Guid storagePoolId) {
        Set<Guid> vdsNotInMaintenance = new HashSet<>();

        for (VDS vds : DbFacade.getInstance().getVdsDao().getAllForStoragePool(storagePoolId)) {
            if (vds.getStatus() == VDSStatus.Up
                    || vds.getStatus() == VDSStatus.NonResponsive
                    || vds.getStatus() == VDSStatus.PreparingForMaintenance
                    || vds.getStatus() == VDSStatus.NonOperational) {
                vdsNotInMaintenance.add(vds.getId());
            }
        }

        return vdsNotInMaintenance;
    }

    @SuppressWarnings("unchecked")
    private void proceedStoragePoolStats(StoragePool storagePool) {
        // ugly patch because vdsm doesnt check if host is spm on spm
        // operations
        VDSReturnValue result = null;
        Guid curVdsId = mCurrentVdsId;
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
                    ResourceManager
                            .getInstance()
                            .getEventListener()
                            .storagePoolStatusChange(_storagePoolId, StoragePoolStatus.NonResponsive,
                                    AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_PROBLEMATIC_WITH_ERROR,
                                    result.getVdsError().getCode());
                } else {
                    ResourceManager
                            .getInstance()
                            .getEventListener()
                            .storagePoolStatusChange(_storagePoolId, StoragePoolStatus.NonResponsive,
                                    AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_PROBLEMATIC,
                                    VdcBllErrors.ENGINE);
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
            int masterVersion = data.getKey().getmaster_domain_version();
            HashSet<Guid> domainsInVds = new HashSet<Guid>();
            for (StorageDomain domainData : data.getValue()) {
                domainsInVds.add(domainData.getId());
                proceedStorageDomain(domainData, masterVersion, storagePool);
            }
            for (final StorageDomain domainInDb : domainsInDb) {
                if (domainInDb.getStorageDomainType() != StorageDomainType.Master
                        && domainInDb.getStatus() != StorageDomainStatus.Locked
                        && !domainsInVds.contains(domainInDb.getId())) {
                    // domain not attached to pool anymore
                    DbFacade.getInstance()
                            .getStoragePoolIsoMapDao()
                            .remove(new StoragePoolIsoMapId(domainInDb.getId(),
                                    _storagePoolId));
                }

            }
        }
        for (final StorageDomain domainInDb : domainsInDb) {
            if (domainInDb.getStatus() == StorageDomainStatus.PreparingForMaintenance) {
                queueDomainMaintenanceCheck(domainInDb);
            }
        }
    }

    public void queueDomainMaintenanceCheck(final StorageDomain domain) {
        ((EventQueue) EjbUtils.findBean(BeanType.EVENTQUEUE_MANAGER, BeanProxyType.LOCAL))
                .submitEventAsync(new Event(_storagePoolId, domain.getId(), null, EventType.DOMAINFAILOVER, ""),
                        new Callable<EventResult>() {
                            @Override
                            public EventResult call() {
                                Set<Guid> vdsConnectedToPool = getVdsConnectedToPool(_storagePoolId);
                                Set<Guid> vdsDomInMaintenance = _domainsInMaintenance.get(domain.getId());
                                if (vdsConnectedToPool.isEmpty() ||
                                        (vdsDomInMaintenance != null &&
                                                vdsDomInMaintenance.containsAll(vdsConnectedToPool))) {
                                    log.info("Moving domain '{}' to maintenance", domain.getId());
                                    DbFacade.getInstance().getStoragePoolIsoMapDao().updateStatus(
                                            domain.getStoragePoolIsoMapData().getId(),
                                            StorageDomainStatus.Maintenance);
                                }
                                return null;
                            }
                        });
    }

    public Guid getPreferredHostId() {
        return preferredHostId;
    }

    public void setPreferredHostId(Guid preferredHostId) {
        this.preferredHostId = preferredHostId;
    }

    private void proceedStorageDomain(StorageDomain data, int dataMasterVersion, StoragePool storagePool) {
        StorageDomain storage_domain = DbFacade.getInstance().getStorageDomainDao().getForStoragePool(data.getId(), _storagePoolId);
        StorageDomainStatic domainFromDb = null;
        StoragePoolIsoMap domainPoolMap = null;

        if (storage_domain != null) {
            domainFromDb = storage_domain.getStorageStaticData();
            domainPoolMap = storage_domain.getStoragePoolIsoMapData();
            // If the domain is master in the DB
            if (domainFromDb.getStorageDomainType() == StorageDomainType.Master && domainPoolMap != null
                    && domainPoolMap.getStatus() != StorageDomainStatus.Locked) {
                // and the domain is not master in the VDSM
                if (!((data.getStorageDomainType() == StorageDomainType.Master) || (data.getStorageDomainType() == StorageDomainType.Unknown))) {
                    reconstructMasterDomainNotInSync(data.getStoragePoolId(),
                            domainFromDb.getId(),
                            "Mismatch between master in DB and VDSM",
                            MessageFormat.format("Master domain is not in sync between DB and VDSM. "
                                    + "Domain {0} marked as master in DB and not in the storage",
                                    domainFromDb.getStorageName()));
                } // or master in DB and VDSM but there is a version
                  // mismatch
                else if (dataMasterVersion != storagePool.getmaster_domain_version()) {
                    reconstructMasterDomainNotInSync(data.getStoragePoolId(),
                            domainFromDb.getId(),
                            "Mismatch between master version in DB and VDSM",
                            MessageFormat.format("Master domain version is not in sync between DB and VDSM. "
                                    + "Domain {0} marked as master, but the version in DB: {1} and in VDSM: {2}",
                                    domainFromDb.getStorageName(),
                                    storagePool.getmaster_domain_version(),
                                    dataMasterVersion));
                }
            }
            boolean statusChanged = false;
            if (domainPoolMap == null) {
                data.setStoragePoolId(_storagePoolId);
                DbFacade.getInstance().getStoragePoolIsoMapDao().save(data.getStoragePoolIsoMapData());
                statusChanged = true;
            } else if (domainPoolMap.getStatus() != StorageDomainStatus.Locked
                    && domainPoolMap.getStatus() != data.getStatus()) {
                if (domainPoolMap.getStatus() != StorageDomainStatus.Inactive
                        && data.getStatus() != StorageDomainStatus.Inactive) {
                    DbFacade.getInstance().getStoragePoolIsoMapDao().update(data.getStoragePoolIsoMapData());
                    statusChanged = true;
                }
                if (data.getStatus() != null && data.getStatus() == StorageDomainStatus.Inactive
                        && domainFromDb.getStorageDomainType() == StorageDomainType.Master) {
                    StoragePool pool = DbFacade.getInstance().getStoragePoolDao()
                            .get(domainPoolMap.getstorage_pool_id());
                    if (pool != null) {
                        DbFacade.getInstance().getStoragePoolDao().updateStatus(pool.getId(), StoragePoolStatus.Maintenance);
                        pool.setStatus(StoragePoolStatus.Maintenance);
                        ResourceManager.getInstance().getEventListener()
                                .storagePoolStatusChanged(pool.getId(), StoragePoolStatus.Maintenance);
                    }
                }
            }

            // For block domains, synchronize LUN details comprising the storage domain with the DB
            if (statusChanged && data.getStatus() == StorageDomainStatus.Active && storage_domain.getStorageType().isBlockDomain()) {
                ResourceManager.getInstance().getEventListener().syncLunsInfoForBlockStorageDomain(
                        data.getId(), getCurrentVdsId());
            }

            // if status didn't change and still not active no need to
            // update dynamic data
            if (statusChanged
                    || (domainPoolMap.getStatus() != StorageDomainStatus.Inactive && data.getStatus() == StorageDomainStatus.Active)) {
                DbFacade.getInstance().getStorageDomainDynamicDao().update(data.getStorageDynamicData());
                if (data.getAvailableDiskSize() != null && data.getUsedDiskSize() != null) {
                    double freePercent = data.getStorageDynamicData().getfreeDiskPercent();
                    int freeDiskInGB = data.getStorageDynamicData().getfreeDiskInGB();
                    AuditLogType type = AuditLogType.UNASSIGNED;
                    boolean spaceThresholdMet =
                            freeDiskInGB <= Config.<Integer> getValue(ConfigValues.FreeSpaceCriticalLowInGB);
                    boolean percentThresholdMet =
                            freePercent <= Config.<Integer> getValue(ConfigValues.FreeSpaceLow);
                    if (spaceThresholdMet && percentThresholdMet) {
                        type = AuditLogType.IRS_DISK_SPACE_LOW_ERROR;
                    } else {
                        if (spaceThresholdMet || percentThresholdMet) {
                            type = AuditLogType.IRS_DISK_SPACE_LOW;
                        }
                    }
                    if (type != AuditLogType.UNASSIGNED) {
                        AuditLogableBase logable = new AuditLogableBase();
                        logable.setStorageDomain(data);
                        logable.setStoragePoolId(_storagePoolId);
                        logable.addCustomValue("DiskSpace", (data.getAvailableDiskSize()).toString());
                        data.setStorageName(domainFromDb.getStorageName());
                        AuditLogDirector.log(logable, type);

                    }
                }

                Set<VdcBllErrors> alerts = data.getAlerts();
                if (alerts != null && !alerts.isEmpty()) {

                    AuditLogableBase logable = new AuditLogableBase();
                    logable.setStorageDomain(data);
                    data.setStorageName(domainFromDb.getStorageName());
                    logable.setStoragePoolId(_storagePoolId);

                    for (VdcBllErrors alert : alerts) {
                        switch (alert) {
                        case VG_METADATA_CRITICALLY_FULL:
                            AuditLogDirector.log(logable, AuditLogType.STORAGE_ALERT_VG_METADATA_CRITICALLY_FULL);
                            break;
                        case SMALL_VG_METADATA:
                            AuditLogDirector.log(logable, AuditLogType.STORAGE_ALERT_SMALL_VG_METADATA);
                            break;
                        default:
                            log.error("Unrecognized alert for domain {}(id = {}): {}",
                                    data.getStorageName(),
                                    data.getId(),
                                    alert);
                            break;
                        }
                    }
                }
            }

        } else {
            log.debug("The domain with id '{}' was not found in DB", data.getId());
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

        ((EventQueue) EjbUtils.findBean(BeanType.EVENTQUEUE_MANAGER, BeanProxyType.LOCAL)).submitEventSync(new Event(_storagePoolId,
                masterDomainId, null, EventType.RECONSTRUCT, "Reconstruct caused by failure to execute spm command"),
                new Callable<EventResult>() {
                    @Override
                    public EventResult call() {
                        log.warn(logMessage);

                        AuditLogableBase logable = new AuditLogableBase(mCurrentVdsId);
                        logable.setStorageDomainId(masterDomainId);
                        AuditLogDirector.log(logable, AuditLogType.SYSTEM_MASTER_DOMAIN_NOT_IN_SYNC);

                        return ResourceManager.getInstance()
                                .getEventListener()
                                .masterDomainNotOperational(masterDomainId, storagePoolId, false, true);

                    }
                });
        throw new IRSNoMasterDomainException(exceptionMessage);
    }

    public HashSet<Guid> getTriedVdssList() {
        return mTriedVdssList;
    }

    public void init(VDS vds) {
        mCurrentVdsId = vds.getId();
        setmIrsPort(vds.getPort());
        privatemCurrentIrsHost = vds.getHostName();
        setProtocol(vds.getProtocol());
    }

    public boolean failover() {
        Guid vdsId = mCurrentVdsId;
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
                // (in case mCurrentVdsId has wrong id for some reason)
                log.error("Could not get spm status on host '{}' for spmStop: {}", vdsId, ex.getMessage());
                log.debug("Exception", ex);
                performFailover = true;
            }
        }

        if (performFailover) {
            log.info("Irs placed on server '{}' failed. Proceed Failover", vdsId);
            mTriedVdssList.add(vdsId);
            return true;
        } else {
            log.error("IRS failover failed - cant allocate vds server");
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
                                new TransactionMethod<String>() {
                                    @Override
                                    public String runInTransaction() {
                                        return gethostFromVds();
                                    }
                                });

                if (host != null) {
                    // Get the values of the timeouts:
                    int clientTimeOut = Config.<Integer> getValue(ConfigValues.vdsTimeout) * 1000;
                    int connectionTimeOut = Config.<Integer> getValue(ConfigValues.vdsConnectionTimeout) * 1000;
                    int heartbeat = Config.<Integer> getValue(ConfigValues.vdsHeartbeatInSeconds) * 1000;
                    int clientRetries = Config.<Integer> getValue(ConfigValues.vdsRetries);
                    irsProxy = TransportFactory.createIrsServer(getProtocol(), host, getmIrsPort(), clientTimeOut, connectionTimeOut, clientRetries, heartbeat);
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
                    if (DbFacade.getInstance().isStoragePoolMasterUp(_storagePoolId)) {
                        ResourceManager.getInstance()
                                .getEventListener()
                                .storagePoolUpEvent(storagePool);
                    }
                } catch (RuntimeException exp) {
                    log.error("Error in StoragePoolUpEvent: {}", exp.getMessage());
                    log.debug("Exception", exp);
                }

            }
        });
    }

    /**
     * Returns True if there are other vdss in pool
     */
    public boolean getHasVdssForSpmSelection() {
        return (getPrioritizedVdsInPool().size() > 0);
    }

    private String gethostFromVds() {
        String returnValue = null;
        Guid curVdsId = (mCurrentVdsId != null) ? mCurrentVdsId : Guid.Empty;
        StoragePool storagePool = DbFacade.getInstance().getStoragePoolDao().get(_storagePoolId);

        if (storagePool == null) {
            log.info("hostFromVds::Finished elect spm, storage pool '{}' was removed", _storagePoolId);
            return null;
        }

        List<VDS> prioritizedVdsInPool = getPrioritizedVdsInPool();
        mCurrentVdsId = null;

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
                                VdcBllErrors.ENGINE, TransactionScopeOption.RequiresNew);
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
            mTriedVdssList.add(selectedVdsId);

            VDSReturnValue returnValueFromVds = ResourceManager.getInstance().runVdsCommand(
                    VDSCommandType.SpmStatus,
                    new SpmStatusVDSCommandParameters(selectedVds.getId(), _storagePoolId));
            spmStatus = (SpmStatusResult) returnValueFromVds.getReturnValue();
            if (spmStatus != null) {
                mCurrentVdsId = selectedVds.getId();
                boolean performedPoolConnect = false;
                log.info("hostFromVds::selectedVds - '{}', spmStatus '{}', storage pool '{}'",
                        selectedVds.getName(), spmStatus.getSpmStatus(), storagePool.getName());
                if (spmStatus.getSpmStatus() == SpmStatus.Unknown_Pool) {
                    Guid masterDomainId = DbFacade.getInstance().getStorageDomainDao()
                            .getMasterStorageDomainIdForPool(_storagePoolId);
                    List<StoragePoolIsoMap> storagePoolIsoMap = DbFacade.getInstance()
                            .getStoragePoolIsoMapDao().getAllForStoragePool(_storagePoolId);
                    VDSReturnValue connectResult = ResourceManager.getInstance().runVdsCommand(
                            VDSCommandType.ConnectStoragePool,
                            new ConnectStoragePoolVDSCommandParameters(selectedVds, storagePool,
                                    masterDomainId, storagePoolIsoMap));
                    if (!connectResult.getSucceeded()
                            && connectResult.getExceptionObject() instanceof IRSNoMasterDomainException) {
                        throw connectResult.getExceptionObject();
                    } else if (!connectResult.getSucceeded()) {
                        // if connect to pool fails throw exception for
                        // failover
                        throw new IRSNonOperationalException("Could not connect host to Data Center(Storage issue)");
                    }
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
                RefObject<VDS> tempRefObject = new RefObject<VDS>(selectedVds);
                spmStatus =
                        handleSpmStatusResult(curVdsId, prioritizedVdsInPool, storagePool, tempRefObject, spmStatus);
                selectedVds = tempRefObject.argvalue;

                if (selectedVds != null) {
                    RefObject<VDS> tempRefObject2 = new RefObject<VDS>(selectedVds);
                    RefObject<SpmStatusResult> tempRefObject3 = new RefObject<SpmStatusResult>(spmStatus);
                    returnValue = handleSelectedVdsForSPM(storagePool, tempRefObject2, tempRefObject3, prevStatus);
                    selectedVds = tempRefObject2.argvalue;
                    spmStatus = tempRefObject3.argvalue;
                } else {
                    mCurrentVdsId = null;
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
        Guid curVdsId = (mCurrentVdsId != null) ? mCurrentVdsId : Guid.Empty;
        // Gets a list of the hosts in the storagePool, that are "UP", ordered
        // by vds_spm_priority (not including -1) and secondly ordered by RANDOM(), to
        // deal with the case that there are several hosts with the same priority.
        List<VDS> allVds = DbFacade.getInstance().getVdsDao().getListForSpmSelection(_storagePoolId);
        List<VDS> vdsRelevantForSpmSelection = new ArrayList<VDS>();
        Guid preferredHost = IrsBrokerCommand.getIrsProxyData(_storagePoolId).getPreferredHostId();
        IrsBrokerCommand.getIrsProxyData(_storagePoolId).setPreferredHostId(null);

        for (VDS vds : allVds) {
            if (!mTriedVdssList.contains(vds.getId()) && !vds.getId().equals(curVdsId)) {
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
                    spmStatus.argvalue.getSpmStatus());
        } else {
            init(selectedVds.argvalue);
            storagePool.setLVER(spmStatus.argvalue.getSpmLVER());
            storagePool.setspm_vds_id(selectedVds.argvalue.getId());
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
            AuditLogDirector.log(logable, AuditLogType.IRS_HOSTED_ON_VDS);
        }
        return returnValue;
    }

    /**
     * Waits for VDS if is initializing.
     *
     * @param curVdsId
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
        ResourceManager
                .getInstance()
                .getEventListener()
                .storagePoolStatusChange(storagePool.getId(), StoragePoolStatus.NonResponsive,
                        AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_PROBLEMATIC, VdcBllErrors.ENGINE);

        storagePool.setspm_vds_id(null);
        DbFacade.getInstance().getStoragePoolDao().update(storagePool);
    }

    private boolean wasVdsManuallyFenced(int spmId) {
        vds_spm_id_map map = DbFacade.getInstance().getVdsSpmIdMapDao().get(
                _storagePoolId, spmId);
        return map != null && map.getId().equals(getFencedIrs());
    }

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
                vds_spm_id_map map = DbFacade.getInstance().getVdsSpmIdMapDao().get(
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
                storagePool.setStatus(StoragePoolStatus.Contend);
                storagePool.setspm_vds_id(selectedVds.argvalue.getId());

                TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
                    @Override
                    public Object runInTransaction() {
                        DbFacade.getInstance().getStoragePoolDao().update(storagePool);
                        return null;
                    }
                });

                log.info("starting spm on vds '{}', storage pool '{}', prevId '{}', LVER '{}'",
                        selectedVds.argvalue.getName(), storagePool.getName(), spmStatus.getSpmId(),
                        spmStatus.getSpmLVER());
                spmStatus = (SpmStatusResult) ResourceManager
                        .getInstance()
                        .runVdsCommand(
                                VDSCommandType.SpmStart,
                                new SpmStartVDSCommandParameters(selectedVds.argvalue.getId(), _storagePoolId,
                                        spmStatus.getSpmId(), spmStatus.getSpmLVER(), storagePool
                                                .getrecovery_mode(), vdsSpmIdToFence != -1, storagePool.getStoragePoolFormatType())).getReturnValue();
                if (spmStatus == null || spmStatus.getSpmStatus() != SpmStatus.SPM) {
                    ResourceManager
                            .getInstance()
                            .getEventListener()
                            .storagePoolStatusChange(storagePool.getId(),
                                    StoragePoolStatus.NonResponsive,
                                    AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_PROBLEMATIC,
                                    VdcBllErrors.ENGINE,
                                    TransactionScopeOption.RequiresNew);
                    if (spmStatus != null) {
                        TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
                            @Override
                            public Object runInTransaction() {
                                StoragePool pool =
                                        DbFacade.getInstance().getStoragePoolDao().get(storagePool.getId());
                                pool.setspm_vds_id(null);
                                DbFacade.getInstance().getStoragePoolDao().update(pool);
                                return null;
                            }
                        });
                    }
                    throw new IrsSpmStartFailedException();
                }
            }
        }
        return spmStatus;
    }

    public String getIsoDirectory() {
        String tempVar = privatemCurrentIrsHost;
        return String.format("\\\\%1$s\\CD", ((tempVar != null) ? tempVar : gethostFromVds()));
    }

    public void resetIrs() {
        nullifyInternalProxies();
        StoragePool storagePool = DbFacade.getInstance().getStoragePoolDao().get(_storagePoolId);
        if (storagePool != null) {
            storagePool.setspm_vds_id(null);
            DbFacade.getInstance().getStoragePoolDao().update(storagePool);
        }
    }

    private void nullifyInternalProxies() {
        if (irsProxy != null) {
            if (IrsServerWrapper.class.isInstance(irsProxy)) {
                XmlRpcUtils.shutDownConnection(((IrsServerWrapper) irsProxy).getHttpClient());
            } else {
                ((JsonRpcIIrsServer) irsProxy).close();
            }
        }
        privatemCurrentIrsHost = null;
        irsProxy = null;
        mCurrentVdsId = null;
    }

    private final Map<Guid, HashSet<Guid>> _domainsInProblem = new ConcurrentHashMap<Guid, HashSet<Guid>>();
    private final Map<Guid, HashSet<Guid>> _domainsInMaintenance = new ConcurrentHashMap<Guid, HashSet<Guid>>();
    private final Map<Guid, Guid> vdsReportsOnUnseenDomain = new ConcurrentHashMap<>();
    private final Map<Guid, Guid> vdsHandeledReportsOnUnseenDomains = new ConcurrentHashMap<>();
    private final Map<Guid, String> _timers = new HashMap<Guid, String>();

    public void updateVdsDomainsData(final Guid vdsId, final String vdsName,
                                     final ArrayList<VDSDomainsData> data) {

        Set<Guid> domainsInMaintenance = null;
        StoragePool storagePool =
                DbFacade.getInstance().getStoragePoolDao().get(_storagePoolId);
        if (storagePool != null
                && (storagePool.getStatus() == StoragePoolStatus.Up || storagePool.getStatus() == StoragePoolStatus.NonResponsive)) {

            try {
                Map<Guid, DomainMonitoringResult> domainsProblematicReportInfo = new HashMap<>();
                // build a list of all domains in pool
                // which are in status Active or Unknown
                Set<Guid> domainsInPool = new HashSet<Guid>(
                        DbFacade.getInstance().getStorageDomainStaticDao().getAllIds(
                                _storagePoolId, StorageDomainStatus.Active));
                domainsInPool.addAll(DbFacade.getInstance().getStorageDomainStaticDao().getAllIds(
                        _storagePoolId, StorageDomainStatus.Unknown));
                Set<Guid> inActiveDomainsInPool =
                        new HashSet<Guid>(DbFacade.getInstance()
                                .getStorageDomainStaticDao()
                                .getAllIds(_storagePoolId, StorageDomainStatus.Inactive));

                // build a list of all the domains in
                // pool (domainsInPool) that are not
                // visible by the host.
                Set<Guid> dataDomainIds = new HashSet<Guid>();
                for (VDSDomainsData tempData : data) {
                    dataDomainIds.add(tempData.getDomainId());
                }
                for (Guid tempDomainId : domainsInPool) {
                    if (!dataDomainIds.contains(tempDomainId)) {
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
                    if (domainsInPool.contains(tempData.getDomainId())) {
                        DomainMonitoringResult domainMonitoringResult = analyzeDomainReport(tempData, false);
                        if (domainMonitoringResult.invalid()) {
                            domainsProblematicReportInfo.put(tempData.getDomainId(), domainMonitoringResult);
                        } else if (tempData.getDelay() > Config.<Double> getValue(ConfigValues.MaxStorageVdsDelayCheckSec)) {
                            logDelayedDomain(vdsId, tempData);
                        }
                    } else if (inActiveDomainsInPool.contains(tempData.getDomainId())
                            && analyzeDomainReport(tempData, false).valid()) {
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

                Set<Guid> maintInPool = new HashSet<Guid>(
                        DbFacade.getInstance().getStorageDomainStaticDao().getAllIds(
                                _storagePoolId, StorageDomainStatus.Maintenance));
                maintInPool.addAll(DbFacade.getInstance().getStorageDomainStaticDao().getAllIds(
                        _storagePoolId, StorageDomainStatus.PreparingForMaintenance));

                domainsInMaintenance = new HashSet<Guid>();
                for (Guid tempDomainId : maintInPool) {
                    if (!dataDomainIds.contains(tempDomainId)) {
                        domainsInMaintenance.add(tempDomainId);
                    }
                }

                updateDomainInProblem(vdsId, vdsName, domainsProblematicReportInfo, domainsInMaintenance);
            } catch (RuntimeException ex) {
                log.error("error in updateVdsDomainsData: {}", ex.getMessage());
                log.debug("Exception", ex);
            }
        }
    }

    private void updateDomainInProblem(final Guid vdsId, final String vdsName, final Map<Guid, DomainMonitoringResult> domainsInProblem,
                                       final Set<Guid> domainsInMaintenance) {
        ((EventQueue) EjbUtils.findBean(BeanType.EVENTQUEUE_MANAGER, BeanProxyType.LOCAL)).submitEventSync(new Event(_storagePoolId,
                null, vdsId, EventType.DOMAINMONITORING, ""),
                new Callable<EventResult>() {
                    @Override
                    public EventResult call() {
                        EventResult result = new EventResult(true, EventType.DOMAINMONITORING);
                        updateProblematicVdsData(vdsId, vdsName, domainsInProblem);
                        updateMaintenanceVdsData(vdsId, vdsName, domainsInMaintenance);
                        return result;
                    }
                });
    }

    private void logDelayedDomain(final Guid vdsId, VDSDomainsData tempData) {
        AuditLogableBase logable = new AuditLogableBase();
        logable.setVdsId(vdsId);
        logable.setStorageDomainId(tempData.getDomainId());
        logable.addCustomValue("Delay",
                Double.toString(tempData.getDelay()));
        AuditLogDirector.log(logable,
                AuditLogType.VDS_DOMAIN_DELAY_INTERVAL);
    }

    protected List<Guid> obtainDomainsReportedAsProblematic(List<VDSDomainsData> vdsDomainsData) {
        List<Guid> domainsInProblem = new LinkedList<>();
        Set<Guid> domainsInPool = new HashSet<Guid>(
                DbFacade.getInstance().getStorageDomainStaticDao().getAllIds(
                        _storagePoolId, StorageDomainStatus.Active));
        domainsInPool.addAll(DbFacade.getInstance().getStorageDomainStaticDao().getAllIds(
                _storagePoolId, StorageDomainStatus.Unknown));
        List<Guid> domainWhichWereSeen = new ArrayList<Guid>();
        for (VDSDomainsData vdsDomainData : vdsDomainsData) {
            if (domainsInPool.contains(vdsDomainData.getDomainId())) {
                if (analyzeDomainReport(vdsDomainData, true).invalid()) {
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
        PROBLEMATIC(false), STORAGE_ACCCESS_ERROR(false), OK(true), NOT_REPORTED(false);

        private boolean valid;

        private DomainMonitoringResult(boolean valid) {
            this.valid = valid;
        }

        public boolean valid() {
            return valid;
        }

        public boolean invalid() {
            return !valid;
        }
    }

    private DomainMonitoringResult analyzeDomainReport(VDSDomainsData tempData, boolean isLog) {
        if (tempData.getCode() != 0) {
            if (isLog) {
                log.error("Domain '{}' was reported with error code '{}'",
                        getDomainIdTuple(tempData.getDomainId()),
                        tempData.getCode());
            }

            if (tempData.getCode() == VdcBllErrors.StorageDomainDoesNotExist.getValue()
                    || tempData.getCode() == VdcBllErrors.StorageException.getValue()) {
                return DomainMonitoringResult.STORAGE_ACCCESS_ERROR;
            }

            return DomainMonitoringResult.PROBLEMATIC;
        }
        if (tempData.getLastCheck() > Config
                .<Double> getValue(ConfigValues.MaxStorageVdsTimeoutCheckSec)) {
            if (isLog) {
                log.error("Domain '{}' check timeot '{}' is too big",
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
                updateDomainInProblemData(domainId, vdsId, vdsName);
            } else {
                if (domainNotFound) {
                    newDomainUnreachableByHost = true;
                }
                // new domains in problems
                addDomainInProblemData(domainId, vdsId, vdsName);
            }
        }

        if (domainsUnreachableByHost.isEmpty()) {
            Guid clearedReport = clearVdsReportInfoOnUnseenDomain(vdsId);
            if (clearedReport != null)
                log.info("Host '{}' no longer storage access problem to any relevant domain " +
                        " clearing it's report (report id: '{}')",
                        vdsId,
                        clearedReport);
        } else if (newDomainUnreachableByHost) {
            Guid newReportId = Guid.newGuid();
            log.info("Host '{}' has reported new storage access problem to the following domains '{}'" +
                    " marking it for storage connections and pool metadata refresh (report id: '{}')",
                    vdsId,
                    StringUtils.join(domainsUnreachableByHost, ","),
                    newReportId);
            vdsReportsOnUnseenDomain.put(vdsId, newReportId);
        }

        Set<Guid> notReportedDomainsByHost = new HashSet<Guid>(_domainsInProblem.keySet());
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

    private void addDomainInProblemData(Guid domainId, Guid vdsId, String vdsName) {
        _domainsInProblem.put(domainId, new HashSet<Guid>(Arrays.asList(vdsId)));
        log.warn("domain '{}' in problem. vds: '{}'", getDomainIdTuple(domainId), vdsName);
        Class[] inputType = new Class[] { Guid.class };
        Object[] inputParams = new Object[] { domainId };
        String jobId = SchedulerUtilQuartzImpl.getInstance().scheduleAOneTimeJob(this, "onTimer", inputType,
                inputParams, Config.<Integer> getValue(ConfigValues.StorageDomainFailureTimeoutInMinutes),
                TimeUnit.MINUTES);
        clearTimer(domainId);
        _timers.put(domainId, jobId);
    }

    @OnTimerMethodAnnotation("onTimer")
    public void onTimer(final Guid domainId) {
        ((EventQueue) EjbUtils.findBean(BeanType.EVENTQUEUE_MANAGER, BeanProxyType.LOCAL)).submitEventAsync(new Event(_storagePoolId,
                domainId, null, EventType.DOMAINFAILOVER, ""),
                new Callable<EventResult>() {
                    @Override
                    public EventResult call() {
                        EventResult result = null;
                        if (_domainsInProblem.containsKey(domainId)) {
                            log.info("starting processDomainRecovery for domain '{}'.", getDomainIdTuple(domainId));
                            result = processDomainRecovery(domainId);
                        }
                        _timers.remove(domainId);
                        return result;
                    }
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
                                VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED.toString()));
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

                connectStorageTasks.add(new Callable<Void>() {

                    @Override
                    public Void call() {
                        ResourceManager.getInstance()
                                .getEventListener().connectHostToDomainsInActiveOrUnknownStatus(vds);
                        return null;
                    }
                });

                refreshStoragePoolTasks.add(new Callable<Void>() {

                    @Override
                    public Void call() {
                        StoragePoolDomainHelper.refreshHostPoolMetadata(vds, storagePool, masterDomainId, storagePoolIsoMap);
                        return null;
                    }
                });
            }

            final Set<String> handledHosts = acquiredLocks.keySet();
            log.info("Running storage connections refresh for hosts '{}'", handledHosts);
            ThreadPoolUtil.invokeAll(connectStorageTasks);

            log.info("Submitting to the event queue pool refresh for hosts '{}'", handledHosts);
            ((EventQueue) EjbUtils.findBean(BeanType.EVENTQUEUE_MANAGER, BeanProxyType.LOCAL)).submitEventSync(new Event(_storagePoolId,
                    null,
                    null,
                    EventType.POOLREFRESH,
                    ""),
                    new Callable<EventResult>() {
                        @Override
                        public EventResult call() {
                            log.info("Running storage pool metadata refresh for hosts '{}'", handledHosts);
                            ThreadPoolUtil.invokeAll(refreshStoragePoolTasks);
                            return new EventResult(true, EventType.POOLREFRESH);
                        }
                    });
        } finally {
            if (!acquiredLocks.isEmpty()) {
                LockManagerFactory.getLockManager().releaseLock(new EngineLock(acquiredLocks, null));
            }
        }
    }

    private void updateDomainInProblemData(Guid domainId, Guid vdsId, String vdsName) {
        log.debug("domain '{}' still in problem. vds: '{}'", getDomainIdTuple(domainId), vdsName);
        _domainsInProblem.get(domainId).add(vdsId);
    }

    private EventResult processDomainRecovery(final Guid domainId) {
        EventResult result = null;
        // build a list of all the hosts in status UP in
        // Pool.
        List<Guid> vdssInPool = new ArrayList<Guid>();
        List<VDS> allVds = DbFacade.getInstance().getVdsDao().getAllForStoragePool(_storagePoolId);
        Map<Guid, VDS> vdsMap = new HashMap<Guid, VDS>();
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
        List<Guid> vdssInProblem = new ArrayList<Guid>();
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
        List<Guid> nonOpVdss = new ArrayList<Guid>();
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

                        ThreadPoolUtil.execute(new Runnable() {
                            @Override
                            public void run() {
                                ResourceManager
                                        .getInstance()
                                        .getEventListener()
                                        .vdsNonOperational(vdsId, NonOperationalReason.STORAGE_DOMAIN_UNREACHABLE,
                                                true, domainId);
                            }
                        });

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
            SchedulerUtilQuartzImpl.getInstance().deleteJob(jobId);
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
                SchedulerUtilQuartzImpl.getInstance().deleteJob(jobId);
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
        synchronized (syncObj) {
            log.info("IrsProxyData::disposing");
            resetIrs();
            SchedulerUtilQuartzImpl.getInstance().deleteJob(storagePoolRefreshJobId);
            SchedulerUtilQuartzImpl.getInstance().deleteJob(domainRecoverOnHostJobId);
            _disposed = true;
        }
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

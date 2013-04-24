package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.SocketException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.SpmStatus;
import org.ovirt.engine.core.common.businessentities.SpmStatusResult;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSDomainsData;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.vds_spm_id_map;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.eventqueue.Event;
import org.ovirt.engine.core.common.eventqueue.EventQueue;
import org.ovirt.engine.core.common.eventqueue.EventResult;
import org.ovirt.engine.core.common.eventqueue.EventType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.DisconnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetStoragePoolInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
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
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.ovirt.engine.core.utils.log.LoggedUtils;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.BrokerCommandBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSExceptionBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSNetworkException;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcRunTimeException;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcUtils;

@Logged(errorLevel = LogLevel.ERROR)
public abstract class IrsBrokerCommand<P extends IrsBaseVDSCommandParameters> extends BrokerCommandBase<P> {
    public static final long BYTES_TO_GB = 1024 * 1024 * 1024;
    private static Map<Guid, IrsProxyData> _irsProxyData = new ConcurrentHashMap<Guid, IrsProxyData>();

    public static void UpdateVdsDomainsData(Guid vdsId, String vdsName, Guid storagePoolId,
            java.util.ArrayList<VDSDomainsData> vdsDomainData) {
        IrsProxyData proxy = _irsProxyData.get(storagePoolId);
        if (proxy != null) {
            proxy.UpdateVdsDomainsData(vdsId, vdsName, vdsDomainData);
        }
    }

    public static boolean isDomainsReportedAsProblematic(Guid storagePoolId, List<VDSDomainsData> vdsDomainsData) {
        IrsProxyData proxy = _irsProxyData.get(storagePoolId);
        if (proxy != null) {
            return proxy.isDomainsReportedAsProblematic(vdsDomainsData);
        }
        return false;
    }

    @Override
    protected VDSExceptionBase createDefaultConcreteException(String errorMessage) {
        return new IRSErrorException(errorMessage);
    }

    public static void Init() {
        for (StoragePool sp : DbFacade.getInstance().getStoragePoolDao().getAll()) {
            if (!_irsProxyData.containsKey(sp.getId())) {
                _irsProxyData.put(sp.getId(), new IrsProxyData(sp.getId()));
            }
        }
    }

    public void RemoveIrsProxy() {
        _irsProxyData.get(getParameters().getStoragePoolId()).Dispose();
        _irsProxyData.remove(getParameters().getStoragePoolId());
    }

    protected static class IrsProxyData {
        // TODO the syncObj initial purposs was to lock the IrsBroker creation
        // but eventually because the IRS is singlethreaded and suppose to have
        // quite a load of requests,
        // In order to avoid unexpected behaviour we have used the syncObj to
        // lock each request to the IRS
        // and by that we caused a searialization of requests to the IRS.
        // This lock should be removed as soon as the IrsBroker is turned
        // multithreaded
        public Object syncObj = new Object();

        private final String storagePoolRefreshJobId;
        private final java.util.HashSet<Guid> mTriedVdssList = new java.util.HashSet<Guid>();
        private Guid mCurrentVdsId;

        public Guid getCurrentVdsId() {
            return getIrsProxy() != null ? mCurrentVdsId : Guid.Empty;
        }

        public void setCurrentVdsId(Guid value) {
            mCurrentVdsId = (Guid.Empty.equals(value)) ? null : value;
        }

        private String privatemCurrentIrsHost;
        private IIrsServer privatemIrsProxy;

        private IIrsServer getmIrsProxy() {
            return privatemIrsProxy;
        }

        private int privatemIrsPort;

        private int getmIrsPort() {
            return privatemIrsPort;
        }

        private void setmIrsPort(int value) {
            privatemIrsPort = value;
        }

        private Guid _storagePoolId = Guid.Empty;
        private String mIsoPrefix = "";

        public IrsProxyData(Guid storagePoolId) {
            _storagePoolId = storagePoolId;
            int storagePoolRefreshTime = Config.<Integer> GetValue(ConfigValues.StoragePoolRefreshTimeInSeconds);
            storagePoolRefreshJobId = SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(this,
                    "_updatingTimer_Elapsed", new Class[0], new Object[0], storagePoolRefreshTime,
                    storagePoolRefreshTime, TimeUnit.SECONDS);
        }

        @OnTimerMethodAnnotation("_updatingTimer_Elapsed")
        public void _updatingTimer_Elapsed() {
            try {
                synchronized (syncObj) {
                    if (!_disposed) {
                        StoragePool storagePool = DbFacade.getInstance().getStoragePoolDao()
                                .get(_storagePoolId);
                        if (storagePool != null
                                && (storagePool.getstatus() == StoragePoolStatus.Up
                                        || storagePool.getstatus() == StoragePoolStatus.Problematic || storagePool
                                        .getstatus() == StoragePoolStatus.Contend)) {
                            proceedStoragePoolStats(storagePool);
                        }

                    }
                }
            } catch (java.lang.Exception ex) {
            }
        }

        private int _errorAttempts;

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
                if (storagePool.getstatus() != StoragePoolStatus.Problematic
                        && storagePool.getstatus() != StoragePoolStatus.NotOperational) {
                    if (result != null && result.getVdsError() != null) {
                        ResourceManager
                                .getInstance()
                                .getEventListener()
                                .storagePoolStatusChange(_storagePoolId, StoragePoolStatus.Problematic,
                                        AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_PROBLEMATIC_WITH_ERROR,
                                        result.getVdsError().getCode());
                    } else {
                        ResourceManager
                                .getInstance()
                                .getEventListener()
                                .storagePoolStatusChange(_storagePoolId, StoragePoolStatus.Problematic,
                                        AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_PROBLEMATIC,
                                        VdcBllErrors.ENGINE);
                    }
                }

                // if spm status didnt work or not spm and NOT NETWORK
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
                        if (_errorAttempts < Config.<Integer> GetValue(ConfigValues.SPMFailOverAttempts)) {
                            _errorAttempts++;
                            log.warnFormat("failed getting spm status for pool {0}:{1}, attempt number {2}",
                                    _storagePoolId, storagePool.getname(), _errorAttempts);
                        } else {
                            nullifyInternalProxies();
                            _errorAttempts = 0;
                        }
                    }
                }
            } else if (result != null
                    && result.getSucceeded()
                    && ((SpmStatusResult) result.getReturnValue()).getSpmStatus() == SpmStatus.SPM
                    && (storagePool.getstatus() == StoragePoolStatus.Problematic || storagePool.getstatus() == StoragePoolStatus.Contend)) {
                // if recovered from network exception set back to up
                DbFacade.getInstance().getStoragePoolDao().updateStatus(storagePool.getId(), StoragePoolStatus.Up);
                storagePool.setstatus(StoragePoolStatus.Up);
                ResourceManager.getInstance().getEventListener()
                        .storagePoolStatusChanged(storagePool.getId(), storagePool.getstatus());
            }
            GetStoragePoolInfoVDSCommandParameters tempVar = new GetStoragePoolInfoVDSCommandParameters(
                    _storagePoolId);
            tempVar.setIgnoreFailoverLimit(true);
            VDSReturnValue storagePoolInfoResult = ResourceManager.getInstance().runVdsCommand(
                    VDSCommandType.GetStoragePoolInfo, tempVar);
            if (storagePoolInfoResult.getSucceeded()) {
                KeyValuePairCompat<StoragePool, java.util.List<StorageDomain>> data =
                        (KeyValuePairCompat<StoragePool, java.util.List<StorageDomain>>) storagePoolInfoResult
                                .getReturnValue();
                int masterVersion = data.getKey().getmaster_domain_version();
                java.util.HashSet<Guid> domainsInVds = new java.util.HashSet<Guid>();
                for (StorageDomain domainData : data.getValue()) {
                    domainsInVds.add(domainData.getId());
                    proceedStorageDomain(domainData, masterVersion, storagePool);
                }
                List<StorageDomain> domainsInDb = DbFacade.getInstance().getStorageDomainDao()
                        .getAllForStoragePool(_storagePoolId);

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
                        && domainPoolMap.getstatus() != StorageDomainStatus.Locked) {
                    // and the domain is not master in the VDSM
                    if (!((data.getStorageDomainType() == StorageDomainType.Master) || (data.getStorageDomainType() == StorageDomainType.Unknown))) {
                        reconstructMasterDomainNotInSync(data.getStoragePoolId().getValue(),
                                domainFromDb.getId(),
                                "Mismatch between master in DB and VDSM",
                                MessageFormat.format("Master domain is not in sync between DB and VDSM. "
                                        + "Domain {0} marked as master in DB and not in the storage",
                                        domainFromDb.getStorageName()));
                    } // or master in DB and VDSM but there is a version
                      // mismatch
                    else if (dataMasterVersion != storagePool.getmaster_domain_version()) {
                        reconstructMasterDomainNotInSync(data.getStoragePoolId().getValue(),
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
                } else if (domainPoolMap.getstatus() != StorageDomainStatus.Locked
                        && domainPoolMap.getstatus() != data.getStatus()) {
                    if (domainPoolMap.getstatus() != StorageDomainStatus.InActive
                            && data.getStatus() != StorageDomainStatus.InActive) {
                        DbFacade.getInstance().getStoragePoolIsoMapDao().update(data.getStoragePoolIsoMapData());
                        statusChanged = true;
                    }
                    if (data.getStatus() != null && data.getStatus() == StorageDomainStatus.InActive
                            && domainFromDb.getStorageDomainType() == StorageDomainType.Master) {
                        StoragePool pool = DbFacade.getInstance().getStoragePoolDao()
                                .get(domainPoolMap.getstorage_pool_id().getValue());
                        if (pool != null) {
                            DbFacade.getInstance().getStoragePoolDao().updateStatus(pool.getId(),StoragePoolStatus.Maintenance);
                            pool.setstatus(StoragePoolStatus.Maintenance);
                            ResourceManager.getInstance().getEventListener()
                                    .storagePoolStatusChanged(pool.getId(), StoragePoolStatus.Maintenance);
                        }
                    }
                }
                // if status didn't change and still not active no need to
                // update dynamic data
                if (statusChanged
                        || (domainPoolMap.getstatus() != StorageDomainStatus.InActive && data.getStatus() == StorageDomainStatus.Active)) {
                    DbFacade.getInstance().getStorageDomainDynamicDao().update(data.getStorageDynamicData());
                    if (data.getAvailableDiskSize() != null && data.getUsedDiskSize() != null) {
                        double freePercent = data.getStorageDynamicData().getfreeDiskPercent();
                        int freeDiskInGB = data.getStorageDynamicData().getfreeDiskInGB();
                        AuditLogType type = AuditLogType.UNASSIGNED;
                        boolean spaceThresholdMet =
                                freeDiskInGB <= Config.<Integer> GetValue(ConfigValues.FreeSpaceCriticalLowInGB);
                        boolean percentThresholdMet =
                                freePercent <= Config.<Integer> GetValue(ConfigValues.FreeSpaceLow);
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
                                log.errorFormat("Unrecognized alert for domain {0}(id = {1}): {2}",
                                        data.getStorageName(),
                                        data.getId(),
                                        alert);
                                break;
                            }
                        }
                    }
                }

            } else {
                log.debugFormat("The domain with id {0} was not found in DB", data.getId());
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
                    masterDomainId, null, EventType.RECONSTRUCT),
                    new Callable<EventResult>() {
                        @Override
                        public EventResult call() {
                            log.warnFormat(logMessage);

                            AuditLogableBase logable = new AuditLogableBase(mCurrentVdsId);
                            logable.setStorageDomainId(masterDomainId);
                            AuditLogDirector.log(logable, AuditLogType.SYSTEM_MASTER_DOMAIN_NOT_IN_SYNC);

                            return ResourceManager.getInstance()
                                    .getEventListener()
                                    .masterDomainNotOperational(masterDomainId, storagePoolId);

                        }
                    });
            throw new IRSNoMasterDomainException(exceptionMessage);
        }

        public java.util.HashSet<Guid> getTriedVdssList() {
            return mTriedVdssList;
        }

        public void Init(VDS vds) {
            mCurrentVdsId = vds.getId();
            setmIrsPort(vds.getPort());
            privatemCurrentIrsHost = vds.getHostName();
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
                    log.errorFormat("Could not get spm status on host {0} for spmStop.", vdsId);
                    performFailover = true;
                }
            }

            if (performFailover) {
                log.infoFormat("Irs placed on server {0} failed. Proceed Failover", vdsId);
                mTriedVdssList.add(vdsId);
                return true;
            } else {
                log.errorFormat("IRS failover failed - cant allocate vds server");
                return false;
            }
        }

        public IIrsServer getIrsProxy() {
            if (getmIrsProxy() == null) {
                final StoragePool storagePool = DbFacade.getInstance().getStoragePoolDao().get(_storagePoolId);
                // don't try to start spm on uninitialized pool
                if (storagePool.getstatus() != StoragePoolStatus.Uninitialized) {
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
                        int clientTimeOut = Config.<Integer> GetValue(ConfigValues.vdsTimeout) * 1000;
                        int connectionTimeOut = Config.<Integer>GetValue(ConfigValues.vdsConnectionTimeout) * 1000;
                        int clientRetries = Config.<Integer> GetValue(ConfigValues.vdsRetries);

                        Pair<IrsServerConnector, HttpClient> returnValue =
                                XmlRpcUtils.getConnection(host,
                                        getmIrsPort(),
                                        clientTimeOut,
                                        connectionTimeOut,
                                        clientRetries,
                                        IrsServerConnector.class,
                                        Config.<Boolean> GetValue(ConfigValues.UseSecureConnectionWithServers));
                        privatemIrsProxy = new IrsServerWrapper(returnValue.getFirst(), returnValue.getSecond());
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
                                    .storagePoolUpEvent(storagePool, _isSpmStartCalled);
                        }
                    } catch (RuntimeException exp) {
                        log.error("Error in StoragePoolUpEvent - ", exp);
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
            _isSpmStartCalled = false;
            String returnValue = null;
            Guid curVdsId = (mCurrentVdsId != null) ? mCurrentVdsId : Guid.Empty;
            StoragePool storagePool = DbFacade.getInstance().getStoragePoolDao().get(_storagePoolId);

            if (storagePool == null) {
                log.infoFormat("hostFromVds::Finished elect spm, storage pool {0} was removed", _storagePoolId);
                return null;
            }

            List<VDS> prioritizedVdsInPool = getPrioritizedVdsInPool();
            mCurrentVdsId = null;

            // If VDS is in initialize status, wait for it to be up (or until
            // configurable timeout is reached)
            waitForVdsIfIsInitializing(curVdsId);
            // update pool status to problematic while selecting spm
            StoragePoolStatus prevStatus = storagePool.getstatus();
            if (prevStatus != StoragePoolStatus.Problematic) {
                try {
                    ResourceManager
                            .getInstance()
                            .getEventListener()
                            .storagePoolStatusChange(_storagePoolId, StoragePoolStatus.Problematic,
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
                    log.infoFormat("hostFromVds::selectedVds - {0}, spmStatus {1}, storage pool {2}",
                            selectedVds.getName(), spmStatus.getSpmStatus().toString(), storagePool.getname());
                    if (spmStatus.getSpmStatus() == SpmStatus.Unknown_Pool) {
                        Guid masterId = DbFacade.getInstance().getStorageDomainDao()
                                .getMasterStorageDomainIdForPool(_storagePoolId);
                        VDSReturnValue connectResult = ResourceManager.getInstance().runVdsCommand(
                                VDSCommandType.ConnectStoragePool,
                                new ConnectStoragePoolVDSCommandParameters(selectedVds.getId(), _storagePoolId,
                                        selectedVds.getVdsSpmId(), masterId, storagePool.getmaster_domain_version()));
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
                        log.infoFormat(
                                "hostFromVds::Connected host to pool - selectedVds - {0}, spmStatus {1}, storage pool {2}",
                                selectedVds.getName(),
                                spmStatus.getSpmStatus().toString(),
                                storagePool.getname());
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

                    log.infoFormat("hostFromVds::selectedVds - {0}, spmStatus returned null!",
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
            for (VDS vds : allVds) {
                if (!mTriedVdssList.contains(vds.getId()) && !vds.getId().equals(curVdsId)) {
                    vdsRelevantForSpmSelection.add(vds);
                }
            }
            return vdsRelevantForSpmSelection;
        }

        private boolean _isSpmStartCalled;

        private String handleSelectedVdsForSPM(StoragePool storagePool, RefObject<VDS> selectedVds,
                                               RefObject<SpmStatusResult> spmStatus, StoragePoolStatus prevStatus) {
            String returnValue = null;
            if (spmStatus.argvalue == null || spmStatus.argvalue.getSpmStatus() != SpmStatus.SPM) {
                movePoolToProblematicInDB(storagePool);

                selectedVds.argvalue = null;
                log.infoFormat(
                        "spm start treatment ended and status is not SPM!!! status: {0} - setting selectedVds to null!",
                        spmStatus.argvalue.getSpmStatus().toString());
            } else {
                Init(selectedVds.argvalue);
                storagePool.setLVER(spmStatus.argvalue.getSpmLVER());
                storagePool.setspm_vds_id(selectedVds.argvalue.getId());
                // if were problemtaic or not operational and succeeded to find
                // host move pool to up
                if (prevStatus != StoragePoolStatus.NotOperational && prevStatus != StoragePoolStatus.Problematic) {
                    storagePool.setstatus(prevStatus);
                } else {
                    storagePool.setstatus(StoragePoolStatus.Up);
                }
                DbFacade.getInstance().getStoragePoolDao().update(storagePool);
                ResourceManager.getInstance()
                        .getEventListener()
                        .storagePoolStatusChanged(storagePool.getId(), storagePool.getstatus());

                returnValue = selectedVds.argvalue.getHostName();
                log.infoFormat("Initialize Irs proxy from vds: {0}", returnValue);
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
                    Integer maxSecToWait = Config.GetValue(ConfigValues.WaitForVdsInitInSec);
                    while (total <= maxSecToWait
                            && DbFacade.getInstance().getVdsDynamicDao().get(curVdsId).getstatus() == VDSStatus.Initializing) {
                        try {
                            Thread.sleep(DELAY * 1000);
                        } catch (InterruptedException e) {
                            log.errorFormat("Interrupt exception {0}", e.getMessage());
                            // exit the while block
                            break;
                        }
                        total += DELAY;
                        log.infoFormat("Waiting to Host {0} to finish initialization for {1} Sec.", vdsName, total);
                    }
                }
            }
        }

        private void movePoolToProblematicInDB(StoragePool storagePool) {
            ResourceManager
                    .getInstance()
                    .getEventListener()
                    .storagePoolStatusChange(storagePool.getId(), StoragePoolStatus.Problematic,
                            AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_PROBLEMATIC, VdcBllErrors.ENGINE);

            storagePool.setspm_vds_id(null);
            DbFacade.getInstance().getStoragePoolDao().update(storagePool);
        }

        private SpmStatusResult handleSpmStatusResult(Guid curVdsId,
                                                      List<VDS> vdsByPool,
                                                      final StoragePool storagePool,
                                                      RefObject<VDS> selectedVds,
                                                      SpmStatusResult spmStatus) {
            if (spmStatus.getSpmStatus() == SpmStatus.Free) {
                int vdsSpmIdToFence = -1;
                boolean startSpm = true;
                if (spmStatus.getSpmId() != -1) {
                    int spmId = spmStatus.getSpmId();
                    Guid spmVdsId = Guid.Empty;
                    VDS spmVds = null;
                    if (selectedVds.argvalue.getVdsSpmId() == spmId) {
                        spmVdsId = selectedVds.argvalue.getId();
                    } else {
                        for (VDS tempVds : vdsByPool) {
                            if (tempVds.getVdsSpmId() == spmId) {
                                log.infoFormat("Found spm host {0}, host name: {1}, according to spmId: {2}.",
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
                            log.infoFormat("SpmStatus on vds {0}: {1}", spmVdsId, destSpmStatus == null ? "NULL"
                                    : destSpmStatus.getSpmStatus().toString());
                            /**
                             * intentional unreachable code
                             */
                            if (destSpmStatus != null && destSpmStatus.getSpmStatus() == SpmStatus.SPM) {
                                if (spmVdsId != selectedVds.argvalue.getId() && spmVds != null
                                        && spmVds.getStatus() == VDSStatus.Up) {
                                    selectedVds.argvalue = spmVds;
                                    startSpm = false;
                                    log.infoFormat("Using old spm server: {0}, no start needed", spmVds.getName());
                                    return destSpmStatus;
                                }
                                // VDS is non-operational and SPM
                                else {
                                    log.warn("Host reports to be SPM but is not up. " + spmVdsId);
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
                            log.errorFormat(
                                    "SPM Init: could not find reported vds or not up - pool:{0} vds_spm_id: {1}",
                                    storagePool.getname(), spmStatus.getSpmId());
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
                            log.infoFormat("SPM selection - vds seems as spm {0}", vdsToFenceObject.getName());
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
                    storagePool.setstatus(StoragePoolStatus.Contend);
                    storagePool.setspm_vds_id(selectedVds.argvalue.getId());

                    TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
                        @Override
                        public Object runInTransaction() {
                            DbFacade.getInstance().getStoragePoolDao().update(storagePool);
                            return null;
                        }
                    });

                    log.infoFormat("starting spm on vds {0}, storage pool {1}, prevId {2}, LVER {3}",
                            selectedVds.argvalue.getName(), storagePool.getname(), spmStatus.getSpmId(),
                            spmStatus.getSpmLVER());
                    spmStatus = (SpmStatusResult) ResourceManager
                            .getInstance()
                            .runVdsCommand(
                                    VDSCommandType.SpmStart,
                                    new SpmStartVDSCommandParameters(selectedVds.argvalue.getId(), _storagePoolId,
                                            spmStatus.getSpmId(), spmStatus.getSpmLVER(), storagePool
                                                    .getrecovery_mode(), vdsSpmIdToFence != -1, storagePool.getStoragePoolFormatType())).getReturnValue();
                    if (spmStatus != null && spmStatus.getSpmStatus() == SpmStatus.SPM) {
                        _isSpmStartCalled = true;
                    } else {
                        ResourceManager
                                .getInstance()
                                .getEventListener()
                                .storagePoolStatusChange(storagePool.getId(),
                                        StoragePoolStatus.Problematic,
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

        public String getIsoPrefix() {
            synchronized (syncObj) {
                if (mIsoPrefix == null || mIsoPrefix.length() == 0) {
                    try {
                        StoragePoolInfoReturnForXmlRpc returnValue = getIrsProxy().getStoragePoolInfo(
                                _storagePoolId.toString());
                        if (returnValue.mStoragePoolInfo.containsKey(IrsProperties.isoPrefix)) {
                            mIsoPrefix = returnValue.mStoragePoolInfo.get(IrsProperties.isoPrefix).toString();
                        }
                    } catch (Exception ex) {
                        log.errorFormat("IrsBroker::IsoPrefix Failed to get IRS statistics.");
                    }
                }
                return mIsoPrefix;
            }
        }

        public void setIsoPrefix(String value) {
            mIsoPrefix = value;
        }

        public String getIsoDirectory() {
            String tempVar = privatemCurrentIrsHost;
            return String.format("\\\\%1$s\\CD", ((tempVar != null) ? tempVar : gethostFromVds()));
        }

        public void ResetIrs() {
            nullifyInternalProxies();
            StoragePool storagePool = DbFacade.getInstance().getStoragePoolDao().get(_storagePoolId);
            if (storagePool != null) {
                storagePool.setspm_vds_id(null);
                DbFacade.getInstance().getStoragePoolDao().update(storagePool);
            }
        }

        private void nullifyInternalProxies() {
            if (privatemIrsProxy != null) {
                XmlRpcUtils.shutDownConnection(((IrsServerWrapper) privatemIrsProxy).getHttpClient());
            }
            privatemCurrentIrsHost = null;
            privatemIrsProxy = null;
            mCurrentVdsId = null;
        }

        private final Map<Guid, HashSet<Guid>> _vdssInProblem = new HashMap<Guid, HashSet<Guid>>();
        private final Map<Guid, HashSet<Guid>> _domainsInProblem = new ConcurrentHashMap<Guid, HashSet<Guid>>();
        private final Map<Guid, String> _timers = new HashMap<Guid, String>();

        public void UpdateVdsDomainsData(final Guid vdsId, final String vdsName,
                final ArrayList<VDSDomainsData> data) {

            Set<Guid> domainsInProblems = null;
            StoragePool storagePool =
                    DbFacade.getInstance().getStoragePoolDao().get(_storagePoolId);
            if (storagePool != null
                    && (storagePool.getstatus() == StoragePoolStatus.Up || storagePool.getstatus() == StoragePoolStatus.Problematic)) {

                try {
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
                                    .getAllIds(_storagePoolId, StorageDomainStatus.InActive));

                    // build a list of all the domains in
                    // pool (domainsInPool) that are not
                    // visible by the host.
                    List<Guid> domainsInPoolThatNonVisibleByVds = new ArrayList<Guid>();
                    Set<Guid> dataDomainIds = new HashSet<Guid>();
                    for (VDSDomainsData tempData : data) {
                        dataDomainIds.add(tempData.getDomainId());
                    }
                    for (Guid tempDomainId : domainsInPool) {
                        if (!dataDomainIds.contains(tempDomainId)) {
                            domainsInPoolThatNonVisibleByVds.add(tempDomainId);
                        }
                    }

                    // build a list of domains that the host
                    // reports as in problem (code!=0) or (code==0
                    // && lastChecl >
                    // ConfigValues.MaxStorageVdsTimeoutCheckSec)
                    // and are contained in the Active or
                    // Unknown domains in pool
                    List<Guid> domainsSeenByVdsInProblem = new ArrayList<Guid>();
                    for (VDSDomainsData tempData : data) {
                        if (domainsInPool.contains(tempData.getDomainId())) {
                            if (isDomainReportedAsProblematic(tempData, false)) {
                                domainsSeenByVdsInProblem.add(tempData.getDomainId());
                            } else if (tempData.getDelay() > Config.<Double> GetValue(ConfigValues.MaxStorageVdsDelayCheckSec)) {
                                logDelayedDomain(vdsId, tempData);
                            }
                        } else if (inActiveDomainsInPool.contains(tempData.getDomainId())
                                && !isDomainReportedAsProblematic(tempData, false)) {
                            log.warnFormat("Storage Domain {0} was reported by Host {1} as Active in Pool {2}, moving to active status",
                                    getDomainIdTuple(tempData.getDomainId()),
                                    vdsName,
                                    _storagePoolId);
                            StoragePoolIsoMap map =
                                    DbFacade.getInstance()
                                            .getStoragePoolIsoMapDao()
                                            .get(new StoragePoolIsoMapId(tempData.getDomainId(), _storagePoolId));
                            map.setstatus(StorageDomainStatus.Active);
                            DbFacade.getInstance().getStoragePoolIsoMapDao().update(map);
                        }
                    }

                    // build a list of all potential domains
                    // in problem
                    domainsInProblems = new HashSet<Guid>();
                    domainsInProblems.addAll(domainsInPoolThatNonVisibleByVds);
                    domainsInProblems.addAll(domainsSeenByVdsInProblem);

                } catch (RuntimeException ex) {
                    log.error("error in UpdateVdsDomainsData", ex);
                }

            }
            updateDomainInProblem(vdsId, vdsName, domainsInProblems);
        }

        private void updateDomainInProblem(final Guid vdsId, final String vdsName, final Set<Guid> domainsInProblems) {
            if (domainsInProblems != null) {
                ((EventQueue) EjbUtils.findBean(BeanType.EVENTQUEUE_MANAGER, BeanProxyType.LOCAL)).submitEventSync(new Event(_storagePoolId,
                        null, vdsId, EventType.DOMAINMONITORING),
                        new Callable<EventResult>() {
                            @Override
                            public EventResult call() {
                                EventResult result = new EventResult(true, EventType.DOMAINMONITORING);
                                updateProblematicVdsData(vdsId, vdsName, domainsInProblems);
                                return result;
                            }
                        });
            }
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

        public boolean isDomainsReportedAsProblematic(List<VDSDomainsData> vdsDomainsData) {
            Set<Guid> domainsInPool = new HashSet<Guid>(
                    DbFacade.getInstance().getStorageDomainStaticDao().getAllIds(
                            _storagePoolId, StorageDomainStatus.Active));
            domainsInPool.addAll(DbFacade.getInstance().getStorageDomainStaticDao().getAllIds(
                    _storagePoolId, StorageDomainStatus.Unknown));
            List<Guid> domainWhicWereSeen = new ArrayList<Guid>();
            for (VDSDomainsData vdsDomainData : vdsDomainsData) {
                if (domainsInPool.contains(vdsDomainData.getDomainId())) {
                    if (isDomainReportedAsProblematic(vdsDomainData, true)) {
                        return true;
                    }
                    domainWhicWereSeen.add(vdsDomainData.getDomainId());
                }
            }
            domainsInPool.removeAll(domainWhicWereSeen);
            if (domainsInPool.size() > 0) {
                for (Guid domainId : domainsInPool) {
                    log.errorFormat("Domain {0} is not seen by Host", domainId);
                }
                return true;
            }
            return false;
        }

        private boolean isDomainReportedAsProblematic(VDSDomainsData tempData, boolean isLog) {
            if (tempData.getCode() != 0) {
                if (isLog) {
                    log.errorFormat("Domain {0} was reported with error code {1}",
                            getDomainIdTuple(tempData.getDomainId()),
                            tempData.getCode());
                }
                return true;
            }
            if (tempData.getLastCheck() > Config
                    .<Double> GetValue(ConfigValues.MaxStorageVdsTimeoutCheckSec)) {
                if (isLog) {
                    log.errorFormat("Domain {0} check timeot {1} is too big",
                            getDomainIdTuple(tempData.getDomainId()),
                            tempData.getLastCheck());
                }
                return true;
            }
            return false;
        }

        private void updateProblematicVdsData(final Guid vdsId, final String vdsName, Set<Guid> domainsInProblems) {
            // for all recovered domains, update the
            // cache of _domainsInProblem and
            // _vdssInProblem
            if (_vdssInProblem.containsKey(vdsId)) {
                // DO NOT unify the next 2 for
                // statements because of
                // java.util.ConcurrentModificationException
                List<Guid> domainsRecoveredFromProblem = new ArrayList<Guid>();
                for (Guid domainId : _vdssInProblem.get(vdsId)) {
                    if (!domainsInProblems.contains(domainId)) {
                        domainsRecoveredFromProblem.add(domainId);
                    }
                }
                for (Guid domainRecovered : domainsRecoveredFromProblem) {
                    DomainRecoveredFromProblem(domainRecovered, vdsId, vdsName);
                }
            }

            // for all problematic domains
            // update cache of _domainsInProblem
            // and _vdssInProblem and add a new
            // timer for new domains in problem
            Set<Guid> domainsInProblemKeySet = _domainsInProblem.keySet();
            for (Guid domainId : domainsInProblems) {
                if (domainsInProblemKeySet.contains(domainId)) {
                    // existing domains in problem
                    UpdateDomainInProblemData(domainId, vdsId, vdsName);
                } else {
                    // new domains in problems
                    AddDomainInProblemData(domainId, vdsId, vdsName);
                }
            }
        }

        private void DomainRecoveredFromProblem(Guid domainId, Guid vdsId, String vdsName) {
            String domainIdTuple = getDomainIdTuple(domainId);
            log.infoFormat("Domain {0} recovered from problem. vds: {1}", domainIdTuple, vdsName);
            ClearVds(vdsId, domainId);
            _domainsInProblem.get(domainId).remove(vdsId);
            if (_domainsInProblem.get(domainId).size() == 0) {
                log.infoFormat("Domain {0} has recovered from problem. No active host in the DC is reporting it as" +
                        " problematic, so clearing the domain recovery timer.", domainIdTuple);
                _domainsInProblem.remove(domainId);
                clearTimer(domainId);
            }
        }

        private void AddDomainInProblemData(Guid domainId, Guid vdsId, String vdsName) {
            UpdateVdsInProblem(domainId, vdsId);
            _domainsInProblem.put(domainId, new java.util.HashSet<Guid>(java.util.Arrays.asList(vdsId)));
            log.warnFormat("domain {0} in problem. vds: {1}", getDomainIdTuple(domainId), vdsName);
            Class[] inputType = new Class[] { Guid.class };
            Object[] inputParams = new Object[] { domainId };
            String jobId = SchedulerUtilQuartzImpl.getInstance().scheduleAOneTimeJob(this, "OnTimer", inputType,
                    inputParams, Config.<Integer> GetValue(ConfigValues.StorageDomainFalureTimeoutInMinutes),
                    TimeUnit.MINUTES);
            clearTimer(domainId);
            _timers.put(domainId, jobId);
        }

        @OnTimerMethodAnnotation("OnTimer")
        public void OnTimer(final Guid domainId) {
            ((EventQueue) EjbUtils.findBean(BeanType.EVENTQUEUE_MANAGER, BeanProxyType.LOCAL)).submitEventAsync(new Event(_storagePoolId,
                    domainId, null, EventType.DOMAINFAILOVER),
                    new Callable<EventResult>() {
                        @Override
                        public EventResult call() {
                            EventResult result = null;
                            if (_domainsInProblem.containsKey(domainId)) {
                                log.info("starting ProcessDomainRecovery for domain " + getDomainIdTuple(domainId));
                                result = ProcessDomainRecovery(domainId);
                            }
                            _timers.remove(domainId);
                            return result;
                        }
                    });
        }

        private void UpdateDomainInProblemData(Guid domainId, Guid vdsId, String vdsName) {
            log.debugFormat("domain {0} still in problem. vds: {1}", getDomainIdTuple(domainId), vdsName);
            _domainsInProblem.get(domainId).add(vdsId);
            UpdateVdsInProblem(domainId, vdsId);
        }

        private void UpdateVdsInProblem(Guid domainId, Guid vdsId) {
            if (!_vdssInProblem.containsKey(vdsId)) {
                _vdssInProblem.put(vdsId, new java.util.HashSet<Guid>());
            }
            _vdssInProblem.get(vdsId).add(domainId);
        }

        private EventResult ProcessDomainRecovery(final Guid domainId) {
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
            if (vdssInProblem.size() > 0) {
                if (storageDomain.getStorageDomainType() != StorageDomainType.ImportExport
                        && storageDomain.getStorageDomainType() != StorageDomainType.ISO) {
                    // The domain is of type DATA and was
                    // reported as in problem.
                    // Moving all the hosts which reported on
                    // this domain as in problem to non
                    // operational.
                    for (Guid vdsId : _domainsInProblem.get(domainId)) {
                        VDS vds = vdsMap.get(vdsId);
                        if (vds == null) {
                            log.warnFormat(
                                    "vds {0} reported domain {1} - as in problem but cannot find vds in db!!",
                                    vdsId,
                                    domainIdTuple);
                        } else if (vds.getStatus() != VDSStatus.Maintenance
                                && vds.getStatus() != VDSStatus.NonOperational) {
                            log.warnFormat(
                                    "vds {0} reported domain {1} as in problem, moving the vds to status NonOperational",
                                    vds.getName(),
                                    domainIdTuple);
                            ResourceManager
                                    .getInstance()
                                    .getEventListener()
                                    .vdsNonOperational(vdsId, NonOperationalReason.STORAGE_DOMAIN_UNREACHABLE,
                                            true, true, domainId);
                            clearVdsFromCache(vdsId, vds.getName());
                        } else {
                            log.warnFormat(
                                    "vds {0} reported domain {1} as in problem, vds is in status {3}, no need to move to nonoperational",
                                    vds.getName(),
                                    domainIdTuple,
                                    vds.getStatus());
                        }
                    }
                } else {
                    log.warnFormat(
                            "Storage domain {0} is not visible to one or more hosts. "
                                    +
                                    "Since the domain's type is {1}, hosts status will not be changed to non-operational",
                            domainIdTuple,
                            storageDomain.getStorageDomainType());
                }
                result = new EventResult(true, EventType.VDSSTOARGEPROBLEMS);
            } else { // Because all the hosts in status UP
                     // reported on this domain as in problem
                     // we assume the problem is with the
                     // Domain.
                if (storageDomain.getStorageDomainType() != StorageDomainType.Master) {
                    log.errorFormat("Domain {0} was reported by all hosts in status UP as problematic. Moving the domain to NonOperational.",
                            domainIdTuple);
                    result = ResourceManager.getInstance()
                            .getEventListener().storageDomainNotOperational(domainId, _storagePoolId);
                } else {
                    log.warnFormat("Domain {0} was reported by all hosts in status UP as problematic. Not moving the domain to NonOperational because it is being reconstructed now.",
                            domainIdTuple);
                    result = ResourceManager.getInstance()
                            .getEventListener().masterDomainNotOperational(domainId, _storagePoolId);
                }
            }

            // clear from cache of _vdssInProblem and
            // _domainsInProblem
            clearDomainFromCache(domainId);
            clearTimer(domainId);
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
         */
        private void clearDomainFromCache(Guid domainId) {
            Set<Guid> vdsIds = _domainsInProblem.remove(domainId);
            if (vdsIds != null) {
                for (Guid vdsId : vdsIds) {
                    ClearVds(vdsId, domainId);
                }
            }
        }

        /**
         * deletes all the jobs for the domains in the pool and clears the problematic entities caches.
         */
        public void clearCache() {
            log.info("clearing cache for problematic entities in pool " + _storagePoolId);
            // clear lists
            _timers.clear();
            _domainsInProblem.clear();
            _vdssInProblem.clear();
        }

        public void clearPoolTimers() {
            log.info("clear domain error-timers for pool " + _storagePoolId);
            for (String jobId : _timers.values()) {
                try {
                    SchedulerUtilQuartzImpl.getInstance().deleteJob(jobId);
                } catch (Exception e) {
                    log.warn("failed deleting job " + jobId);
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
            log.infoFormat("Clearing cache of pool: {0} for problematic entities of VDS: {1}.",
                    _storagePoolId, vdsName);

            if (_vdssInProblem.containsKey(vdsId)) {
                for (Guid domainId : _vdssInProblem.get(vdsId)) {
                    DomainRecoveredFromProblem(domainId, vdsId, vdsName);
                }
            }
        }

        private void ClearVds(Guid vdsId, Guid domainId) {
            _vdssInProblem.get(vdsId).remove(domainId);
            if (_vdssInProblem.get(vdsId).isEmpty()) {
                _vdssInProblem.remove(vdsId);
            }
        }

        private boolean _disposed;

        public void Dispose() {
            synchronized (syncObj) {
                log.info("IrsProxyData::disposing");
                ResetIrs();
                SchedulerUtilQuartzImpl.getInstance().deleteJob(storagePoolRefreshJobId);
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

    /**
     * Remove a VDS entry from the pool's IRS Proxy cache, clearing the problematic domains for this VDS and their
     * timers if they need to be cleaned. This is for a case when the VDS is switched to maintenance by the user, since
     * we need to clear it's cache data and timers, or else the cache will contain stale data (since the VDS is not
     * active anymore, it won't be connected to any of the domains).
     *
     * @param storagePoolId
     *            The ID of the storage pool to clean the IRS Proxy's cache for.
     * @param vdsId
     *            The ID of the VDS to remove from the cache.
     * @param vdsName
     *            The name of the VDS (for logging).
     */
    public static void clearVdsFromCache(Guid storagePoolId, Guid vdsId, String vdsName) {
        IrsProxyData irsProxyData = getIrsProxyData(storagePoolId);
        if (irsProxyData != null) {
            irsProxyData.clearVdsFromCache(vdsId, vdsName);
        }
    }

    /**
     * Return the IRS Proxy object for the given pool id. If there's no proxy data available, since there's no SPM
     * for the pool, then returns <code>null</code>.
     * @param storagePoolId The ID of the storage pool to get the IRS proxy for.
     * @return The IRS Proxy object, on <code>null</code> if no proxy data is available.
     */
    protected static IrsProxyData getIrsProxyData(Guid storagePoolId) {
        return _irsProxyData.get(storagePoolId);
    }

    protected IrsProxyData getCurrentIrsProxyData() {
        IrsProxyData returnValue;
        if (_irsProxyData.containsKey(getParameters().getStoragePoolId())) {
            returnValue = _irsProxyData.get(getParameters().getStoragePoolId());
        } else {
            _irsProxyData.put(getParameters().getStoragePoolId(), new IrsProxyData(getParameters().getStoragePoolId()));
            returnValue = _irsProxyData.get(getParameters().getStoragePoolId());
        }
        return returnValue;
    }

    private int _failoverCounter;

    private void failover() {
        if ((getParameters().getIgnoreFailoverLimit() || _failoverCounter < Config
                .<Integer> GetValue(ConfigValues.SpmCommandFailOverRetries) - 1)
                && getCurrentIrsProxyData().getHasVdssForSpmSelection() && getCurrentIrsProxyData().failover()) {
            _failoverCounter++;
            executeCommand();
        } else {
            getVDSReturnValue().setSucceeded(false);
        }
    }

    public IrsBrokerCommand(P parameters) {
        super(parameters);

    }

    protected IIrsServer getIrsProxy() {
        return getCurrentIrsProxyData().getIrsProxy();
    }

    @Override
    protected void executeVDSCommand() {
        boolean isStartReconstruct = false;
        synchronized (getCurrentIrsProxyData().syncObj) {
            try {
                if (getIrsProxy() != null) {
                    ExecuteIrsBrokerCommand();
                } else {
                    if (getVDSReturnValue().getVdsError() == null) {
                        getVDSReturnValue().setExceptionString("Cannot allocate IRS server");
                        VDSError tempVar = new VDSError();
                        tempVar.setCode(VdcBllErrors.IRS_REPOSITORY_NOT_FOUND);
                        tempVar.setMessage("Cannot allocate IRS server");
                        getVDSReturnValue().setVdsError(tempVar);
                    }
                    getVDSReturnValue().setSucceeded(false);
                }
            }
            catch (UndeclaredThrowableException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                getVDSReturnValue().setVdsError(new VDSError(VdcBllErrors.VDS_NETWORK_ERROR, ex.getMessage()));
                if (ExceptionUtils.getRootCause(ex) != null) {
                    logException(ExceptionUtils.getRootCause(ex));
                } else {
                    LoggedUtils.logError(log, LoggedUtils.getObjectId(this), this, ex);
                }
                failover();
            }
            catch (XmlRpcRunTimeException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                if (ex.isNetworkError()) {
                    log.errorFormat("IrsBroker::Failed::{0} - network exception.", getCommandName());
                    getVDSReturnValue().setSucceeded(false);
                } else {
                    log.errorFormat("IrsBroker::Failed::{0}", getCommandName());
                    LoggedUtils.logError(log, LoggedUtils.getObjectId(this), this, ex);
                    throw new IRSProtocolException(ex);
                }
            }
            catch (IRSNoMasterDomainException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                getVDSReturnValue().setVdsError(ex.getVdsError());
                log.errorFormat("IrsBroker::Failed::{0}", getCommandName());
                log.errorFormat("Exception: {0}", ex.getMessage());

                if (getCurrentIrsProxyData().getHasVdssForSpmSelection()) {
                    failover();
                } else {
                    isStartReconstruct = true;
                }
            } catch (IRSUnicodeArgumentException ex) {
                throw new IRSGenericException("UNICODE characters are not supported.", ex);
            } catch (IRSStoragePoolStatusException ex) {
                throw ex;
            } catch (IrsOperationFailedNoFailoverException ex) {
                throw ex;
            } catch (IRSNonOperationalException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                getVDSReturnValue().setVdsError(ex.getVdsError());
                logException(ex);
                if (ex.getVdsError() != null && VdcBllErrors.SpmStatusError == ex.getVdsError().getCode()) {
                    getCurrentIrsProxyData().setCurrentVdsId(Guid.Empty);
                }
                failover();
            } catch (IRSErrorException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                getVDSReturnValue().setVdsError(ex.getVdsError());
                logException(ex);
                if (log.isDebugEnabled()) {
                    LoggedUtils.logError(log, LoggedUtils.getObjectId(this), this, ex);
                }
                failover();
            } catch (RuntimeException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                if (ex instanceof VDSExceptionBase) {
                    getVDSReturnValue().setVdsError(((VDSExceptionBase) ex).getVdsError());
                }
                if (ExceptionUtils.getRootCause(ex) != null &&
                        ExceptionUtils.getRootCause(ex) instanceof SocketException) {
                    logException(ExceptionUtils.getRootCause(ex));
                } else {
                    LoggedUtils.logError(log, LoggedUtils.getObjectId(this), this, ex);
                }
                // always failover because of changes in vdsm error, until we
                // realize what to do in each case:
                failover();
            } finally {
                getCurrentIrsProxyData().getTriedVdssList().clear();
            }
        }
        if (isStartReconstruct) {
            startReconstruct();
        }
    }

    private void startReconstruct() {
        StorageDomainStatic masterDomain = null;
        List<StorageDomainStatic> storageDomainStaticList = DbFacade.getInstance()
                .getStorageDomainStaticDao().getAllForStoragePool(getParameters().getStoragePoolId());
        for (StorageDomainStatic storageDomainStatic : storageDomainStaticList) {
            if (storageDomainStatic.getStorageDomainType() == StorageDomainType.Master) {
                masterDomain = storageDomainStatic;
                break;
            }
        }

        if (masterDomain != null) {
            final Guid masterDomainId = masterDomain.getId();
            ((EventQueue) EjbUtils.findBean(BeanType.EVENTQUEUE_MANAGER, BeanProxyType.LOCAL)).submitEventAsync(new Event(getParameters().getStoragePoolId(),
                    masterDomainId, null, EventType.RECONSTRUCT),
                    new Callable<EventResult>() {
                        @Override
                        public EventResult call() {
                            return ResourceManager.getInstance()
                                    .getEventListener().masterDomainNotOperational(
                                            masterDomainId, getParameters().getStoragePoolId());
                        }
                    });
        } else {
            log.errorFormat(
                    "IrsBroker::IRSNoMasterDomainException:: Could not find master domain for pool {0} !!!",
                    getParameters().getStoragePoolId());
        }
    }

    protected void ExecuteIrsBrokerCommand() {
    }

    /**
     * Write the exception to the system log.
     *
     * @param ex
     *            Exception to log.
     */
    private void logException(Throwable ex) {
        log.errorFormat("IrsBroker::Failed::{0} due to: {1}", getCommandName(), ExceptionUtils.getMessage(ex));
    }

    public static Long AssignLongValue(Map<String, Object> input, String name) {
        Long returnValue = null;
        if (input.containsKey(name)) {
            String stringValue = null;
            try {
                if (input.get(name) instanceof String) {
                    stringValue = (String) input.get(name);
                    returnValue = Long.parseLong(stringValue);
                }
            } catch (NumberFormatException nfe) {
                log.errorFormat("Failed to parse {0} value {1} to long", name, stringValue);
                returnValue = null;
            }
        }
        return returnValue;
    }

    private static Log log = LogFactory.getLog(IrsBrokerCommand.class);
}

package org.ovirt.engine.core.vdsbroker;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSDomainsData;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.FileUtil;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSErrorException;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.CollectVdsNetworkDataVDSCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.GetCapabilitiesVDSCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.IVdsServer;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSNetworkException;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSRecoveringException;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsServerConnector;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsServerWrapper;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcUtils;

public class VdsManager {
    private VDS _vds;

    protected VDS getVds() {
        return _vds;
    }

    protected void setVds(VDS value) {
        _vds = value;
    }

    public boolean getRefreshStatistics() {
        return (_refreshIteration == _numberRefreshesBeforeSave);
    }

    private static final int VDS_REFRESH_RATE = Config.<Integer> GetValue(ConfigValues.VdsRefreshRate) * 1000;

    private String onTimerJobId;

    private final int _numberRefreshesBeforeSave = Config.<Integer> GetValue(ConfigValues.NumberVmRefreshesBeforeSave);
    private int _refreshIteration = 1;

    private final Object _lockObj = new Object();
    private static Map<Guid, String> recoveringJobIdMap = new ConcurrentHashMap<Guid, String>();
    private boolean isSetNonOperationalExecuted;

    public Object getLockObj() {
        return _lockObj;
    }

    public static void cancelRecoveryJob(Guid vdsId) {
        String jobId = recoveringJobIdMap.remove(vdsId);
        if (jobId != null) {
            log.infoFormat("Cancelling the recovery from crash timer for VDS {0} because vds started initializing", vdsId);
            try {
                SchedulerUtilQuartzImpl.getInstance().deleteJob(jobId);
            } catch (Exception e) {
                log.warnFormat("Failed deleting job {0} at cancelRecoveryJob", jobId);
            }
        }
    }

    public DateTime mLastUpdate = DateTime.getNow();

    private final AtomicInteger mFailedToRunVmAttempts;
    private final AtomicInteger mUnrespondedAttempts;

    private static final int VDS_DURING_FAILURE_TIMEOUT_IN_MINUTES = Config
            .<Integer> GetValue(ConfigValues.TimeToReduceFailedRunOnVdsInMinutes);
    private static final int VDS_RECOVERY_TIMEOUT_IN_MINUTES = Config
            .<Integer> GetValue(ConfigValues.VdsRecoveryTimeoutInMintues);
    private String duringFailureJobId;
    private boolean privateInitialized;

    public boolean getInitialized() {
        return privateInitialized;
    }

    public void setInitialized(boolean value) {
        privateInitialized = value;
    }

    private IVdsServer _vdsProxy;

    public IVdsServer getVdsProxy() {
        return _vdsProxy;
    }

    public Guid getVdsId() {
        return _vdsId;
    }

    private boolean mBeforeFirstRefresh = true;

    public boolean getbeforeFirstRefresh() {
        return mBeforeFirstRefresh;
    }

    public void setbeforeFirstRefresh(boolean value) {
        mBeforeFirstRefresh = value;
    }

    private final Guid _vdsId;

    private VdsManager(VDS vds) {
        _vds = vds;
        _vdsId = vds.getvds_id();
        mUnrespondedAttempts = new AtomicInteger();
        mFailedToRunVmAttempts = new AtomicInteger();
        log.info("Eneterd VdsManager:constructor");
        if (_vds.getstatus() == VDSStatus.PreparingForMaintenance) {
            _vds.setprevious_status(_vds.getstatus());
        } else {
            _vds.setprevious_status(VDSStatus.Up);
        }
        // if ssl is on and no certificate file
        if (Config.<Boolean> GetValue(ConfigValues.UseSecureConnectionWithServers)
                    && !FileUtil.fileExists(Config.resolveCertificatePath())) {
            if (_vds.getstatus() != VDSStatus.Maintenance && _vds.getstatus() != VDSStatus.InstallFailed) {
                setStatus(VDSStatus.NonResponsive, _vds);
                UpdateDynamicData(_vds.getDynamicData());
            }
            log.error("Could not find VDC Certificate file.");
            AuditLogableBase logable = new AuditLogableBase(_vdsId);
            AuditLogDirector.log(logable, AuditLogType.CERTIFICATE_FILE_NOT_FOUND);
        }
        InitVdsBroker();
        _vds = null;

    }

    public static VdsManager buildVdsManager(VDS vds) {
        VdsManager vdsManager = new VdsManager(vds);
        vdsManager.schedulJobs();
        return vdsManager;
    }

    private void schedulJobs() {
        SchedulerUtil sched = SchedulerUtilQuartzImpl.getInstance();
        duringFailureJobId = sched.scheduleAFixedDelayJob(this, "OnVdsDuringFailureTimer", new Class[0],
                    new Object[0], VDS_DURING_FAILURE_TIMEOUT_IN_MINUTES, VDS_DURING_FAILURE_TIMEOUT_IN_MINUTES,
                    TimeUnit.MINUTES);
        sched.pauseJob(duringFailureJobId);
        // start with refresh statistics
        _refreshIteration = _numberRefreshesBeforeSave - 1;

        onTimerJobId = sched.scheduleAFixedDelayJob(this, "OnTimer", new Class[0], new Object[0], VDS_REFRESH_RATE,
                VDS_REFRESH_RATE, TimeUnit.MILLISECONDS);
    }

    private void InitVdsBroker() {
        log.infoFormat("vdsBroker({0},{1})", _vds.gethost_name(), _vds.getport());

        int clientTimeOut = Config.<Integer> GetValue(ConfigValues.vdsTimeout) * 1000;
        KeyValuePairCompat<VdsServerConnector, HttpClient> returnValue =
                XmlRpcUtils.getConnection(_vds.gethost_name(),
                        _vds.getport(),
                        clientTimeOut,
                        VdsServerConnector.class,
                        Config.<Boolean> GetValue(ConfigValues.UseSecureConnectionWithServers));
        _vdsProxy = new VdsServerWrapper(returnValue.getKey(), returnValue.getValue());
    }

    public void UpdateVmDynamic(VmDynamic vmDynamic) {
        if (_vdsUpdater != null) {
            VM vm = _vdsUpdater.GetVmFromDictionary(vmDynamic.getId());
            if (vm != null) {
                vm.setDynamicData(vmDynamic);
            }
        }
        DbFacade.getInstance().getVmDynamicDAO().update(vmDynamic);
    }

    private VdsUpdateRunTimeInfo _vdsUpdater;

    @OnTimerMethodAnnotation("OnTimer")
    public void OnTimer() {
        try {
            setIsSetNonOperationalExecuted(false);
            Guid vdsId = null;
            Guid storagePoolId = null;
            String vdsName = null;
            ArrayList<VDSDomainsData> domainsList = null;

            synchronized (getLockObj()) {
                TransactionSupport.executeInScope(TransactionScopeOption.Suppress, new TransactionMethod<Object>() {
                    @Override
                    public Object runInTransaction() {
                        {
                            _vds = DbFacade.getInstance().getVdsDAO().get(getVdsId());
                            if (_vds == null) {
                                log.errorFormat("ResourceManager::refreshVdsRunTimeInfo - OnTimer is NULL for {0}",
                                        getVdsId());
                                return null;
                            }

                            try {
                                if (_refreshIteration == _numberRefreshesBeforeSave) {
                                    _refreshIteration = 1;
                                } else {
                                    _refreshIteration++;
                                }
                                if ((_vds.getstatus() != VDSStatus.NonOperational || (_vds.getvm_count() > 0))
                                        && (_vds.getstatus() != VDSStatus.Installing
                                                && _vds.getstatus() != VDSStatus.InstallFailed
                                                && _vds.getstatus() != VDSStatus.Reboot
                                                && _vds.getstatus() != VDSStatus.Maintenance
                                                && _vds.getstatus() != VDSStatus.PendingApproval
                                                && _vds.getstatus() != VDSStatus.Down)) {

                                    _vdsUpdater = new VdsUpdateRunTimeInfo(VdsManager.this, _vds);
                                    _vdsUpdater.Refresh();
                                    mUnrespondedAttempts.set(0);
                                    mLastUpdate = DateTime.getNow();
                                }
                                if (!getInitialized() && getVds().getstatus() != VDSStatus.NonResponsive
                                        && getVds().getstatus() != VDSStatus.PendingApproval) {
                                    log.info("Initializing Host: " + getVds().getvds_name());
                                    ResourceManager.getInstance().HandleVdsFinishedInit(_vds.getvds_id());
                                    setInitialized(true);
                                }
                            } catch (VDSNetworkException e) {
                                logNetworkException(e);
                            } catch (VDSRecoveringException ex) {
                                HandleVdsRecoveringException(ex);
                            } catch (IRSErrorException ex) {
                                logFailureMessage(ex);
                                if (log.isDebugEnabled()) {
                                    logException(ex);
                                }
                            } catch (RuntimeException ex) {
                                logFailureMessage(ex);
                                logException(ex);
                            }

                        }
                        return null;
                    }

                    private void logFailureMessage(RuntimeException ex) {
                        log.warnFormat(
                                "ResourceManager::refreshVdsRunTimeInfo::Failed to refresh VDS , vds = {0} : {1}, error = '{2}', continuing.",
                                _vds.getvds_id(),
                                _vds.getvds_name(),
                                ExceptionUtils.getMessage(ex));
                    }
                });
                try {
                    if (_vdsUpdater != null) {
                        _vdsUpdater.AfterRefreshTreatment();

                        // Get vds data for updating domains list, ignoring vds which is down, since it's not connected to
                        // the storage anymore (so there is no sense in updating the domains list in that case).
                        if (_vds != null && _vds.getstatus() != VDSStatus.Maintenance) {
                            vdsId = _vds.getvds_id();
                            vdsName = _vds.getvds_name();
                            storagePoolId = _vds.getstorage_pool_id();
                            domainsList = _vds.getDomains();
                        }
                    }

                    _vds = null;
                    _vdsUpdater = null;
                } catch (IRSErrorException ex) {
                    logFailureMessage(ex);
                    if (log.isDebugEnabled()) {
                        logException(ex);
                    }
                } catch (RuntimeException ex) {
                    logFailureMessage(ex);
                    logException(ex);
                }

            }

            // Now update the status of domains, this code should not be in
            // synchronized part of code
            if (domainsList != null) {
                IrsBrokerCommand.UpdateVdsDomainsData(vdsId, vdsName, storagePoolId, domainsList);
            }
        } catch (Exception e) {
            log.error("Timer update runtimeinfo failed. Exception:", e);
        }
    }

    private static void logException(final RuntimeException ex) {
        log.error("ResourceManager::refreshVdsRunTimeInfo", ex);
    }

    private void logFailureMessage(RuntimeException ex) {
        log.warnFormat(
                "ResourceManager::refreshVdsRunTimeInfo::Failed to AfterRefreshTreatment VDS  error = '{0}', continuing.",
                ExceptionUtils.getMessage(ex));
    }

    private void HandleVdsRecoveringException(VDSRecoveringException ex) {
        if (_vds.getstatus() != VDSStatus.Initializing && _vds.getstatus() != VDSStatus.NonOperational) {
            setStatus(VDSStatus.Initializing, _vds);
            UpdateDynamicData(_vds.getDynamicData());
            AuditLogableBase logable = new AuditLogableBase(_vds.getvds_id());
            logable.AddCustomValue("ErrorMessage", ex.getMessage());
            AuditLogDirector.log(logable, AuditLogType.VDS_INITIALIZING);
            log.warnFormat(
                    "ResourceManager::refreshVdsRunTimeInfo::Failed to refresh VDS , vds = {0} : {1}, error = {2}, continuing.",
                    _vds.getvds_id(),
                    _vds.getvds_name(),
                    ex.getMessage());

            String jobId = SchedulerUtilQuartzImpl.getInstance().scheduleAOneTimeJob(this, "onTimerHandleVdsRecovering", new Class[0],
                    new Object[0], VDS_RECOVERY_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
            recoveringJobIdMap.put(_vds.getvds_id(), jobId);
        }
    }

    @OnTimerMethodAnnotation("onTimerHandleVdsRecovering")
    public void onTimerHandleVdsRecovering() {
        recoveringJobIdMap.remove(getVdsId());
        VDS vds = DbFacade.getInstance().getVdsDAO().get(getVdsId());
        if (vds.getstatus() == VDSStatus.Initializing) {
            try {
                ResourceManager
                            .getInstance()
                            .getEventListener()
                            .VdsNonOperational(vds.getvds_id(),
                                    NonOperationalReason.TIMEOUT_RECOVERING_FROM_CRASH,
                                    true,
                                    true,
                                    Guid.Empty);
                setIsSetNonOperationalExecuted(true);
            } catch (RuntimeException exp) {
                log.errorFormat(
                            "HandleVdsRecoveringException::Error in recovery timer treatment, vds = {0} : {1}, error = {2}.",
                            vds.getvds_id(),
                            vds.getvds_name(),
                            exp.getMessage());
            }
        }
    }

    /**
     * Save dynamic data to cache and DB.
     *
     * @param dynamicData
     */
    public void UpdateDynamicData(VdsDynamic dynamicData) {
        DbFacade.getInstance().getVdsDynamicDAO().update(dynamicData);
    }

    /**
     * Save statistics data to cache and DB.
     *
     * @param statisticsData
     */
    public void UpdateStatisticsData(VdsStatistics statisticsData) {
        DbFacade.getInstance().getVdsStatisticsDAO().update(statisticsData);
    }

    public void activate() {
        VDS vds = null;
        boolean cpuFlagsChanged = false;
        try {
            // refresh vds from db in case changed while was down
            // ResourceManager.Instance.RunVdsCommand(VDSCommandType.UpdateVdsCache,
            // new VdsIdVDSCommandParametersBase(_vds.vds_id));
            if (log.isDebugEnabled()) {
                log.debugFormat(
                        "ResourceManager::activateVds - trying to activate host {0} , meanwhile setting status to Unassigned meanwhile",
                        getVdsId());
            }
            vds = DbFacade.getInstance().getVdsDAO().get(getVdsId());
            /**
             * refresh capabilities
             */
            RefObject<Boolean> tempRefObject = new RefObject<Boolean>(cpuFlagsChanged);
            VDSStatus newStatus = refreshCapabilities(tempRefObject, vds);
            if (log.isDebugEnabled()) {
                log.debugFormat(
                        "ResourceManager::activateVds - success to refreshCapabilities for host {0} , new status will be {1} ",
                        getVdsId(),
                        newStatus);
            }
            cpuFlagsChanged = tempRefObject.argvalue;
        } catch (java.lang.Exception e) {
            log.infoFormat("ResourceManager::activateVds - failed to get VDS = {0} capabilities with error: {1}.",
                    getVdsId(), e.getMessage());
            log.infoFormat("ResourceManager::activateVds - failed to activate VDS = {0}", getVdsId());

        } finally {
            if (vds != null) {
                UpdateDynamicData(vds.getDynamicData());
                // always check flags in case host cluster changed
                ResourceManager.getInstance().getEventListener().ProcessOnCpuFlagsChange(vds.getvds_id());
            }
        }
    }

    public void setStatus(VDSStatus status, VDS vds) {
        synchronized (getLockObj()) {
            if (vds == null) {
                vds = DbFacade.getInstance().getVdsDAO().get(getVdsId());
            }
            if (vds.getprevious_status() != vds.getstatus()) {
                vds.setprevious_status(vds.getstatus());
                if (_vds != null) {
                    _vds.setprevious_status(vds.getstatus());
                }
            }
            // update to new status
            vds.setstatus(status);
            if (_vds != null) {
                _vds.setstatus(status);
            }

            switch (status) {
            case NonOperational:
                if (_vds != null) {
                    _vds.setNonOperationalReason(vds.getNonOperationalReason());
                }
                if(vds.getvm_count() > 0) {
                    break;
                }
            case NonResponsive:
            case Down:
            case Maintenance:
                vds.setcpu_sys(Double.valueOf(0));
                vds.setcpu_user(Double.valueOf(0));
                vds.setcpu_idle(Double.valueOf(0));
                vds.setcpu_load(Double.valueOf(0));
                vds.setusage_cpu_percent(0);
                vds.setusage_mem_percent(0);
                vds.setusage_network_percent(0);
                if (_vds != null) {
                    _vds.setcpu_sys(Double.valueOf(0));
                    _vds.setcpu_user(Double.valueOf(0));
                    _vds.setcpu_idle(Double.valueOf(0));
                    _vds.setcpu_load(Double.valueOf(0));
                    _vds.setusage_cpu_percent(0);
                    _vds.setusage_mem_percent(0);
                    _vds.setusage_network_percent(0);
                }
            }
        }
    }

    /**
     * This function called when vds have failed vm attempts one in predefined time. Its increments failure attemts to
     * one
     *
     * @param obj
     * @param arg
     */
    @OnTimerMethodAnnotation("OnVdsDuringFailureTimer")
    public void OnVdsDuringFailureTimer() {
        synchronized (getLockObj()) {
            VDS vds = DbFacade.getInstance().getVdsDAO().get(getVdsId());
            /**
             * Disable timer if vds returns from suspitious mode
             */
            if (mFailedToRunVmAttempts.decrementAndGet() == 0) {
                SchedulerUtilQuartzImpl.getInstance().pauseJob(duringFailureJobId);
            }
            /**
             * Move vds to Up status from error
             */
            if (mFailedToRunVmAttempts.get() < Config.<Integer> GetValue(ConfigValues.NumberOfFailedRunsOnVds)
                    && vds.getstatus() == VDSStatus.Error) {
                setStatus(VDSStatus.Up, vds);
                UpdateDynamicData(vds.getDynamicData());
            }
            log.infoFormat("OnVdsDuringFailureTimer of vds {0} entered. Time:{1}. Attemts after{2}", vds.getvds_name(),
                    new java.util.Date(), mFailedToRunVmAttempts);
        }
    }

    public void failedToRunVm(VDS vds) {
        if (mFailedToRunVmAttempts.get() < Config.<Integer> GetValue(ConfigValues.NumberOfFailedRunsOnVds)
                && mFailedToRunVmAttempts.incrementAndGet() >= Config
                        .<Integer> GetValue(ConfigValues.NumberOfFailedRunsOnVds)) {
            ResourceManager.getInstance().runVdsCommand(VDSCommandType.SetVdsStatus,
                    new SetVdsStatusVDSCommandParameters(vds.getvds_id(), VDSStatus.Error));

            SchedulerUtilQuartzImpl.getInstance().resumeJob(duringFailureJobId);
            AuditLogableBase logable = new AuditLogableBase(vds.getvds_id());
            logable.AddCustomValue("Time", Config.<Integer> GetValue(ConfigValues.TimeToReduceFailedRunOnVdsInMinutes)
                    .toString());
            AuditLogDirector.log(logable, AuditLogType.VDS_FAILED_TO_RUN_VMS);
            log.infoFormat("Vds {0} moved to Error mode after {1} attemts. Time: {2}", vds.getvds_name(),
                    mFailedToRunVmAttempts, new java.util.Date());
        }
    }

    public void forceRefreshRunTimeInfo() {
        // TODO should be solved with a thread pool
        SchedulerUtilQuartzImpl.getInstance().scheduleAOneTimeJob(this, "OnTimer", new Class[0], new Object[0], 0,
                TimeUnit.MILLISECONDS);
    }

    /**
     */
    public void SuccededToRunVm(Guid vmId) {
        mUnrespondedAttempts.set(0);
        ResourceManager.getInstance().SuccededToRunVm(vmId, _vds.getvds_id());
    }

    public VDSStatus refreshCapabilities(RefObject<Boolean> cpuFlagsHasChanged, VDS vds) {
        log.debug("refreshCapabilities:GetCapabilitiesVDSCommand started method");
        String oldFlags = vds.getcpu_flags();
        GetCapabilitiesVDSCommand vdsBrokerCommand = new GetCapabilitiesVDSCommand(
                new VdsIdAndVdsVDSCommandParametersBase(vds));
        vdsBrokerCommand.Execute();
        if (vdsBrokerCommand.getVDSReturnValue().getSucceeded()) {

            VDSStatus returnStatus = vds.getstatus();
            boolean isSetNonOperational = CollectVdsNetworkDataVDSCommand.UpdateNetworkToDb(vds);
            if (isSetNonOperational) {
                setIsSetNonOperationalExecuted(true);
            }

            if (isSetNonOperational && returnStatus != VDSStatus.NonOperational) {
                if (log.isDebugEnabled()) {
                    log.debugFormat(
                            "refreshCapabilities:GetCapabilitiesVDSCommand vds {0} networks  not match it's cluster networks, vds will be moved to NonOperational",
                            vds.getStaticData().getId());
                }
                vds.setstatus(VDSStatus.NonOperational);
                vds.setNonOperationalReason(NonOperationalReason.NETWORK_UNREACHABLE);
                returnStatus = vds.getstatus();
            }

            if (vds.getkvm_enabled() != null && vds.getkvm_enabled().equals(false)
                    && vds.getstatus() != VDSStatus.NonOperational) {
                if (log.isDebugEnabled()) {
                    log.debugFormat(
                            "refreshCapabilities:GetCapabilitiesVDSCommand vds {0} has not kvm, vds will be moved to NonOperational",
                            vds.getStaticData().getId());
                }
                ResourceManager
                        .getInstance()
                        .getEventListener()
                        .VdsNonOperational(vds.getvds_id(), NonOperationalReason.KVM_NOT_RUNNING, true, true,
                                Guid.Empty);
                vds.setstatus(VDSStatus.NonOperational);
                returnStatus = vds.getstatus();
                setIsSetNonOperationalExecuted(true);
            }

            cpuFlagsHasChanged.argvalue = (!StringHelper.EqOp(oldFlags, vds.getcpu_flags()));
            return returnStatus;
        } else if (vdsBrokerCommand.getVDSReturnValue().getExceptionObject() != null) {
            // if exception is VDSNetworkException then call to
            // handleNetworkException
            if (vdsBrokerCommand.getVDSReturnValue().getExceptionObject() instanceof VDSNetworkException
                    && handleNetworkException((VDSNetworkException) vdsBrokerCommand.getVDSReturnValue()
                            .getExceptionObject(), vds)) {
                UpdateDynamicData(vds.getDynamicData());
                UpdateStatisticsData(vds.getStatisticsData());
            }
            throw vdsBrokerCommand.getVDSReturnValue().getExceptionObject();
        } else {
            log.errorFormat("refreshCapabilities:GetCapabilitiesVDSCommand failed with no exception!");
            throw new RuntimeException(vdsBrokerCommand.getVDSReturnValue().getExceptionString());
        }
    }

    /**
     * Handle network exception, return true if save vdsDynamic to DB is needed.
     *
     * @param ex
     * @return
     */
    public boolean handleNetworkException(VDSNetworkException ex, VDS vds) {
        if (vds.getstatus() != VDSStatus.Down) {
            if (mUnrespondedAttempts.get() < Config.<Integer> GetValue(ConfigValues.VDSAttemptsToResetCount)
                    || mLastUpdate.AddSeconds(Config.<Integer> GetValue(ConfigValues.TimeoutToResetVdsInSeconds))
                            .compareTo(new java.util.Date()) > 0) {
                boolean result = false;
                if (vds.getstatus() != VDSStatus.Problematic && vds.getstatus() != VDSStatus.PreparingForMaintenance
                        && vds.getstatus() != VDSStatus.NonResponsive) {
                    setStatus(VDSStatus.Problematic, vds);
                    result = true;
                }
                mUnrespondedAttempts.incrementAndGet();
                return result;
            }

            if (vds.getstatus() == VDSStatus.NonResponsive || vds.getstatus() == VDSStatus.Maintenance) {
                // clearNotRespondingVds();
                setStatus(VDSStatus.NonResponsive, vds);
                return true;
            }
            setStatus(VDSStatus.NonResponsive, vds);
            log.errorFormat(
                    "VDS::handleNetworkException Server failed to respond,  vds_id = {0}, vds_name = {1}, error = {2}",
                    vds.getvds_id(), vds.getvds_name(), ex.getMessage());

            AuditLogableBase logable = new AuditLogableBase(vds.getvds_id());
            AuditLogDirector.log(logable, AuditLogType.VDS_FAILURE);
            if (ResourceManager.getInstance().getEventListener() != null) {
                ResourceManager.getInstance().getEventListener().VdsNotResponding(vds);
            }
        }
        return true;
    }

    public void dispose() {
        log.info("vdsManager::disposing");
        SchedulerUtilQuartzImpl.getInstance().deleteJob(onTimerJobId);
        XmlRpcUtils.shutDownConnection(((VdsServerWrapper) _vdsProxy).getHttpClient());;
    }

    /**
     * Log the network exception depending on the VDS status.
     *
     * @param e
     *            The exception to log.
     */
    private void logNetworkException(VDSNetworkException e) {
        switch (_vds.getstatus()) {
        case Down:
            break;
        case NonResponsive:
            log.debugFormat(
                    "ResourceManager::refreshVdsRunTimeInfo::Failed to refresh VDS , vds = {0} : {1}, VDS Network Error, continuing.\n{2}",
                    _vds.getvds_id(),
                    _vds.getvds_name(),
                    e.getMessage());
            break;
        default:
            log.warnFormat(
                    "ResourceManager::refreshVdsRunTimeInfo::Failed to refresh VDS , vds = {0} : {1}, VDS Network Error, continuing.\n{2}",
                    _vds.getvds_id(),
                    _vds.getvds_name(),
                    e.getMessage());
        }
    }

    public void setIsSetNonOperationalExecuted(boolean isExecuted) {
        this.isSetNonOperationalExecuted = isExecuted;
    }

    public boolean isSetNonOperationalExecuted() {
        return isSetNonOperationalExecuted;
    }

    private static LogCompat log = LogFactoryCompat.getLog(VdsManager.class);

}

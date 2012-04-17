package org.ovirt.engine.core.vdsbroker;

import static org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomAuditLogKeys.HostNetworkMTU;
import static org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomAuditLogKeys.LogicalNetworkMTU;
import static org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomAuditLogKeys.NetworkName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VdsNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.FullListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetVmStatsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dal.dbbroker.generic.RepositoryException;
import org.ovirt.engine.core.dao.MassOperationsDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;
import org.ovirt.engine.core.utils.Pair;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSErrorException;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.DestroyVDSCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.FullListVdsCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.GetAllVmStatsVDSCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.GetStatsVDSCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.GetVmStatsVDSCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.ListVDSCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSErrorException;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSNetworkException;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSProtocolException;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSRecoveringException;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class VdsUpdateRunTimeInfo {
    private static final Integer LOW_SPACE_THRESHOLD =
            Config.<Integer> GetValue(ConfigValues.VdsLocalDisksLowFreeSpace);
    private static final Integer LOW_SPACE_CRITICAL_THRESHOLD =
            Config.<Integer> GetValue(ConfigValues.VdsLocalDisksCriticallyLowFreeSpace);

    private java.util.HashMap<Guid, java.util.Map.Entry<VmDynamic, VmStatistics>> _runningVms;
    private final java.util.HashMap<Guid, VmDynamic> _vmDynamicToSave = new java.util.HashMap<Guid, VmDynamic>();
    private final java.util.HashMap<Guid, VmStatistics> _vmStatisticsToSave =
            new java.util.HashMap<Guid, VmStatistics>();
    private final java.util.HashMap<Guid, List<VmNetworkInterface>> _vmInterfaceStatisticsToSave =
            new java.util.HashMap<Guid, List<VmNetworkInterface>>();
    private final java.util.HashMap<Guid, DiskImageDynamic> _vmDiskImageDynamicToSave =
            new java.util.HashMap<Guid, DiskImageDynamic>();
    private final HashMap<VmDeviceId, VmDevice> vmDeviceToSave = new HashMap<VmDeviceId, VmDevice>();
    private final List<VmDevice> newVmDevices = new ArrayList<VmDevice>();
    private final List<VmDeviceId> removedDeviceIds = new ArrayList<VmDeviceId>();
    private final java.util.HashMap<VM, VmDynamic> _vmsClientIpChanged = new java.util.HashMap<VM, VmDynamic>();
    private final java.util.ArrayList<VmDynamic> _poweringUpVms = new java.util.ArrayList<VmDynamic>();
    private final java.util.ArrayList<Guid> _vmsToRerun = new java.util.ArrayList<Guid>();
    private final java.util.ArrayList<Guid> _autoVmsToRun = new java.util.ArrayList<Guid>();
    private final java.util.ArrayList<Guid> _vmsMovedToDown = new java.util.ArrayList<Guid>();
    private final java.util.ArrayList<Guid> _vmsToRemoveFromAsync = new java.util.ArrayList<Guid>();
    private final java.util.ArrayList<Guid> _succededToRunVms = new java.util.ArrayList<Guid>();
    private boolean _saveVdsDynamic;
    private VDSStatus _firstStatus = VDSStatus.forValue(0);
    private boolean _saveVdsStatistics;
    private java.util.ArrayList<Guid> _vdssToRefresh;
    private VdsManager _vdsManager;
    private MonitoringStrategy monitoringStrategy;
    private VDS _vds;
    private Map<Guid, VM> _vmDict;
    private boolean processHardwareCapsNeeded;
    private boolean refreshedCapabilities = false;
    private static Map<Guid, Long> hostDownTimes = new HashMap<Guid, Long>();
    private int runningVmsInTransition = 0;

    public VM GetVmFromDictionary(Guid id) {
        VM vm = null;
        vm = _vmDict.get(id);
        return vm;
    }

    private void SaveDataToDb() {
        if (_saveVdsDynamic) {
            _vdsManager.UpdateDynamicData(_vds.getDynamicData());
        }

        if (_saveVdsStatistics) {
            VdsStatistics stat = _vds.getStatisticsData();
            _vdsManager.UpdateStatisticsData(stat);
            CheckVdsMemoryThreshold(stat);

            final List<VdsNetworkStatistics> statistics = new LinkedList<VdsNetworkStatistics>();
            for (VdsNetworkInterface iface : _vds.getInterfaces()) {
                statistics.add(iface.getStatistics());
            }
            if (!statistics.isEmpty()) {
                TransactionSupport.executeInScope(TransactionScopeOption.Required,
                        new TransactionMethod<Void>() {

                            @Override
                            public Void runInTransaction() {
                                DbFacade.getInstance().getInterfaceDAO().massUpdateStatisticsForVds(statistics);
                                return null;
                            }
                        });
            }
        }

        updateAllInTransaction(_vmDynamicToSave.values(), DbFacade.getInstance().getVmDynamicDAO());
        updateAllInTransaction(_vmStatisticsToSave.values(), DbFacade.getInstance().getVmStatisticsDAO());

        final List<VmNetworkStatistics> allVmInterfaceStatistics = new LinkedList<VmNetworkStatistics>();
        for (List<VmNetworkInterface> list : _vmInterfaceStatisticsToSave.values()) {
            for (VmNetworkInterface iface : list) {
                allVmInterfaceStatistics.add(iface.getStatistics());
            }
        }

        updateAllInTransaction(allVmInterfaceStatistics, DbFacade.getInstance().getVmNetworkStatisticsDAO());
        updateAllInTransaction(_vmDiskImageDynamicToSave.values(), DbFacade.getInstance().getDiskImageDynamicDAO());
        saveVmDevicesToDb();
    }

    private void saveVmDevicesToDb() {

        updateAllInTransaction(vmDeviceToSave.values(), DbFacade.getInstance().getVmDeviceDAO());

        if (!removedDeviceIds.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required,
                    new TransactionMethod<Void>() {

                        @Override
                        public Void runInTransaction() {
                            DbFacade.getInstance().getVmDeviceDAO().removeAll(removedDeviceIds);
                            return null;
                        }
                    });
        }

        if (!newVmDevices.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required,
                    new TransactionMethod<Void>() {

                        @Override
                        public Void runInTransaction() {
                            DbFacade.getInstance().getVmDeviceDAO().saveAll(newVmDevices);
                            return null;
                        }
                    });
        }
    }

    /**
     * Update all the given entities in a transaction, so that a new connection/transaction won't be opened for each
     * entity update.
     *
     * @param <T>
     *            The type of entity.
     * @param entities
     *            The entities to update.
     * @param dao
     *            The DAO used for updating.
     */
    private <T extends BusinessEntity<?>> void updateAllInTransaction(final Collection<T> entities,
            final MassOperationsDao<T> dao) {
        if (!entities.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required,
                    new TransactionMethod<Void>() {

                        @Override
                        public Void runInTransaction() {
                            dao.updateAll(entities);
                            return null;
                        }
                    });
        }
    }

    /**
     * check if value is less than configurable threshold , if yes , generated event log message
     *
     * @param stat
     */
    private void CheckVdsMemoryThreshold(VdsStatistics stat) {

        Integer threshold = Config.GetValue(ConfigValues.LogPhysicalMemoryThresholdInMB);
        if (stat.getmem_available() < threshold) {
            AuditLogableBase logable = new AuditLogableBase();
            logable.setVdsId(stat.getId());
            logable.AddCustomValue("HostName", _vds.getvds_name());
            logable.AddCustomValue("AvailableMemory", stat.getmem_available().toString());
            logable.AddCustomValue("Threshold", threshold.toString());
            AuditLogDirector.log(logable, AuditLogType.VDS_LOW_MEM);
        }
    }

    public VdsUpdateRunTimeInfo(VdsManager vdsManager, VDS vds) {
        _vdsManager = vdsManager;
        _vds = vds;
        _firstStatus = _vds.getstatus();
        monitoringStrategy = MonitoringStrategyFactory.getMonitoringStrategyForVds(vds);
        _vmDict =
                TransactionSupport.executeInScope(TransactionScopeOption.Suppress,
                        new TransactionMethod<Map<Guid, VM>>() {
                            @Override
                            public Map<Guid, VM> runInTransaction() {
                                return DbFacade.getInstance().getVmDAO().getAllRunningByVds(_vds.getId());
                            }
                        });

        for (VM vm : _vmDict.values()) {
            if (vm.isStatusUp() && vm.getstatus() != VMStatus.Up) {
                runningVmsInTransition++;
            }
        }
    }

    public void Refresh() {
        try {
            refreshVdsRunTimeInfo();
        } finally {
            try {
                if (_firstStatus != _vds.getstatus() && _vds.getstatus() == VDSStatus.Up) {
                    // use this lock in order to allow only one host updating DB and
                    // calling UpEvent in a time
                    VdsManager.cancelRecoveryJob(_vds.getId());
                    IrsBrokerCommand.lockDbSave(_vds.getstorage_pool_id());
                    if (log.isDebugEnabled()) {
                        log.debugFormat("vds {0}-{1} firing up event.", _vds.getId(), _vds.getvds_name());
                    }
                    ResourceManager.getInstance().getEventListener().vdsUpEvent(_vds.getId());
                    markIsSetNonOperationalExecuted();

                    // Check hardware capabilities in case VDS moved to up
                    processHardwareCapsNeeded = true;
                }
                // save all data to db
                SaveDataToDb();
            } catch (IRSErrorException ex) {
                logFailureMessage("ResourceManager::refreshVdsRunTimeInfo:", ex);
                if (log.isDebugEnabled()) {
                    log.error(ExceptionUtils.getMessage(ex), ex);
                }
            } catch (RuntimeException ex) {
                logFailureMessage("ResourceManager::refreshVdsRunTimeInfo:", ex);
                log.error(ExceptionUtils.getMessage(ex), ex);
            } finally {
                IrsBrokerCommand.unlockDbSave(_vds.getstorage_pool_id());
            }
        }
    }

    private void logFailureMessage(String messagePrefix, RuntimeException ex) {
        log.errorFormat("{0} Error: {1}, vds = {2} : {3}",
                messagePrefix,
                ExceptionUtils.getMessage(ex),
                _vds.getId(),
                _vds.getvds_name());
    }

    public void AfterRefreshTreatment() {
        try {
            if (processHardwareCapsNeeded) {
                monitoringStrategy.processHardwareCapabilities(_vds);
                markIsSetNonOperationalExecuted();
            }

            if (refreshedCapabilities) {
                ResourceManager.getInstance().getEventListener().handleVdsVersion(_vds.getId());
                markIsSetNonOperationalExecuted();
            }

            if (_vds.getstatus() == VDSStatus.Maintenance) {
                try {
                    ResourceManager.getInstance().getEventListener().vdsMovedToMaintanance(_vds.getId());
                } catch (RuntimeException ex) {
                    log.error("Host encounter a problem moving to maintenance mode. The Host status will change to Non operational status.");
                    ResourceManager
                            .getInstance()
                            .getEventListener()
                            .vdsNonOperational(_vds.getId(), _vds.getNonOperationalReason(), true, true,
                                    Guid.Empty);
                    throw ex;
                }
            } else if (_vds.getstatus() == VDSStatus.NonOperational && _firstStatus != VDSStatus.NonOperational) {

                if (!_vdsManager.isSetNonOperationalExecuted()) {
                    ResourceManager
                            .getInstance()
                            .getEventListener()
                            .vdsNonOperational(_vds.getId(), _vds.getNonOperationalReason(), false, false,
                                    Guid.Empty);
                } else {
                    log.infoFormat("Host {0} : {1} is already in NonOperational status. SetNonOperationalVds command is skipped.",
                            _vds.getId(),
                            _vds.getvds_name());
                }
            }
            // rerun all vms from rerun list
            for (Guid vm_guid : _vmsToRerun) {
                log.errorFormat("Rerun vm {0}. Called from vds {1}", vm_guid, _vds.getvds_name());
                ResourceManager.getInstance().RerunFailedCommand(vm_guid, _vds.getId());

            }
            for (Guid vm_guid : _succededToRunVms) {
                _vdsManager.SuccededToRunVm(vm_guid);
            }
            // run all vms that crushed that marked with auto startup
            for (Guid vm_guid : _autoVmsToRun) {
                // Refrain from auto-start HA VM during its re-run attempts.
                if (!_vmsToRerun.contains(vm_guid)) {
                    ResourceManager.getInstance().getEventListener().runFailedAutoStartVM(vm_guid);
                }
            }

            // process all vms that their ip changed.
            for (java.util.Map.Entry<VM, VmDynamic> pair : _vmsClientIpChanged.entrySet()) {
                ResourceManager.getInstance().getEventListener()
                        .processOnClientIpChange(_vds, pair.getValue().getId());
            }

            // process all vms that powering up.
            for (VmDynamic runningVm : _poweringUpVms) {
                ResourceManager
                        .getInstance()
                        .getEventListener()
                        .processOnVmPoweringUp(_vds.getId(), runningVm.getId(), runningVm.getdisplay_ip(),
                                runningVm.getdisplay());
            }

            // process all vms that went down
            for (Guid vm_guid : _vmsMovedToDown) {
                ResourceManager.getInstance().getEventListener().processOnVmStop(vm_guid);
            }
            for (Guid vm_guid : _vmsToRemoveFromAsync) {
                ResourceManager.getInstance().RemoveAsyncRunningVm(vm_guid);
            }
        } catch (IRSErrorException ex) {
            logFailureMessage("ResourceManager::RerunFailedCommand:", ex);
            if (log.isDebugEnabled()) {
                log.error(ExceptionUtils.getMessage(ex), ex);
            }
        } catch (RuntimeException ex) {
            logFailureMessage("ResourceManager::RerunFailedCommand:", ex);
            log.error(ExceptionUtils.getMessage(ex), ex);
        }
    }

    private void markIsSetNonOperationalExecuted() {
        if (!_vdsManager.isSetNonOperationalExecuted()) {
            VdsDynamic vdsDynamic = DbFacade.getInstance().getVdsDynamicDAO().get(_vds.getId());
            if (vdsDynamic.getstatus() == VDSStatus.NonOperational) {
                _vdsManager.setIsSetNonOperationalExecuted(true);
            }
        }
    }

    private void refreshVdsRunTimeInfo() {
        boolean isVdsUpOrGoingToMaintanance = _vds.getstatus() == VDSStatus.Up
                || _vds.getstatus() == VDSStatus.PreparingForMaintenance || _vds.getstatus() == VDSStatus.Error
                || _vds.getstatus() == VDSStatus.NonOperational;
        try {
            if (isVdsUpOrGoingToMaintanance) {
                // check if its time for statistics refresh
                if (_vdsManager.getRefreshStatistics() || _vds.getstatus() == VDSStatus.PreparingForMaintenance) {
                    refreshVdsStats();
                } else {
                    /**
                     * TODO: Omer if vds team will not implement events to 4.2 please call here to refreshVdsStats -
                     * refresh dynamic data
                     */
                }
            } else {
                // refresh dynamic data
                RefObject<Boolean> tempRefObj = new RefObject<Boolean>(processHardwareCapsNeeded);
                VDSStatus refreshReturnStatus = _vdsManager.refreshCapabilities(tempRefObj, _vds);
                processHardwareCapsNeeded = tempRefObj.argvalue;
                refreshedCapabilities = true;
                if (refreshReturnStatus != VDSStatus.NonOperational) {
                    _vdsManager.setStatus(VDSStatus.Up, _vds);
                }
                _saveVdsDynamic = true;
            }
            beforeFirstRefreshTreatment(isVdsUpOrGoingToMaintanance);
            refreshVmStats();
        } catch (VDSRecoveringException e) {
            // if PreparingForMaintenance and vds is in install failed keep to
            // move vds to maintenance
            if (_vds.getstatus() != VDSStatus.PreparingForMaintenance) {
                throw e;
            }
        }
        MoveVDSToMaintenanceIfNeeded();
    }

    private void refreshVdsStats() {
        if (Config.<Boolean> GetValue(ConfigValues.DebugTimerLogging)) {
            log.debugFormat("vdsManager::refreshVdsStats entered, vds = {0} : {1}", _vds.getId(),
                    _vds.getvds_name());
        }
        // get statistics data, images checks and vm_count data (dynamic)
        GetStatsVDSCommand<VdsIdAndVdsVDSCommandParametersBase> vdsBrokerCommand =
                new GetStatsVDSCommand<VdsIdAndVdsVDSCommandParametersBase>(new VdsIdAndVdsVDSCommandParametersBase(_vds));
        vdsBrokerCommand.Execute();
        if (!vdsBrokerCommand.getVDSReturnValue().getSucceeded()
                && vdsBrokerCommand.getVDSReturnValue().getExceptionObject() != null) {
            VDSNetworkException ex =
                    (VDSNetworkException) ((vdsBrokerCommand.getVDSReturnValue().getExceptionObject() instanceof VDSNetworkException) ? vdsBrokerCommand
                            .getVDSReturnValue().getExceptionObject()
                            : null);
            if (ex != null) {
                if (_vdsManager.handleNetworkException(ex, _vds)) {
                    _saveVdsDynamic = true;
                }
                log.errorFormat("vds::refreshVdsStats Failed getVdsStats,  vds = {0} : {1}, error = {2}",
                        _vds.getId(), _vds.getvds_name(), ExceptionUtils.getMessage(ex));
            } else {
                log.errorFormat("vds::refreshVdsStats Failed getVdsStats,  vds = {0} : {1}, error = {2}",
                        _vds.getId(), _vds.getvds_name(), vdsBrokerCommand.getVDSReturnValue().getExceptionString());
            }
            throw vdsBrokerCommand.getVDSReturnValue().getExceptionObject();
        }
        // save also dynamic because vm_count data and image_check getting with
        // statistics data
        /**
         * TODO: omer- one day remove dynamic save when possible please check if vdsDynamic changed before save
         */
        _saveVdsDynamic = true;
        _saveVdsStatistics = true;

        alertIfLowDiskSpaceOnHost();
        checkVdsInterfaces();

        if (Config.<Boolean> GetValue(ConfigValues.DebugTimerLogging)) {
            log.debugFormat("vds::refreshVdsStats\n{0}", toString());
        }
    }

    /**
     * Log to the audit log in case one/some of the paths monitored by VDSM are low on disk space.
     */
    private void alertIfLowDiskSpaceOnHost() {
        Map<String, Long> disksUsage = _vds.getLocalDisksUsage();
        if (disksUsage == null || disksUsage.isEmpty()) {
            return;
        }

        List<String> disksWithLowSpace = new ArrayList<String>();
        List<String> disksWithCriticallyLowSpace = new ArrayList<String>();
        for (Entry<String, Long> diskUsage : disksUsage.entrySet()) {
            if (diskUsage.getValue() != null) {
                if (diskUsage.getValue() <= LOW_SPACE_CRITICAL_THRESHOLD) {
                    disksWithCriticallyLowSpace.add(diskUsage.getKey());
                } else if (diskUsage.getValue() <= LOW_SPACE_THRESHOLD) {
                    disksWithLowSpace.add(diskUsage.getKey());
                }
            }
        }

        logLowDiskSpaceOnHostDisks(disksWithLowSpace, LOW_SPACE_THRESHOLD, AuditLogType.VDS_LOW_DISK_SPACE);
        logLowDiskSpaceOnHostDisks(disksWithCriticallyLowSpace,
                LOW_SPACE_CRITICAL_THRESHOLD,
                AuditLogType.VDS_LOW_DISK_SPACE_ERROR);
    }

    /**
     * Log that the disks have low space, if the disks list is not empty.
     *
     * @param disksWithLowSpace
     *            The disks with the low space.
     * @param lowSpaceThreshold
     *            The low space threshold that below it we log.
     * @param logType
     *            The type of log to use.
     */
    private void logLowDiskSpaceOnHostDisks(List<String> disksWithLowSpace,
            final Integer lowSpaceThreshold,
            AuditLogType logType) {
        if (!disksWithLowSpace.isEmpty()) {
            AuditLogableBase logable = new AuditLogableBase(_vds.getId());
            logable.AddCustomValue("DiskSpace", lowSpaceThreshold.toString());
            logable.AddCustomValue("Disks", StringUtils.join(disksWithLowSpace, ", "));
            AuditLogDirector.log(logable, logType);
        }
    }

    // Check if one of the Host interfaces is down, we set the host to non-operational
    // We cannot have Host that don't have all networks in cluster in status Up
    private void checkVdsInterfaces() {
        if (_vds.getstatus() != VDSStatus.Up) {
            return;
        }
        Map<String, Boolean> activeBonds = new HashMap<String, Boolean>();
        List<network> clusterNetworks = DbFacade.getInstance().getNetworkDAO()
                .getAllForCluster(_vds.getvds_group_id());
        boolean setHostDown = false;
        List<String> networks = new ArrayList<String>();
        List<String> nics = new ArrayList<String>();
        Map<String, List<String>> bondNics = new HashMap<String, List<String>>();

        ArrayList<VdsNetworkInterface> interfaces = _vds.getInterfaces();
        Map<String, network> networksByName = NetworkUtils.networksByName(clusterNetworks);

        try {
            for (VdsNetworkInterface iface : interfaces) {
                // report if MTU value differ from cluster
                logMTUDifferences(networksByName, iface);

                // Handle nics that are non bonded and not vlan over bond
                setHostDown = isInteraceDown(clusterNetworks, networks, nics, iface);

                // Handle bond nics
                if (iface.getBondName() != null) {
                    poplate(activeBonds, clusterNetworks, networks, bondNics, iface);
                }
            }

            // check the bond statuses, if one is down we set the host to down
            // only if we didn't already set the host to down
            if (!setHostDown) {
                for (String key : activeBonds.keySet()) {
                    if (!activeBonds.get(key)) {
                        setHostDown = true;
                        // add the nics name for audit log
                        for (String name : bondNics.get(key)) {
                            nics.add(name);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(String.format("Failure on checkInterfaces on update runtimeinfo for vds: %s", _vds.getvds_name()),
                    e);
        } finally {
            if (setHostDown) {
                // we give 1 minutes to a nic to get up in case the nic get the ip from DHCP server
                if (!hostDownTimes.containsKey(_vds.getId())) {
                    hostDownTimes.put(_vds.getId(), System.currentTimeMillis());
                    return;
                } else {
                    int delay = Config.<Integer> GetValue(ConfigValues.NicDHCPDelayGraceInMS) * 1000;
                    if (System.currentTimeMillis() < hostDownTimes.get(_vds.getId()) + delay) {
                        // if less then 1 minutes, still waiting for DHCP
                        return;
                    } else {
                        // else remove from map (for future checks) and set the host to non-operational
                        hostDownTimes.remove(_vds.getId());
                    }
                }
                try {
                    StringBuilder sNics = new StringBuilder();
                    StringBuilder sNetworks = new StringBuilder();

                    for (String nic : nics) {
                        sNics.append(nic)
                                .append(", ");
                    }
                    for (String net : networks) {
                        sNetworks.append(net)
                                .append(", ");
                    }

                    String message =
                            String.format(
                                    "Host '%s' moved to Non-Operational state because interface/s '%s' are down which needed by network/s '%s' in the current cluster",
                                    _vds.getvds_name(),
                                    sNics.toString(),
                                    sNetworks.toString());

                    _vdsManager.setStatus(VDSStatus.NonOperational, _vds);
                    log.info(message);

                    AuditLogableBase logable = new AuditLogableBase(_vds.getId());
                    logable.AddCustomValue("Networks", StringHelper.trimEnd(sNetworks.toString(), ',', ' '));
                    logable.AddCustomValue("Interfaces", StringHelper.trimEnd(sNics.toString(), ',', ' '));
                    AuditLogDirector.log(logable, AuditLogType.VDS_SET_NONOPERATIONAL_IFACE_DOWN);
                } catch (Exception e) {
                    log.error(String.format("checkInterface: Failure on moving host: %s to non-operational.",
                            _vds.getvds_name()),
                            e);
                }
            } else {
                // no nics are down, remove from list if exists
                hostDownTimes.remove(_vds.getId());
            }
        }
    }

    /**
     * audit networks which have different values from what the logical network dictates. logical network MTU = 0 is for
     * using the host default MTU so no need to warn the admin.
     * @param clusterNetworkByName
     * @param interfaces
     */
    public void logMTUDifferences(Map<String, network> clusterNetworkByName,
            VdsNetworkInterface iface) {
        if (iface.getNetworkName() != null && clusterNetworkByName.containsKey(iface.getNetworkName()) &&
                clusterNetworkByName.get(iface.getNetworkName()).getMtu() != 0 &&
                iface.getMtu() != clusterNetworkByName.get(iface.getNetworkName()).getMtu()) {
            AuditLogableBase logable = new AuditLogableBase();
            logable.AddCustomValue(NetworkName, iface.getNetworkName());
            logable.AddCustomValue(HostNetworkMTU, String.valueOf(iface.getMtu()));
            logable.AddCustomValue(LogicalNetworkMTU,
                    String.valueOf(clusterNetworkByName.get(iface.getNetworkName()).getMtu()));
            AuditLogDirector.log(logable, AuditLogType.VDS_NETWORK_MTU_DIFFER_FROM_LOGICAL_NETWORK);
        }
    }

    private void poplate(Map<String, Boolean> activeBonds,
            List<network> clusterNetworks,
            List<String> networks,
            Map<String, List<String>> bondNics,
            VdsNetworkInterface iface) {
        Pair<Boolean, String> retVal =
                IsNetworkInCluster(iface.getBondName(), clusterNetworks);
        String networkName = retVal.getSecond();
        if (retVal.getFirst()) {
            if (!activeBonds.containsKey(iface.getBondName())) {
                activeBonds.put(iface.getBondName(), false);
            }
            activeBonds.put(iface.getBondName(),
                    activeBonds.get(iface.getBondName())
                            || (iface.getStatistics().getStatus() == InterfaceStatus.Up));

            if (!networks.contains(networkName)
                    && !activeBonds.containsKey(iface.getName())) {
                networks.add(networkName);
            }
            // we remove the network from the audit log if the bond
            // is active
            else if (networks.contains(networkName)
                    && activeBonds.containsKey(iface.getBondName())) {
                networks.remove(networkName);
            }
            if (!bondNics.containsKey(iface.getBondName())) {
                bondNics.put(iface.getBondName(),
                        new ArrayList<String>());
            }
            bondNics.get(iface.getBondName()).add(iface.getName());
        }
    }

    private boolean isInteraceDown(List<network> clusterNetworks,
            List<String> networks,
            List<String> nics,
            VdsNetworkInterface iface) {
        if (iface.getStatistics().getStatus() != InterfaceStatus.Up
                && iface.getNetworkName() != null
                && iface.getBonded() == null
                && !isBondOrVlanOverBond(iface)) {
            // check if the network require by the cluster
            for (network net : clusterNetworks) {
                if (net.getStatus() == NetworkStatus.Operational &&
                        net.getname().equals(iface.getNetworkName()) &&
                        (iface.getVlanId() == null || !isVlanInterfaceUp(iface))) {
                    networks.add(iface.getNetworkName());
                    nics.add(iface.getName());
                    return true;
                }
            }
        }
        return false;
    }

    // method get bond name, list of cluster network - checks if the specified
    // bonds network is in the clusterNetworks,
    // if so return true and networkName of the bonds
    private Pair<Boolean, String> IsNetworkInCluster(String bondName, List<network> clusterNetworks) {
        Pair<Boolean, String> retVal = new Pair<Boolean, String>();
        for (VdsNetworkInterface iface : _vds.getInterfaces()) {
            if (iface.getName().equals(bondName)) {
                for (network net : clusterNetworks) {
                    if (net.getname().equals(iface.getNetworkName())
                            || isVlanOverBondNetwork(bondName, net.getname())) {
                        retVal.setFirst(true);
                        retVal.setSecond(net.getname());
                        return retVal;
                    }
                }
                retVal.setFirst(false);
                return retVal;
            }
        }
        retVal.setFirst(false);
        return retVal;
    }

    // IsBond return true if the interface is bond,
    // it also check if it's vlan over bond and return true in that case
    // i.e. it return true in case of bond0 and bond0.5
    private boolean isBondOrVlanOverBond(VdsNetworkInterface iface) {
        if (iface.getBonded() != null && iface.getBonded() == true) {
            return true;
        }

        // check if vlan over bond i.e if we are in bond0.5 we look for bond0
        String name = NetworkUtils.getVlanInterfaceName(iface.getName());
        if (name == null) {
            return false;
        }

        for (VdsNetworkInterface i : _vds.getInterfaces()) {
            if (name.equals(i.getName())) {
                return (i.getBonded() != null && i.getBonded() == true);
            }
        }
        return false;
    }

    // function check if vlan over bond connected to network
    // i.e. if we have bond0 that have vlan #5 like:
    // bond0 and bond0.5
    // bond0 is not connectet to network just the bond0.5 is connected to network
    // and this method check for that case
    private boolean isVlanOverBondNetwork(String bondName, String networkName) {
        for (VdsNetworkInterface iface : _vds.getInterfaces()) {
            String name = NetworkUtils.getVlanInterfaceName(iface.getName());
            // this if check if the interface is vlan
            if (name == null) {
                continue;
            } else if (name.equals(bondName)
                    && networkName.equals(iface.getNetworkName())) {
                return true;
            }
        }
        return false;
    }

    // If vlan we search if the interface is up (i.e. not eth2.5 we look for eth2)
    private boolean isVlanInterfaceUp(VdsNetworkInterface vlan) {
        String[] tokens = vlan.getName().split("[.]");
        if (tokens.length == 1) {
            // not vlan
            return true;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length - 1; i++) {
            sb.append(tokens[i])
                    .append(".");
        }
        String ifaceName = StringHelper.trimEnd(sb.toString(), '.');
        for (VdsNetworkInterface iface : _vds.getInterfaces()) {
            if (iface.getName().equals(ifaceName)) {
                return iface.getStatistics().getStatus() == InterfaceStatus.Up;
            }
        }

        // not suppose to get here
        return false;
    }

    private void beforeFirstRefreshTreatment(boolean isVdsUpOrGoingToMaintanance) {
        if (_vdsManager.getbeforeFirstRefresh()) {
            boolean flagsChanged = false;
            RefObject<Boolean> tempRefObject = new RefObject<Boolean>(flagsChanged);
            _vdsManager.refreshCapabilities(tempRefObject, _vds);
            flagsChanged = tempRefObject.argvalue;
            _vdsManager.setbeforeFirstRefresh(false);
            refreshedCapabilities = true;
            _saveVdsDynamic = true;
            // change the _cpuFlagsChanged flag only if it was false,
            // because get capabilities is called twice on a new server in same
            // loop!
            processHardwareCapsNeeded = (processHardwareCapsNeeded) ? processHardwareCapsNeeded : flagsChanged;
        } else if (isVdsUpOrGoingToMaintanance || _vds.getstatus() == VDSStatus.Error) {
            return;
        }
        AuditLogableBase logable = new AuditLogableBase(_vds.getId());
        logable.AddCustomValue("VdsStatus", EnumUtils.ConvertToStringWithSpaces(_vds.getstatus().toString()));
        AuditLogDirector.log(logable, AuditLogType.VDS_DETECTED);
    }

    private void refreshVmStats() {
        if (Config.<Boolean> GetValue(ConfigValues.DebugTimerLogging)) {
            log.debug("vds::refreshVmList entered");
        }

        VdsBrokerCommand<VdsIdAndVdsVDSCommandParametersBase> command;
        if (!_vdsManager.getRefreshStatistics()) {
            command = new ListVDSCommand<VdsIdAndVdsVDSCommandParametersBase>(
                    new VdsIdAndVdsVDSCommandParametersBase(_vds));
        } else {
            command = new GetAllVmStatsVDSCommand<VdsIdAndVdsVDSCommandParametersBase>(
                    new VdsIdAndVdsVDSCommandParametersBase(_vds));
        }
        _runningVms = (java.util.HashMap<Guid, java.util.Map.Entry<VmDynamic, VmStatistics>>) command
                .ExecuteWithReturnValue();

        if (command.getVDSReturnValue().getSucceeded()) {
            java.util.List<VM> running = checkVmsStatusChanged();

            proceedDownVms();

            // update repository and check if there are any vm in cache that not
            // in vdsm
            updateRepository(running);
            // Going over all returned VMs and updting the data structures
            // accordingly

            // checking the db for incoherent vm status;
            // setVmStatusDownForVmNotFound();

            refreshCommitedMemory();
            if (_vdssToRefresh != null) {
                for (Guid vdsToRefreshId : _vdssToRefresh) {
                    VdsManager vdsm = ResourceManager.getInstance().GetVdsManager(vdsToRefreshId);
                    vdsm.forceRefreshRunTimeInfo();
                }
            }
            // Handle VM devices were changed (for 3.1 cluster and above)
            if (!VmDeviceCommonUtils.isOldClusterVersion(_vds.getvds_group_compatibility_version())) {
                handleVmDeviceChange();
            }
        } else if (command.getVDSReturnValue().getExceptionObject() != null) {
            if (command.getVDSReturnValue().getExceptionObject() instanceof VDSErrorException) {
                log.errorFormat("Failed vds listing,  vds = {0} : {1}, error = {2}", _vds.getId(),
                        _vds.getvds_name(), command.getVDSReturnValue().getExceptionString());
            } else if (command.getVDSReturnValue().getExceptionObject() instanceof VDSNetworkException) {
                _saveVdsDynamic = _vdsManager.handleNetworkException((VDSNetworkException) command.getVDSReturnValue()
                        .getExceptionObject(), _vds);
            } else if (command.getVDSReturnValue().getExceptionObject() instanceof VDSProtocolException) {
                log.errorFormat("Failed vds listing,  vds = {0} : {1}, error = {2}", _vds.getId(),
                        _vds.getvds_name(), command.getVDSReturnValue().getExceptionString());
            } else if (command.getVDSReturnValue().getExceptionObject() instanceof RepositoryException) {
                log.errorFormat("Failed to update vms status in database,  vds = {0} : {1}, error = {2}",
                        _vds.getId(), _vds.getvds_name(), command.getVDSReturnValue().getExceptionString());
                log.error("Exception: ", command.getVDSReturnValue().getExceptionObject());
                return;
            }
            throw command.getVDSReturnValue().getExceptionObject();
        } else {
            log.errorFormat("refreshCapabilities:GetCapabilitiesVDSCommand failed with no exception!");
        }
    }

    /**
     * Handle changes in all VM devices
     */
    private void handleVmDeviceChange() {
        List<String> vmsToUpdate = new ArrayList<String>();
        for (Map.Entry<VmDynamic, VmStatistics> vmHelper : _runningVms.values()) {
            VmDynamic vmDynamic = vmHelper.getKey();
            if (vmDynamic != null) {
                VM vm = _vmDict.get(vmDynamic.getId());
                if (vm != null) {
                    String dbHash = vm.getHash();
                    if ((dbHash == null && vmDynamic.getHash() != null) || (dbHash != null)
                            && !dbHash.equals(vmDynamic.getHash())) {
                        vmsToUpdate.add(vmDynamic.getId().toString());
                        // update new hash value
                        if (_vmDynamicToSave.containsKey(vm.getId())) {
                            _vmDynamicToSave.get(vm.getId()).setHash(vmDynamic.getHash());
                        } else {
                            AddVmDynamicToList(vmDynamic);
                        }
                    }
                }
            }
        }
        if (vmsToUpdate.size() > 0) {
            updateVmDevices(vmsToUpdate);
        }
    }

    /**
     * Update the given list of VMs properties in DB
     *
     * @param vmsToUpdate
     */
    private void updateVmDevices(List<String> vmsToUpdate) {
        XmlRpcStruct[] vms = getVmInfo(vmsToUpdate);
        updateVmDevices(vms);
    }

    /**
     * gets VM full information for the given list of VMs
     * @param vmIds
     * @return
     */
    private XmlRpcStruct[] getVmInfo(List<String> vmsToUpdate) {
        return (XmlRpcStruct[]) (new FullListVdsCommand<FullListVDSCommandParameters>(
                new FullListVDSCommandParameters(_vds.getId(), vmsToUpdate)).ExecuteWithReturnValue());
    }

    /**
     * Update VMs devices
     * @param vms
     */
    private void updateVmDevices(XmlRpcStruct[] vms) {
        for (XmlRpcStruct vm : vms) {
            processVmDevices(vm);
        }
    }

    /**
     * Actually process the VM device update in DB.
     * @param vm
     */
    private void processVmDevices(XmlRpcStruct vm) {
        if (vm == null || vm.getItem(VdsProperties.vm_guid) == null) {
            log.errorFormat("Recieved NULL VM or VM id when processing VM devices, abort.");
            return;
        }
        Guid vmId = new Guid((String) vm.getItem(VdsProperties.vm_guid));
        HashSet<Guid> processedDevices = new HashSet<Guid>();
        Object[] objects = (Object[]) vm.getItem(VdsProperties.Devices);
        List<VmDevice> devices = DbFacade.getInstance().getVmDeviceDAO().getVmDeviceByVmId(vmId);
        Map<VmDeviceId, VmDevice> deviceMap = new HashMap<VmDeviceId, VmDevice>();
        for (VmDevice device : devices) {
            deviceMap.put(device.getId(), device);
        }
        for (Object o : objects) {
            XmlRpcStruct device = new XmlRpcStruct((Map<String, Object>) o);
            Guid deviceId = getDeviceId(device);
            if ((device.getItem(VdsProperties.Address)) == null) {
                log.infoFormat("Recieved a Device without an address when processing VM {0} devices, skipping device: {1}.",
                        vmId,
                        device.getInnerMap().toString());
                continue;
            }
            VmDevice vmDevice = deviceMap.get(new VmDeviceId(deviceId, vmId));
            if (deviceId == null || vmDevice == null) {
                deviceId = addNewVmDevice(vmId, device);
            } else {
                vmDevice.setAddress(((Map<String, String>) device.getItem(VdsProperties.Address)).toString());
                addVmDeviceToList(vmDevice);
            }
            processedDevices.add(deviceId);
        }
        handleRemovedDevices(vmId, processedDevices, devices);
    }

    /**
     * Removes unmanaged devices from DB if were removed by libvirt. Empties device address with isPlugged = false
     * @param vmId
     * @param processedDevices
     */
    private void handleRemovedDevices(Guid vmId, HashSet<Guid> processedDevices, List<VmDevice> devices) {
        for (VmDevice device : devices) {
            if (!processedDevices.contains(device.getDeviceId())) {
                if (device.getIsManaged()) {
                    if (device.getIsPlugged()) {
                        log.errorFormat("VM {0} managed non plugable device was removed unexpetedly from libvirt: {1}",
                                vmId, device.toString());
                    } else {
                        device.setAddress("");
                        addVmDeviceToList(device);
                        log.debugFormat("VM {0} unmanaged pluggable device was unplugged : {1}",
                                vmId,
                                device.toString());
                    }
                } else {
                    removedDeviceIds.add(device.getId());
                    log.debugFormat("VM {0} unmanaged device was marked for remove : {1}", vmId, device.toString());
                }
            }
        }
    }

    /**
     * Adds new devices recognized by libvirt
     * @param vmId
     * @param device
     */
    private Guid addNewVmDevice(Guid vmId, XmlRpcStruct device) {
        Guid newDeviceId = Guid.Empty;
        String typeName = (String) device.getItem(VdsProperties.Type);
        String deviceName = (String) device.getItem(VdsProperties.Device);
        // do not allow null or empty device or type values
        if (!StringUtils.isEmpty(typeName) && !StringUtils.isEmpty(deviceName)) {
            String address = ((Map<String, String>) device.getItem(VdsProperties.Address)).toString();
            String specParams = "";
            Object o = device.getItem(VdsProperties.SpecParams);
            newDeviceId = Guid.NewGuid();
            if (o != null) {
                specParams = org.ovirt.engine.core.utils.StringUtils.map2String((Map<String, String>) o);
            }
            VmDeviceId id = new VmDeviceId(newDeviceId, vmId);
            VmDevice newDevice = new VmDevice(id, typeName, deviceName, address,
                    0,
                    specParams,
                    false,
                    true,
                    false);
            newVmDevices.add(newDevice);
            log.debugFormat("New device was marked for adding to VM {0} Devices : {1}", vmId, newDevice.toString());
        } else {
            log.errorFormat("Empty or NULL values were passed for a VM {0} device, Device is skipped", vmId);
        }
        return newDeviceId;
    }

    /**
     * gets the device id from the structure returned by VDSM device ids are stored in specParams map
     * @param device
     * @return
     */
    private Guid getDeviceId(XmlRpcStruct device) {
        Map<String, String> specParams = (Map<String, String>) device.getItem("specParams");
        if (specParams == null)
            return null;
        else
            return new Guid(specParams.get("deviceId"));
    }

    // if not statistics check if status changed and add to running list
    private java.util.List<VM> checkVmsStatusChanged() {
        java.util.List<VM> running = new java.util.ArrayList<VM>();
        if (!_vdsManager.getRefreshStatistics()) {
            // LINQ new List<VmDynamic>(_runningVms.Values.Select(a => a.Key));
            java.util.ArrayList<VmDynamic> tempRunningList = new java.util.ArrayList<VmDynamic>();
            for (java.util.Map.Entry<VmDynamic, VmStatistics> runningVm : _runningVms.values()) {
                tempRunningList.add(runningVm.getKey());
            }
            for (VmDynamic runningVm : tempRunningList) {
                VM vmToUpdate = null;
                vmToUpdate = _vmDict.get(runningVm.getId());

                if (vmToUpdate == null
                        || (vmToUpdate.getstatus() != runningVm.getstatus() && !(vmToUpdate.getstatus() == VMStatus.SavingState && runningVm
                                .getstatus() == VMStatus.Up))) {
                    GetVmStatsVDSCommand<GetVmStatsVDSCommandParameters> command =
                            new GetVmStatsVDSCommand<GetVmStatsVDSCommandParameters>(new GetVmStatsVDSCommandParameters(
                                    _vds, runningVm.getId()));
                    command.Execute();
                    if (command.getVDSReturnValue().getSucceeded()) {
                        _runningVms.put(runningVm.getId(),
                                (java.util.Map.Entry<VmDynamic, VmStatistics>) command.getReturnValue());
                    } else {
                        _runningVms.remove(runningVm.getId());
                    }
                } else {
                    // status not changed move to next vm
                    running.add(vmToUpdate);
                    _runningVms.remove(vmToUpdate.getId());
                }
            }
        }
        return running;
    }

    /**
     * Delete all vms with status Down
     */
    private void proceedDownVms() {
        // LINQ
        // foreach (VmDynamic vm in _runningVms.Values.Select(a =>
        // a.Key).Where(a => a.status == VMStatus.Down))
        for (java.util.Map.Entry<VmDynamic, VmStatistics> vm_helper : _runningVms.values()) {
            VmDynamic vm = vm_helper.getKey();
            if (vm.getstatus() != VMStatus.Down) {
                continue;
            }

            VM vmTo = null;
            // _vdsManager.getVm(vm.getvm_guid());
            vmTo = _vmDict.get(vm.getId());
            VMStatus status = VMStatus.Unassigned;
            if (vmTo != null) {
                status = vmTo.getstatus();
                proceedVmBeforeDeletion(vmTo, vm);

                // when going to suspend, delete vm from cache later
                if (status == VMStatus.SavingState) {
                    ResourceManager.getInstance().InternalSetVmStatus(vmTo, VMStatus.Suspended);
                }

                clearVm(vmTo);
            }

            VmStatistics vmStatistics = DbFacade.getInstance().getVmStatisticsDAO().get(vm.getId());
            if (vmStatistics != null) {
                DestroyVDSCommand<DestroyVmVDSCommandParameters> vdsBrokerCommand =
                        new DestroyVDSCommand<DestroyVmVDSCommandParameters>(new DestroyVmVDSCommandParameters(
                                _vds.getId(), vm.getId(), false, false, 0));
                vdsBrokerCommand.Execute();

                if (vmTo != null && status == VMStatus.SavingState) {
                    AfterSuspendTreatment(vm);
                } else if (status != VMStatus.MigratingFrom) {
                    HandleVmOnDown(vmTo, vm, vmStatistics);
                }
            }
        }
    }

    private void HandleVmOnDown(VM cacheVm, VmDynamic vmDynamic, VmStatistics vmStatistics) {
        AuditLogType type = vmDynamic.getExitStatus() == VmExitStatus.Normal ? AuditLogType.VM_DOWN
                : AuditLogType.VM_DOWN_ERROR;
        AuditLogableBase logable = new AuditLogableBase(_vds.getId(), vmStatistics.getId());
        if (vmDynamic.getExitStatus() != VmExitStatus.Normal) {
            logable.AddCustomValue("ExitMessage", vmDynamic.getExitMessage());
            AuditLogDirector.log(logable, type);
        }
        if (vmDynamic.getExitStatus() != VmExitStatus.Normal) {
            /**
             * Vm failed to run - try to rerun it on other Vds
             */
            if (cacheVm != null) {
                if (ResourceManager.getInstance().IsVmInAsyncRunningList(vmDynamic.getId())) {
                    log.infoFormat("Running on vds during rerun failed vm: {0}", vmDynamic.getrun_on_vds());
                    _vmsToRerun.add(vmDynamic.getId());
                } else if (cacheVm.getauto_startup()) {
                    _autoVmsToRun.add(vmDynamic.getId());
                }
            }
            // if failed in destination right after migration
            else // => cacheVm == null
            {
                if (ResourceManager.getInstance().IsVmInAsyncRunningList(vmDynamic.getId())) {
                    ResourceManager.getInstance().RemoveAsyncRunningVm(vmDynamic.getId());
                }
                AddVmDynamicToList(vmDynamic);
            }
        } else {
            /**
             * Vm moved safely to down status. May be migration - just remove it from Async Running command.
             */
            ResourceManager.getInstance().RemoveAsyncRunningVm(vmDynamic.getId());
        }
    }

    private void AfterSuspendTreatment(VmDynamic vm) {
        AuditLogType type = vm.getExitStatus() == VmExitStatus.Normal ? AuditLogType.USER_SUSPEND_VM_OK
                : AuditLogType.USER_FAILED_SUSPEND_VM;

        AuditLogableBase logable = new AuditLogableBase(_vds.getId(), vm.getId());
        AuditLogDirector.log(logable, type);
        ResourceManager.getInstance().RemoveAsyncRunningVm(vm.getId());
    }

    private void proceedVmBeforeDeletion(VM curVm, VmDynamic vmDynamic) {
        AuditLogType type = AuditLogType.UNASSIGNED;
        AuditLogableBase logable = new AuditLogableBase(_vds.getId(), curVm.getId());
        switch (curVm.getstatus()) {
        case MigratingFrom: {
            if (vmDynamic == null || vmDynamic.getExitStatus() == VmExitStatus.Normal) {
                // type = AuditLogType.VM_MIGRATION_DONE;
                migratingFromTreatment(curVm);
            } else {
                if (curVm.getmigrating_to_vds() != null) {
                    DestroyVmVDSCommand destroyCmd = new DestroyVmVDSCommand(new DestroyVmVDSCommandParameters(
                            new Guid(curVm.getmigrating_to_vds().toString()), curVm.getId(), true, false, 0));
                    destroyCmd.Execute();
                    if (destroyCmd.getVDSReturnValue().getSucceeded()) {
                        log.infoFormat("Stopped migrating vm: {0} on vds: {1}", curVm.getvm_name(),
                                curVm.getmigrating_to_vds());
                    } else {
                        log.infoFormat("Could not stop migrating vm: {0} on vds: {1}, Error: {2}", curVm.getvm_name(),
                                curVm.getmigrating_to_vds(), destroyCmd.getVDSReturnValue().getExceptionString());
                    }
                }
                // set vm status to down if source vm crushed
                ResourceManager.getInstance().InternalSetVmStatus(curVm, VMStatus.Down);
                AddVmDynamicToList(curVm.getDynamicData());
                AddVmStatisticsToList(curVm.getStatisticsData());
                AddVmInterfaceStatisticsToList(curVm.getInterfaces());
                type = AuditLogType.VM_MIGRATION_ABORT;
                logable.AddCustomValue("MigrationError", vmDynamic.getExitMessage());

                ResourceManager.getInstance().RemoveAsyncRunningVm(vmDynamic.getId());
            }
            break;
        }
        case PoweredDown: {
            logable.AddCustomValue("VmStatus", "PoweredDown");
            type = AuditLogType.VM_DOWN;
            break;
        }
        default:
            break;
        }
        if (type != AuditLogType.UNASSIGNED) {
            AuditLogDirector.log(logable, type);
        }
    }

    private void migratingFromTreatment(VM curVm) {
        if (curVm.getmigrating_to_vds() != null) {
            if (_vdssToRefresh == null) {
                _vdssToRefresh = new java.util.ArrayList<Guid>();
            }
            if (!_vdssToRefresh.contains(curVm.getmigrating_to_vds())) {
                _vdssToRefresh.add(new Guid(curVm.getmigrating_to_vds().toString()));
            }
        }
    }

    private void updateRepository(java.util.List<VM> running) {
        // LINQ
        // foreach (VmDynamic runningVm in _runningVms.Values.Select(a =>
        // a.Key))
        for (java.util.Map.Entry<VmDynamic, VmStatistics> vm_helper : _runningVms.values()) {
            VmDynamic runningVm = vm_helper.getKey();
            VM vmToUpdate = null;
            vmToUpdate = _vmDict.get(runningVm.getId());

            // launch powerclient on clientIp change logic
            // if not migrating here and not down
            if (!inMigrationTo(runningVm, vmToUpdate) && runningVm.getstatus() != VMStatus.Down) {
                if (vmToUpdate != null) {
                    if (_vmDict.containsKey(vmToUpdate.getId())
                            && !StringHelper.EqOp(runningVm.getclient_ip(), vmToUpdate.getclient_ip())) {
                        _vmsClientIpChanged.put(vmToUpdate, runningVm);
                    }
                }
                if (vmToUpdate != null) {
                    // open spice for dedicated VMs
                    if (vmToUpdate.getstatus() != VMStatus.Up && runningVm.getstatus() == VMStatus.Up
                            || vmToUpdate.getstatus() != VMStatus.PoweringUp
                            && runningVm.getstatus() == VMStatus.PoweringUp) {
                        // Vm moved to powering Up or up status - launch spice
                        // if no current client ip already connected.
                        if (runningVm.getdisplay() != null) {
                            _poweringUpVms.add(runningVm);
                        } else {
                            log.errorFormat("VdsBroker.VdsUpdateRunTimeInfo.updateRepository - runningVm.display is null, cannot start spice for it");
                        }
                    }

                    if (vmToUpdate.getstatus() != VMStatus.Up && vmToUpdate.getstatus() != VMStatus.MigratingFrom
                            && runningVm.getstatus() == VMStatus.Up) {
                        // Vm moved to Up status - remove its record from Async
                        // running handling
                        if (!_succededToRunVms.contains(vmToUpdate.getId())) {
                            _succededToRunVms.add(vmToUpdate.getId());
                        }
                    }
                    afterMigrationFrom(runningVm, vmToUpdate);

                    if (vmToUpdate.getstatus() != VMStatus.NotResponding
                            && runningVm.getstatus() == VMStatus.NotResponding) {
                        AuditLogableBase logable = new AuditLogableBase(_vds.getId(), vmToUpdate.getId());
                        AuditLogDirector.log(logable, AuditLogType.VM_NOT_RESPONDING);
                    }
                    /**
                     * check if vm is suspended and remove it from async list
                     */
                    else if (runningVm.getstatus() == VMStatus.Paused) {
                        _vmsToRemoveFromAsync.add(vmToUpdate.getId());
                        if (vmToUpdate.getstatus() != VMStatus.Paused) {
                            // check exit message to determine wht the vm has
                            // paused
                            AuditLogType logType = AuditLogType.UNASSIGNED;
                            AuditLogableBase logable = new AuditLogableBase(_vds.getId(), vmToUpdate.getId());
                            VmPauseStatus pauseStatus = runningVm.getPauseStatus();
                            if (pauseStatus.equals(VmPauseStatus.NOERR) || pauseStatus.equals(VmPauseStatus.NONE)) {
                                // user requested pause, no log needed
                            } else if (pauseStatus == VmPauseStatus.ENOSPC) {
                                logType = AuditLogType.VM_PAUSED_ENOSPC;
                            } else if (pauseStatus == VmPauseStatus.EIO) {
                                logType = AuditLogType.VM_PAUSED_EIO;
                            } else if (pauseStatus == VmPauseStatus.EPERM) {
                                logType = AuditLogType.VM_PAUSED_EPERM;
                            } else {
                                logType = AuditLogType.VM_PAUSED_ERROR;
                            }
                            if (logType != AuditLogType.UNASSIGNED) {
                                AuditLogDirector.log(logable, logType);
                            }
                        }

                    }
                }
                if (vmToUpdate != null || runningVm.getstatus() != VMStatus.MigratingFrom) {
                    RefObject<VM> tempRefObj = new RefObject<VM>(vmToUpdate);
                    boolean updateSucceed = UpdateVmRunTimeInfo(tempRefObj, runningVm);
                    vmToUpdate = tempRefObj.argvalue;
                    if (updateSucceed) {
                        AddVmDynamicToList(vmToUpdate.getDynamicData());
                    }
                }
                if (vmToUpdate != null) {
                    UpdateVmStatistics(vmToUpdate);
                    if (_vmDict.containsKey(runningVm.getId())) {
                        running.add(_vmDict.get(runningVm.getId()));
                        if (!_vdsManager.getInitialized()) {
                            ResourceManager.getInstance().RemoveVmFromDownVms(_vds.getId(), runningVm.getId());
                        }
                    }
                }
            } else {
                if (runningVm.getstatus() == VMStatus.MigratingTo && vmToUpdate != null) {
                    running.add(vmToUpdate);
                }

                VmDynamic vmDynamic = DbFacade.getInstance().getVmDynamicDAO().get(runningVm.getId());
                if (vmDynamic == null || vmDynamic.getstatus() != VMStatus.Unknown) {
                    _vmDynamicToSave.remove(runningVm.getId());
                }
            }
        }
        // compare between vm in cache and vm from vdsm
        removeVmsFromCache(running);
    }

    // del from cache all vms that not in vdsm
    private void removeVmsFromCache(java.util.List<VM> running) {
        // LINQ 29456 - fixed
        // foreach (VM vmToRemove in _vmDict.Values.Except(running).ToList())
        Guid vmGuid;
        for (VM vmToRemove : _vmDict.values()) {
            if (running.contains(vmToRemove)) // LINQ 29456 fix
            {
                continue; // LINQ 29456 fix
            }
            proceedVmBeforeDeletion(vmToRemove, null);
            boolean isInMigration = false;
            if (vmToRemove.getstatus() == VMStatus.MigratingFrom) {
                isInMigration = true;
                vmToRemove.setrun_on_vds(vmToRemove.getmigrating_to_vds());
                ResourceManager.getInstance().InternalSetVmStatus(vmToRemove, VMStatus.Unknown);
                AddVmDynamicToList(vmToRemove.getDynamicData());
                AddVmStatisticsToList(vmToRemove.getStatisticsData());
                AddVmInterfaceStatisticsToList(vmToRemove.getInterfaces());
            } else {
                clearVm(vmToRemove);
            }
            log.infoFormat("vm {0} running in db and not running in vds - add to rerun treatment. vds {1}",
                    vmToRemove.getvm_name(), _vds.getvds_name());

            vmGuid = vmToRemove.getId();
            if (!isInMigration && !_vmsToRerun.contains(vmGuid)
                    && ResourceManager.getInstance().IsVmInAsyncRunningList(vmGuid)) {
                _vmsToRerun.add(vmGuid);
            }
            // vm should be auto startup
            // not already in start up list
            // not in reported from vdsm at all
            // or reported from vdsm with error code
            else if (vmToRemove.getauto_startup()
                    && !_autoVmsToRun.contains(vmGuid)
                    && (!_runningVms.containsKey(vmGuid) || (_runningVms.containsKey(vmGuid) && _runningVms.get(vmGuid)
                            .getKey()
                            .getExitStatus() != VmExitStatus.Normal))) {
                _autoVmsToRun.add(vmGuid);
            }
        }
    }

    private boolean inMigrationTo(VmDynamic runningVm, VM vmToUpdate) {
        boolean returnValue = false;
        if (runningVm.getstatus() == VMStatus.MigratingTo) {
            /**
             * inMigration
             */
            log.infoFormat(
                    "vds::refreshVmList vm id '{0}' is migrating to vds '{1}' ignoring it in the refresh till migration is done",
                    runningVm.getId(),
                    _vds.getvds_name());
            returnValue = true;
        } else if ((vmToUpdate == null && runningVm.getstatus() != VMStatus.MigratingFrom)) {
            // check if the vm exists on another vds
            VmDynamic vmDynamic = DbFacade.getInstance().getVmDynamicDAO().get(runningVm.getId());
            if (vmDynamic != null && vmDynamic.getrun_on_vds() != null
                    && !vmDynamic.getrun_on_vds().equals(_vds.getId()) && runningVm.getstatus() != VMStatus.Up) {
                log.infoFormat(
                        "vds::refreshVmList vm id '{0}' status = {1} on vds {2} ignoring it in the refresh till migration is done",
                        runningVm.getId(),
                        runningVm.getstatus(),
                        _vds.getvds_name());
                returnValue = true;
            }
        }
        return returnValue;
    }

    private void afterMigrationFrom(VmDynamic runningVm, VM vmToUpdate) {
        VMStatus oldVmStatus = vmToUpdate.getstatus();

        if (oldVmStatus == VMStatus.MigratingFrom && VM.isGuestUp(runningVm.getstatus())) {
            _vmsToRerun.add(runningVm.getId());
            vmToUpdate.setmigrating_to_vds(null);
        }
    }

    private void refreshCommitedMemory() {
        Integer memCommited = _vds.getguest_overhead() != null ? 0 : null;
        int vmsCoresCount = 0;
        for (VM vm : _vmDict.values()) {
            if (_vds.getguest_overhead() != null) {
                memCommited += vm.getvm_mem_size_mb();
                memCommited += _vds.getguest_overhead();
            }
            vmsCoresCount += vm.getnum_of_cpus();
        }
        if (memCommited == null || !memCommited.equals(_vds.getmem_commited())) {
            _vds.setmem_commited(memCommited);
            _saveVdsDynamic = true;
        }
        if (_vds.getvms_cores_count() == null || !_vds.getvms_cores_count().equals(vmsCoresCount)) {
            _vds.setvms_cores_count(vmsCoresCount);
            _saveVdsDynamic = true;
        }

        if (_vds.getpending_vcpus_count() != 0 && runningVmsInTransition == 0) {
            _vds.setpending_vcpus_count(0);
            _saveVdsDynamic = true;
        }

        if (_vds.getpending_vmem_size() != 0 && runningVmsInTransition == 0) {
            // set also vmem size to 0
            _vds.setpending_vmem_size(0);
            _saveVdsDynamic = true;
        }
    }

    private void MoveVDSToMaintenanceIfNeeded() {
        if ((_vds.getstatus() == VDSStatus.PreparingForMaintenance)
                && monitoringStrategy.canMoveToMaintenance(_vds)) {
            try {
                _vdsManager.setStatus(VDSStatus.Maintenance, _vds);
                _saveVdsDynamic = true;
                _saveVdsStatistics = true;
                log.infoFormat(
                        "vds::Updated vds status from 'Preparing for Maintenance' to 'Maintenance' in database,  vds = {0} : {1}",
                        _vds.getId(),
                        _vds.getvds_name());
            } catch (RepositoryException ex) {
                log.errorFormat(
                        "vds::Failed to update vds status from 'Preparing for Maintenance' to 'Maintenance' in database,  vds = {0} : {1}, error = {2}",
                        _vds.getId(),
                        _vds.getvds_name(),
                        ExceptionUtils.getMessage(ex));
                log.error("Exception: ", ex);
            }
        }
    }

    private boolean UpdateVmRunTimeInfo(RefObject<VM> vmToUpdate, VmDynamic vmNewDynamicData) {
        boolean returnValue = false;
        if (vmToUpdate.argvalue == null) {
            vmToUpdate.argvalue = DbFacade.getInstance().getVmDAO().get(vmNewDynamicData.getId());
            // if vm exists in db update info
            if (vmToUpdate.argvalue != null) {
                // TODO: This is done to keep consistency with VmDAO.getById(Guid).
                // It should probably be removed, but some research is required.
                vmToUpdate.argvalue.setInterfaces(DbFacade.getInstance()
                        .getVmNetworkInterfaceDAO()
                        .getAllForVm(vmToUpdate.argvalue.getId()));

                _vmDict.put(vmToUpdate.argvalue.getId(), vmToUpdate.argvalue);
                if (vmNewDynamicData.getstatus() == VMStatus.Up) {
                    if (!_succededToRunVms.contains(vmToUpdate.argvalue.getId())) {
                        _succededToRunVms.add(vmToUpdate.argvalue.getId());
                    }
                }
            }
        }
        if (vmToUpdate.argvalue != null) {
            // check if dynamic data changed - update cache and DB
            java.util.ArrayList<String> props = ObjectIdentityChecker.GetChangedFields(
                    vmToUpdate.argvalue.getDynamicData(), vmNewDynamicData);
            // dont check fields:
            props.remove("vm_host");
            props.remove("guest_cur_user_name");
            props.remove("run_on_vds");
            props.remove("disks");
            props.remove("boot_sequence");
            props.remove("last_vds_run_on");
            props.remove("hibernation_vol_handle");
            props.remove("exitMessage");
            if (vmNewDynamicData.getstatus() != VMStatus.Up) {
                props.remove("app_list");
                vmNewDynamicData.setapp_list(vmToUpdate.argvalue.getapp_list());
            } else if (props.contains("status")
                    && vmToUpdate.argvalue.getDynamicData().getstatus() == VMStatus.SavingState) {
                vmNewDynamicData.setstatus(VMStatus.SavingState);
                props.remove("status");
            }
            // if anything else changed
            if (props.size() > 0) {
                vmToUpdate.argvalue.updateRunTimeDynamicData(vmNewDynamicData, _vds.getId(), _vds.getvds_name());
                returnValue = true;
            }
        } else {
            // This should only happened when someone run a VM from command
            // line.
            if (Config.<Boolean> GetValue(ConfigValues.DebugTimerLogging)) {
                log.info("VDS::UpdateVmRunTimeInfo Error: found VM on a VDS that is not in the database!");
            }
        }

        return returnValue;
    }

    private void UpdateVmStatistics(VM vmToUpdate) {
        // check if time for vm statistics refresh - update cache and DB
        if (_vdsManager.getRefreshStatistics()) {
            VmStatistics vmStatistics = _runningVms.get(vmToUpdate.getId()).getValue();
            vmToUpdate.updateRunTimeStatisticsData(vmStatistics, vmToUpdate);
            AddVmStatisticsToList(vmToUpdate.getStatisticsData());
            UpdateInterfaceStatistics(vmToUpdate, vmStatistics);

            for (DiskImageDynamic imageDynamic : _runningVms.get(vmToUpdate.getId()).getKey().getDisks()) {
                _vmDiskImageDynamicToSave.put(imageDynamic.getId(), imageDynamic);
            }
        }
    }

    private void UpdateInterfaceStatistics(VM vm, VmStatistics statistics) {
        if (statistics.getInterfaceStatistics() == null) {
            return;
        }

        if (vm.getInterfaces() == null || vm.getInterfaces().isEmpty()) {
            vm.setInterfaces(DbFacade.getInstance().getVmNetworkInterfaceDAO().getAllForVm(vm.getId()));
        }
        java.util.ArrayList<String> macs = new java.util.ArrayList<String>();

        vm.setusage_network_percent(0);

        for (VmNetworkInterface ifStats : statistics.getInterfaceStatistics()) {
            boolean firstTime = !macs.contains(ifStats.getMacAddress());
            // LINQ 29456
            // Interface vmIface = vm.Interfaces.FirstOrDefault(i => i.mac_addr
            // == ifStats.mac_addr);

            VmNetworkInterface vmIface = null;
            for (VmNetworkInterface tempIf : vm.getInterfaces()) {
                if (tempIf.getMacAddress().equals(ifStats.getMacAddress())) {
                    vmIface = tempIf;
                    break;
                }
            }
            // LINQ 29456
            if (vmIface == null) {
                continue;
            }
            if (vmIface != null) {

                // RX rate and TX rate are reported by VDSM in % (minimum value
                // 0, maximum value 100)
                // Rx drop and TX drop are reported in packet numbers

                // if rtl+pv it will get here 2 times (we take the max one)
                if (firstTime) {

                    vmIface.getStatistics().setReceiveRate(ifStats.getStatistics().getReceiveRate());
                    vmIface.getStatistics().setReceiveDropRate(ifStats.getStatistics().getReceiveDropRate());
                    vmIface.getStatistics().setTransmitRate(ifStats.getStatistics().getTransmitRate());
                    vmIface.getStatistics().setTransmitDropRate(ifStats.getStatistics().getTransmitDropRate());
                } else {
                    vmIface.getStatistics().setReceiveRate(Math.max(vmIface.getStatistics().getReceiveRate(),
                            ifStats.getStatistics().getReceiveRate()));
                    vmIface.getStatistics().setReceiveDropRate(Math.max(vmIface.getStatistics().getReceiveDropRate(),
                            ifStats.getStatistics().getReceiveDropRate()));
                    vmIface.getStatistics().setTransmitRate(Math.max(vmIface.getStatistics().getTransmitRate(),
                            ifStats.getStatistics().getTransmitRate()));
                    vmIface.getStatistics().setTransmitDropRate(Math.max(vmIface.getStatistics().getTransmitDropRate(),
                            ifStats.getStatistics().getTransmitDropRate()));
                }
                vmIface.setVmId(vm.getId());
            }

            if (ifStats.getSpeed() != null && vmIface.getStatistics().getReceiveRate() != null
                    && vmIface.getStatistics().getReceiveRate() > 0) {

                double rx_percent = vmIface.getStatistics().getReceiveRate();
                double tx_percent = vmIface.getStatistics().getTransmitRate();

                vm.setusage_network_percent(Math.max(vm.getusage_network_percent(),
                        (int) Math.max(rx_percent, tx_percent)));
            }

            if (firstTime) {
                macs.add(ifStats.getMacAddress());
            }
        }

        vm.setusage_network_percent((vm.getusage_network_percent() > 100) ? 100 : vm.getusage_network_percent());
        AddVmInterfaceStatisticsToList(vm.getInterfaces());
    }

    /**
     * Add or update vmDynamic to save list
     *
     * @param vmDynamic
     */
    private void AddVmDynamicToList(VmDynamic vmDynamic) {
        _vmDynamicToSave.put(vmDynamic.getId(), vmDynamic);
    }

    /**
     * Add or update vmStatistics to save list
     *
     * @param vmDynamic
     */
    private void AddVmStatisticsToList(VmStatistics vmStatistics) {
        _vmStatisticsToSave.put(vmStatistics.getId(), vmStatistics);
    }

    private void AddVmInterfaceStatisticsToList(List<VmNetworkInterface> list) {
        if (list.size() <= 0) {
            return;
        }
        _vmInterfaceStatisticsToSave.put(list.get(0).getVmId().getValue(), list);
    }

    /**
     * Add or update vmDynamic to save list
     *
     * @param vmDynamic
     */
    private void addVmDeviceToList(VmDevice vmDevice) {
        vmDeviceToSave.put(vmDevice.getId(), vmDevice);
    }

    private void clearVm(VM vm) {
        if (vm.getstatus() != VMStatus.MigratingFrom) {
            if (vm.getstatus() != VMStatus.Suspended) {
                ResourceManager.getInstance().InternalSetVmStatus(vm, VMStatus.Down);
            }
            AddVmDynamicToList(vm.getDynamicData());
            AddVmStatisticsToList(vm.getStatisticsData());
            AddVmInterfaceStatisticsToList(vm.getInterfaces());
            if (!ResourceManager.getInstance().IsVmInAsyncRunningList(vm.getId())) {
                _vmsMovedToDown.add(vm.getId());
            }
        }
    }

    private static Log log = LogFactory.getLog(VdsUpdateRunTimeInfo.class);
}

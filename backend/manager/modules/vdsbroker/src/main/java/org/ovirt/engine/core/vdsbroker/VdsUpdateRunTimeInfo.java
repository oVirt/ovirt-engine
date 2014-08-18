package org.ovirt.engine.core.vdsbroker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.IVdsEventListener;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.businessentities.VmBalloonInfo;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.FullListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetVmStatsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSErrorException;
import org.ovirt.engine.core.vdsbroker.vdsbroker.DestroyVDSCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.FullListVdsCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.GetStatsVDSCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSErrorException;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSNetworkException;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSProtocolException;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSRecoveringException;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;

@SuppressWarnings({ "synthetic-access", "unchecked", "rawtypes" })
public class VdsUpdateRunTimeInfo {
    private Map<Guid, VmInternalData> _runningVms;
    private final Map<Guid, VmDynamic> _vmDynamicToSave = new HashMap<>();
    private final Map<Guid, VmStatistics> _vmStatisticsToSave = new HashMap<>();
    private final Map<Guid, List<VmNetworkInterface>> _vmInterfaceStatisticsToSave = new HashMap<>();
    private final Set<DiskImageDynamic> _vmDiskImageDynamicToSave = new HashSet<>();
    private final List<LUNs> vmLunDisksToSave = new ArrayList<>();
    private final Map<VmDeviceId, VmDevice> vmDeviceToSave = new HashMap<>();
    private final List<VmDevice> newVmDevices = new ArrayList<>();
    private final List<VmDeviceId> removedDeviceIds = new ArrayList<>();
    private final Map<VM, VmDynamic> _vmsClientIpChanged = new HashMap<>();
    private final Map<Guid, List<VmGuestAgentInterface>> vmGuestAgentNics = new HashMap<>();
    private final List<VmDynamic> _poweringUpVms = new ArrayList<>();
    private final List<Guid> _vmsToRerun = new ArrayList<>();
    private final List<Guid> _autoVmsToRun = new ArrayList<>();
    private final Set<Guid> _vmsMovedToDown = new HashSet<>();
    private final List<Guid> _vmsToRemoveFromAsync = new ArrayList<>();
    private final List<Guid> _succededToRunVms = new ArrayList<>();
    private static final Map<Guid, Integer> vmsWithBalloonDriverProblem = new HashMap<>();
    private static final Map<Guid, Integer> vmsWithUncontrolledBalloon = new HashMap<>();
    private final List<VmStatic> _externalVmsToAdd = new ArrayList<>();
    private boolean _saveVdsDynamic;
    private VDSStatus _firstStatus = VDSStatus.forValue(0);
    private boolean _saveVdsStatistics;
    private final VdsManager _vdsManager;
    private final MonitoringStrategy monitoringStrategy;
    private final VDS _vds;
    private final Map<Guid, VM> _vmDict;
    private boolean processHardwareCapsNeeded;
    private boolean refreshedCapabilities = false;
    private static Map<Guid, Long> hostDownTimes = new HashMap<>();
    private boolean vdsMaintenanceTimeoutOccurred;

    private static final Log log = LogFactory.getLog(VdsUpdateRunTimeInfo.class);

    private static final int TO_MEGA_BYTES = 1024;
    private static final String HOSTED_ENGINE_VM_NAME = "HostedEngine";
    private static final String EXTERNAL_VM_NAME_FORMAT = "external-%1$s";

    private void saveDataToDb() {
        if (_saveVdsDynamic) {
            _vdsManager.updateDynamicData(_vds.getDynamicData());
        }

        if (_saveVdsStatistics) {
            VdsStatistics stat = _vds.getStatisticsData();
            _vdsManager.updateStatisticsData(stat);
            checkVdsMemoryThreshold(stat);
            checkVdsCpuThreshold(stat);
            checkVdsNetworkThreshold(stat);
            checkVdsSwapThreshold(stat);

            final List<VdsNetworkStatistics> statistics = new LinkedList<VdsNetworkStatistics>();
            for (VdsNetworkInterface iface : _vds.getInterfaces()) {
                statistics.add(iface.getStatistics());
            }
            if (!statistics.isEmpty()) {
                TransactionSupport.executeInScope(TransactionScopeOption.Required,
                        new TransactionMethod<Void>() {

                            @Override
                            public Void runInTransaction() {
                                getDbFacade().getInterfaceDao().massUpdateStatisticsForVds(statistics);
                                return null;
                            }
                        });
            }
        }

        getDbFacade().getVmDynamicDao().updateAllInBatch(_vmDynamicToSave.values());
        getDbFacade().getVmStatisticsDao().updateAllInBatch(_vmStatisticsToSave.values());

        final List<VmNetworkStatistics> allVmInterfaceStatistics = new LinkedList<VmNetworkStatistics>();
        for (List<VmNetworkInterface> list : _vmInterfaceStatisticsToSave.values()) {
            for (VmNetworkInterface iface : list) {
                allVmInterfaceStatistics.add(iface.getStatistics());
            }
        }

        getDbFacade().getVmNetworkStatisticsDao().updateAllInBatch(allVmInterfaceStatistics);
        getDbFacade().getDiskImageDynamicDao().updateAllDiskImageDynamicWithDiskId(_vmDiskImageDynamicToSave);
        getDbFacade().getLunDao().updateAllInBatch(vmLunDisksToSave);
        saveVmDevicesToDb();
        saveVmGuestAgentNetworkDevices();
        getVdsEventListener().addExternallyManagedVms(_externalVmsToAdd);
    }

    private void saveVmGuestAgentNetworkDevices() {
        if (!vmGuestAgentNics.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required,
                    new TransactionMethod<Void>() {
                        @Override
                        public Void runInTransaction() {
                            for (Guid vmId : vmGuestAgentNics.keySet()) {
                                getDbFacade().getVmGuestAgentInterfaceDao().removeAllForVm(vmId);
                            }

                            for (List<VmGuestAgentInterface> nics : vmGuestAgentNics.values()) {
                                if (nics != null) {
                                    for (VmGuestAgentInterface nic : nics) {
                                        getDbFacade().getVmGuestAgentInterfaceDao().save(nic);
                                    }
                                }
                            }
                            return null;
                        }
                    });
        }
    }

    private void saveVmDevicesToDb() {
        getDbFacade().getVmDeviceDao().updateAllInBatch(vmDeviceToSave.values());

        if (!removedDeviceIds.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required,
                    new TransactionMethod<Void>() {
                        @Override
                        public Void runInTransaction() {
                            getDbFacade().getVmDeviceDao().removeAll(removedDeviceIds);
                            return null;
                        }
                    });
        }

        if (!newVmDevices.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required,
                    new TransactionMethod<Void>() {

                        @Override
                        public Void runInTransaction() {
                            getDbFacade().getVmDeviceDao().saveAll(newVmDevices);
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
    private void checkVdsMemoryThreshold(VdsStatistics stat) {

        Integer minAvailableThreshold = Config.getValue(ConfigValues.LogPhysicalMemoryThresholdInMB);
        Integer maxUsedPercentageThreshold =
                Config.getValue(ConfigValues.LogMaxPhysicalMemoryUsedThresholdInPercentage);

        if (stat.getMemFree() == null || stat.getusage_mem_percent() == null) {
            return;
        }

        AuditLogType valueToLog = stat.getMemFree() < minAvailableThreshold ?
                AuditLogType.VDS_LOW_MEM :
                AuditLogType.VDS_HIGH_MEM_USE;

        if ((stat.getMemFree() < minAvailableThreshold && Version.v3_2.compareTo(_vds.getVersion()) <= 0)
                || stat.getusage_mem_percent() > maxUsedPercentageThreshold) {
            AuditLogableBase logable = new AuditLogableBase(stat.getId());
            logable.addCustomValue("HostName", _vds.getName());
            logable.addCustomValue("AvailableMemory", stat.getMemFree().toString());
            logable.addCustomValue("UsedMemory", stat.getusage_mem_percent().toString());
            logable.addCustomValue("Threshold", stat.getMemFree() < minAvailableThreshold ?
                    minAvailableThreshold.toString() :
                    maxUsedPercentageThreshold.toString());
            auditLog(logable, valueToLog);
        }
    }

    /**
     * check if value is less than configurable threshold , if yes , generated event log message
     *
     * @param stat
     */
    private void checkVdsCpuThreshold(VdsStatistics stat) {

        Integer maxUsedPercentageThreshold = Config.getValue(ConfigValues.LogMaxCpuUsedThresholdInPercentage);
        if (stat.getusage_cpu_percent() != null
                && stat.getusage_cpu_percent() > maxUsedPercentageThreshold) {
            AuditLogableBase logable = new AuditLogableBase(stat.getId());
            logable.addCustomValue("HostName", _vds.getName());
            logable.addCustomValue("UsedCpu", stat.getusage_cpu_percent().toString());
            logable.addCustomValue("Threshold", maxUsedPercentageThreshold.toString());
            auditLog(logable, AuditLogType.VDS_HIGH_CPU_USE);
        }
    }

    /**
     * check if value is less than configurable threshold , if yes , generated event log message
     *
     * @param stat
     */
    private void checkVdsNetworkThreshold(VdsStatistics stat) {
        Integer maxUsedPercentageThreshold = Config.getValue(ConfigValues.LogMaxNetworkUsedThresholdInPercentage);
        for (VdsNetworkInterface iface : _vds.getInterfaces()) {
            Double transmitRate = iface.getStatistics().getTransmitRate();
            Double receiveRate = iface.getStatistics().getReceiveRate();
            if ((transmitRate != null && iface.getStatistics().getTransmitRate().intValue() > maxUsedPercentageThreshold)
                    || (receiveRate != null && iface.getStatistics().getReceiveRate().intValue() > maxUsedPercentageThreshold)) {
                AuditLogableBase logable = new AuditLogableBase(_vds.getId());
                logable.setCustomId(iface.getName());
                logable.addCustomValue("HostName", _vds.getName());
                logable.addCustomValue("InterfaceName", iface.getName());
                logable.addCustomValue("Threshold", maxUsedPercentageThreshold.toString());
                logable.addCustomValue("TransmitRate", String.valueOf(transmitRate.intValue()));
                logable.addCustomValue("ReceiveRate", String.valueOf(receiveRate.intValue()));
                auditLog(logable, AuditLogType.HOST_INTERFACE_HIGH_NETWORK_USE);
            }
        }
    }

    /**
     * check if value is less than configurable threshold , if yes , generated event log message
     *
     * @param stat
     */
    private void checkVdsSwapThreshold(VdsStatistics stat) {

        Integer minAvailableThreshold = Config.getValue(ConfigValues.LogPhysicalMemoryThresholdInMB);
        Integer maxUsedPercentageThreshold =
                Config.getValue(ConfigValues.LogMaxPhysicalMemoryUsedThresholdInPercentage);

        if (stat.getswap_total() == null || stat.getswap_free() == null || stat.getswap_total() == 0) {
            return;
        }

        Long swapUsedPercent = (stat.getswap_total() - stat.getswap_free()) / stat.getswap_total();

        AuditLogType valueToLog = stat.getswap_free() < minAvailableThreshold ?
                AuditLogType.VDS_LOW_SWAP :
                AuditLogType.VDS_HIGH_SWAP_USE;

        if (stat.getswap_free() < minAvailableThreshold || swapUsedPercent > maxUsedPercentageThreshold) {
            AuditLogableBase logable = new AuditLogableBase(stat.getId());
            logable.addCustomValue("HostName", _vds.getName());
            logable.addCustomValue("UsedSwap", swapUsedPercent.toString());
            logable.addCustomValue("AvailableSwapMemory", stat.getswap_free().toString());
            logable.addCustomValue("Threshold", stat.getswap_free() < minAvailableThreshold ?
                    minAvailableThreshold.toString() : maxUsedPercentageThreshold.toString());
            auditLog(logable, valueToLog);
        }
    }

    public VdsUpdateRunTimeInfo(VdsManager vdsManager, VDS vds, MonitoringStrategy monitoringStrategy) {
        _vdsManager = vdsManager;
        _vds = vds;
        _firstStatus = _vds.getStatus();
        this.monitoringStrategy = monitoringStrategy;
        _vmDict = getDbFacade().getVmDao().getAllRunningByVds(_vds.getId());
    }

    public void refresh() {
        try {
            refreshVdsRunTimeInfo();
        } finally {
            try {
                if (_firstStatus != _vds.getStatus() && _vds.getStatus() == VDSStatus.Up) {
                    // use this lock in order to allow only one host updating DB and
                    // calling UpEvent in a time
                    VdsManager.cancelRecoveryJob(_vds.getId());
                    if (log.isDebugEnabled()) {
                        log.debugFormat("vds {0}-{1} firing up event.", _vds.getId(), _vds.getName());
                    }
                    _vdsManager.setIsSetNonOperationalExecuted(!ResourceManager.getInstance()
                            .getEventListener()
                            .vdsUpEvent(_vds));
                }
                // save all data to db
                saveDataToDb();
            } catch (IRSErrorException ex) {
                logFailureMessage("ResourceManager::refreshVdsRunTimeInfo:", ex);
                if (log.isDebugEnabled()) {
                    log.error(ExceptionUtils.getMessage(ex), ex);
                }
            } catch (RuntimeException ex) {
                logFailureMessage("ResourceManager::refreshVdsRunTimeInfo:", ex);
                log.error(ExceptionUtils.getMessage(ex), ex);
            }
        }
    }

    private void logFailureMessage(String messagePrefix, RuntimeException ex) {
        log.errorFormat("{0} Error: {1}, vds = {2} : {3}",
                messagePrefix,
                ExceptionUtils.getMessage(ex),
                _vds.getId(),
                _vds.getName());
    }

    protected IVdsEventListener getVdsEventListener() {
        return ResourceManager.getInstance().getEventListener();
    }

    public void afterRefreshTreatment() {
        try {
            if (processHardwareCapsNeeded) {
                monitoringStrategy.processHardwareCapabilities(_vds);
                markIsSetNonOperationalExecuted();
            }

            if (refreshedCapabilities) {
                getVdsEventListener().handleVdsVersion(_vds.getId());
                markIsSetNonOperationalExecuted();
            }

            if (vdsMaintenanceTimeoutOccurred) {
                handleVdsMaintenanceTimeout();
            }

            if (_vds.getStatus() == VDSStatus.Maintenance) {
                try {
                    getVdsEventListener().vdsMovedToMaintenance(_vds);
                } catch (RuntimeException ex) {
                    log.errorFormat("Host encounter a problem moving to maintenance mode, probably error during disconnecting it from pool {0}. The Host will stay in Maintenance",
                            ex.getMessage());
                }
            } else if (_vds.getStatus() == VDSStatus.NonOperational && _firstStatus != VDSStatus.NonOperational) {

                if (!_vdsManager.isSetNonOperationalExecuted()) {
                    ResourceManager
                            .getInstance()
                            .getEventListener()
                            .vdsNonOperational(_vds.getId(), _vds.getNonOperationalReason(), true, true,
                                    Guid.Empty);
                } else {

                    log.infoFormat("Host {0} : {1} is already in NonOperational status for reason {2}. SetNonOperationalVds command is skipped.",
                            _vds.getId(),
                            _vds.getName(),
                            (_vds.getNonOperationalReason() != null) ? _vds.getNonOperationalReason().name()
                                    : "unknown");
                }
            }
            // rerun all vms from rerun list
            for (Guid vm_guid : _vmsToRerun) {
                log.errorFormat("Rerun vm {0}. Called from vds {1}", vm_guid, _vds.getName());
                ResourceManager.getInstance().RerunFailedCommand(vm_guid, _vds.getId());

            }
            for (Guid vm_guid : _succededToRunVms) {
                _vdsManager.succededToRunVm(vm_guid);
            }

            // Refrain from auto-start HA VM during its re-run attempts.
            _autoVmsToRun.removeAll(_vmsToRerun);
            // run all vms that crushed that marked with auto startup
            getVdsEventListener().runFailedAutoStartVMs(_autoVmsToRun);

            // process all vms that their ip changed.
            for (Entry<VM, VmDynamic> pair : _vmsClientIpChanged.entrySet()) {
                getVdsEventListener().processOnClientIpChange(_vds, pair.getValue().getId());
            }

            // process all vms that powering up.
            for (VmDynamic runningVm : _poweringUpVms) {
                ResourceManager
                        .getInstance()
                        .getEventListener()
                        .processOnVmPoweringUp(_vds.getId(), runningVm.getId(), runningVm.getDisplayIp(),
                                runningVm.getDisplay());
            }

            // process all vms that went down
            getVdsEventListener().processOnVmStop(_vmsMovedToDown);

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

    private void handleVdsMaintenanceTimeout() {
        getVdsEventListener().handleVdsMaintenanceTimeout(_vds);
        _vdsManager.calculateNextMaintenanceAttemptTime();
    }

    private void markIsSetNonOperationalExecuted() {
        if (!_vdsManager.isSetNonOperationalExecuted()) {
            VdsDynamic vdsDynamic = getDbFacade().getVdsDynamicDao().get(_vds.getId());
            if (vdsDynamic.getStatus() == VDSStatus.NonOperational) {
                _vdsManager.setIsSetNonOperationalExecuted(true);
            }
        }
    }

    private void refreshVdsRunTimeInfo() {
        boolean isVdsUpOrGoingToMaintenance = _vds.getStatus() == VDSStatus.Up
                || _vds.getStatus() == VDSStatus.PreparingForMaintenance || _vds.getStatus() == VDSStatus.Error
                || _vds.getStatus() == VDSStatus.NonOperational;
        try {
            if (isVdsUpOrGoingToMaintenance) {
                // check if its time for statistics refresh
                if (_vdsManager.getRefreshStatistics() || _vds.getStatus() == VDSStatus.PreparingForMaintenance) {
                    refreshVdsStats();
                }
            } else {
                // refresh dynamic data
                final AtomicBoolean processHardwareNeededAtomic = new AtomicBoolean();
                VDSStatus refreshReturnStatus =
                        _vdsManager.refreshCapabilities(processHardwareNeededAtomic, _vds);
                processHardwareCapsNeeded = processHardwareNeededAtomic.get();
                refreshedCapabilities = true;
                if (refreshReturnStatus != VDSStatus.NonOperational) {
                    _vdsManager.setStatus(VDSStatus.Up, _vds);
                }
                _saveVdsDynamic = true;
            }
            beforeFirstRefreshTreatment(isVdsUpOrGoingToMaintenance);
            refreshVmStats();
        } catch (VDSRecoveringException e) {
            // if PreparingForMaintenance and vds is in install failed keep to
            // move vds to maintenance
            if (_vds.getStatus() != VDSStatus.PreparingForMaintenance) {
                throw e;
            }
        }
        moveVDSToMaintenanceIfNeeded();
    }

    private void refreshVdsStats() {
        if (Config.<Boolean> getValue(ConfigValues.DebugTimerLogging)) {
            log.debugFormat("vdsManager::refreshVdsStats entered, vds = {0} : {1}", _vds.getId(),
                    _vds.getName());
        }
        // get statistics data, images checks and vm_count data (dynamic)
        GetStatsVDSCommand<VdsIdAndVdsVDSCommandParametersBase> vdsBrokerCommand =
                new GetStatsVDSCommand<VdsIdAndVdsVDSCommandParametersBase>(new VdsIdAndVdsVDSCommandParametersBase(_vds));
        vdsBrokerCommand.execute();
        ResourceManager.getInstance().getEventListener().updateSchedulingStats(_vds);
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
                        _vds.getId(), _vds.getName(), ExceptionUtils.getMessage(ex));
            } else {
                log.errorFormat("vds::refreshVdsStats Failed getVdsStats,  vds = {0} : {1}, error = {2}",
                        _vds.getId(), _vds.getName(), vdsBrokerCommand.getVDSReturnValue().getExceptionString());
            }
            throw vdsBrokerCommand.getVDSReturnValue().getExceptionObject();
        }
        // save also dynamic because vm_count data and image_check getting with
        // statistics data
        // TODO: omer- one day remove dynamic save when possible please check if vdsDynamic changed before save
        _saveVdsDynamic = true;
        _saveVdsStatistics = true;

        alertIfLowDiskSpaceOnHost();
        checkVdsInterfaces();

        if (Config.<Boolean> getValue(ConfigValues.DebugTimerLogging)) {
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
        final int lowSpaceCriticalThreshold =
                Config.<Integer> getValue(ConfigValues.VdsLocalDisksCriticallyLowFreeSpace);
        final int lowSpaceThreshold =
                Config.<Integer> getValue(ConfigValues.VdsLocalDisksLowFreeSpace);

        for (Entry<String, Long> diskUsage : disksUsage.entrySet()) {
            if (diskUsage.getValue() != null) {
                if (diskUsage.getValue() <= lowSpaceCriticalThreshold) {
                    disksWithCriticallyLowSpace.add(diskUsage.getKey());
                } else if (diskUsage.getValue() <= lowSpaceThreshold) {
                    disksWithLowSpace.add(diskUsage.getKey());
                }
            }
        }

        logLowDiskSpaceOnHostDisks(disksWithLowSpace, lowSpaceThreshold, AuditLogType.VDS_LOW_DISK_SPACE);
        logLowDiskSpaceOnHostDisks(disksWithCriticallyLowSpace,
                lowSpaceCriticalThreshold,
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
            logable.addCustomValue("DiskSpace", lowSpaceThreshold.toString());
            logable.addCustomValue("Disks", StringUtils.join(disksWithLowSpace, ", "));
            auditLog(logable, logType);
        }
    }

    // Check if one of the Host interfaces is down, we set the host to non-operational
    // We cannot have Host that don't have all networks in cluster in status Up
    private void checkVdsInterfaces() {
        if (_vds.getStatus() != VDSStatus.Up) {
            return;
        }

        List<String> networks = new ArrayList<String>();
        List<String> brokenNics = new ArrayList<String>();

        try {
            Pair<List<String>, List<String>> problematicNics = determineProblematicNics(_vds.getInterfaces(),
                    getDbFacade().getNetworkDao().getAllForCluster(_vds.getVdsGroupId()));
            brokenNics.addAll(problematicNics.getFirst());
            networks.addAll(problematicNics.getSecond());
        } catch (Exception e) {
            log.error(String.format("Failure on checkInterfaces on update runtimeinfo for vds: %s", _vds.getName()),
                    e);
        } finally {
            if (!brokenNics.isEmpty()) {
                // we give 1 minutes to a nic to get up in case the nic get the ip from DHCP server
                if (!hostDownTimes.containsKey(_vds.getId())) {
                    hostDownTimes.put(_vds.getId(), System.currentTimeMillis());
                    return;
                }

                // if less then 1 minutes, still waiting for DHCP
                int delay = Config.<Integer> getValue(ConfigValues.NicDHCPDelayGraceInMS) * 1000;
                if (System.currentTimeMillis() < hostDownTimes.get(_vds.getId()) + delay) {
                    return;
                }

                // if we could retrieve it within the timeout, remove from map (for future checks) and set the host to
                // non-operational
                hostDownTimes.remove(_vds.getId());

                try {
                    String networkNames = StringUtils.join(networks, ", ");
                    String nicNames = StringUtils.join(brokenNics, ", ");

                    String message =
                            String.format(
                                    "Host '%s' moved to Non-Operational state because interface/s '%s' are down which needed by network/s '%s' in the current cluster",
                                    _vds.getName(),
                                    nicNames,
                                    networkNames);

                    _vds.setNonOperationalReason(NonOperationalReason.NETWORK_INTERFACE_IS_DOWN);
                    _vdsManager.setStatus(VDSStatus.NonOperational, _vds);
                    log.info(message);

                    AuditLogableBase logable = new AuditLogableBase(_vds.getId());
                    logable.addCustomValue("Networks", networkNames);
                    logable.addCustomValue("Interfaces", nicNames);
                    logable.setCustomId(nicNames + networkNames);
                    auditLog(logable, AuditLogType.VDS_SET_NONOPERATIONAL_IFACE_DOWN);
                } catch (Exception e) {
                    log.error(String.format("checkInterface: Failure on moving host: %s to non-operational.",
                            _vds.getName()),
                            e);
                }
            } else {
                // no nics are down, remove from list if exists
                hostDownTimes.remove(_vds.getId());
            }
        }
    }

    /**
     * Determine which of the given NICs is problematic - a problematic NIC is considered to be a NIC which it's state
     * is down and it is the underlying interface of a required network.
     *
     * @param interfaces
     *            The NICs to check.
     * @param clusterNetworks
     *            The cluster's networks.
     * @return A pair of a list of the names of the NICs which are problematic, and a list of the names of the networks
     *         which are required by these NICs.
     */
    public static Pair<List<String>, List<String>> determineProblematicNics(List<VdsNetworkInterface> interfaces,
            List<Network> clusterNetworks) {
        Map<String, Boolean> bondsWithStatus = new HashMap<String, Boolean>();
        List<String> networks = new ArrayList<String>();
        List<String> brokenNics = new ArrayList<String>();
        Map<String, List<String>> bondsWithListOfNics = new HashMap<String, List<String>>();

        Map<String, Network> networksByName = NetworkUtils.networksByName(clusterNetworks);

        for (VdsNetworkInterface iface : interfaces) {

            // Handle nics that are non bonded and not vlan over bond
            if (isRequiredInterfaceDown(networksByName, interfaces, iface)) {
                brokenNics.add(iface.getName());
                networks.add(iface.getNetworkName());
            }

            // Handle bond nics
            if (iface.getBondName() != null) {
                populate(bondsWithStatus, interfaces, clusterNetworks, networks, bondsWithListOfNics, iface);
            }
        }

        // check the bond statuses, if one is down we mark it as broken
        // only if we didn't already mark a NIC as broken
        if (brokenNics.isEmpty()) {
            for (Entry<String, Boolean> entry : bondsWithStatus.entrySet()) {
                if (!entry.getValue()) {
                    // add the nics name for audit log
                    for (String name : bondsWithListOfNics.get(entry.getKey())) {
                        brokenNics.add(name);
                    }
                }
            }
        }

        return new Pair<List<String>, List<String>>(brokenNics, networks);
    }

    private static void populate(Map<String, Boolean> bondsWithStatus,
            List<VdsNetworkInterface> interfaces,
            List<Network> clusterNetworks,
            List<String> networks,
            Map<String, List<String>> bondsWithListOfNics,
            VdsNetworkInterface iface) {
        Pair<Boolean, String> retVal =
                isRequiredNetworkInCluster(iface.getBondName(), interfaces, clusterNetworks);
        String networkName = retVal.getSecond();
        if (retVal.getFirst()) {
            if (!bondsWithStatus.containsKey(iface.getBondName())) {
                bondsWithStatus.put(iface.getBondName(), false);
            }
            // It is enough for at least one of the interfaces of the bond to be up
            bondsWithStatus.put(iface.getBondName(),
                    bondsWithStatus.get(iface.getBondName())
                            || (iface.getStatistics().getStatus() == InterfaceStatus.UP));

            if (!networks.contains(networkName)
                    && !bondsWithStatus.containsKey(iface.getName())) {
                networks.add(networkName);
            }
            // we remove the network from the audit log if the bond
            // is active
            else if (networks.contains(networkName) && bondsWithStatus.get(iface.getBondName())) {
                networks.remove(networkName);
            }
            if (!bondsWithListOfNics.containsKey(iface.getBondName())) {
                bondsWithListOfNics.put(iface.getBondName(), new ArrayList<String>());
            }
            bondsWithListOfNics.get(iface.getBondName()).add(iface.getName());
        }
    }

    /**
     * check if an interface implementing a required cluster network is down
     *
     * @param networksByName
     * @param iface
     */
    private static boolean isRequiredInterfaceDown(Map<String, Network> networksByName,
            List<VdsNetworkInterface> interfaces,
            VdsNetworkInterface iface) {
        if (iface.getStatistics().getStatus() != InterfaceStatus.UP
                && iface.getNetworkName() != null
                && iface.getBonded() == null
                && !isBondOrVlanOverBond(iface, interfaces)
                && networksByName.containsKey(iface.getNetworkName())) {

            Network net = networksByName.get(iface.getNetworkName());
            if (net.getCluster().getStatus() == NetworkStatus.OPERATIONAL && net.getCluster().isRequired()
                    && (iface.getVlanId() == null || !isVlanInterfaceUp(iface, interfaces))) {
                return true;
            }
        }
        return false;
    }

    // method get bond name, list of cluster network - checks if the specified
    // bonds network is in the clusterNetworks,
    // if so return true and networkName of the bonds
    private static Pair<Boolean, String> isRequiredNetworkInCluster(String bondName,
            List<VdsNetworkInterface> interfaces,
            List<Network> clusterNetworks) {
        Pair<Boolean, String> retVal = new Pair<Boolean, String>();
        for (VdsNetworkInterface iface : interfaces) {
            if (iface.getName().equals(bondName)) {
                for (Network net : clusterNetworks) {
                    // If this is the network on the bond, or on a vlan over the bond, and the network is required
                    // we want to check this network
                    if ((net.getName().equals(iface.getNetworkName())
                            || isVlanOverBondNetwork(bondName, net.getName(), interfaces))
                            && net.getCluster().isRequired()) {
                        retVal.setFirst(true);
                        retVal.setSecond(net.getName());
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
    private static boolean isBondOrVlanOverBond(VdsNetworkInterface iface, List<VdsNetworkInterface> interfaces) {
        if (iface.getBonded() != null && iface.getBonded() == true) {
            return true;
        }

        // check if vlan over bond i.e if we are in bond0.5 we look for bond0
        String name = NetworkUtils.getVlanInterfaceName(iface.getName());
        if (name == null) {
            return false;
        }

        for (VdsNetworkInterface i : interfaces) {
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
    private static boolean isVlanOverBondNetwork(String bondName,
            String networkName,
            List<VdsNetworkInterface> interfaces) {
        for (VdsNetworkInterface iface : interfaces) {
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
    private static boolean isVlanInterfaceUp(VdsNetworkInterface vlan, List<VdsNetworkInterface> interfaces) {
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
        String ifaceName = StringUtils.stripEnd(sb.toString(), ".");
        for (VdsNetworkInterface iface : interfaces) {
            if (iface.getName().equals(ifaceName)) {
                return iface.getStatistics().getStatus() == InterfaceStatus.UP;
            }
        }

        // not suppose to get here
        return false;
    }

    private void beforeFirstRefreshTreatment(boolean isVdsUpOrGoingToMaintenance) {
        if (_vdsManager.getbeforeFirstRefresh()) {
            boolean flagsChanged = false;
            final AtomicBoolean processHardwareCapsNeededTemp = new AtomicBoolean();
            _vdsManager.refreshCapabilities(processHardwareCapsNeededTemp, _vds);
            flagsChanged = processHardwareCapsNeededTemp.get();
            _vdsManager.setbeforeFirstRefresh(false);
            refreshedCapabilities = true;
            _saveVdsDynamic = true;
            // change the _cpuFlagsChanged flag only if it was false,
            // because get capabilities is called twice on a new server in same
            // loop!
            processHardwareCapsNeeded = (processHardwareCapsNeeded) ? processHardwareCapsNeeded : flagsChanged;
        } else if (isVdsUpOrGoingToMaintenance || _vds.getStatus() == VDSStatus.Error) {
            return;
        }
        AuditLogableBase logable = new AuditLogableBase(_vds.getId());
        logable.addCustomValue("VdsStatus", _vds.getStatus().toString());
        auditLog(logable, AuditLogType.VDS_DETECTED);
    }

    protected void refreshVmStats() {
        if (Config.<Boolean> getValue(ConfigValues.DebugTimerLogging)) {
            log.debug("vds::refreshVmList entered");
        }

        if (fetchRunningVms()) {
            // refreshCommitedMemory must be called before we modify _runningVms, because
            // we iterate over it there, assuming it is the same as it was received from VDSM
            refreshCommitedMemory();
            List<Guid> staleRunningVms = checkVmsStatusChanged();

            proceedWatchdogEvents();

            proceedBalloonCheck();

            proceedDownVms();

            proceedGuaranteedMemoryCheck();

            processExternallyManagedVms();
            // update repository and check if there are any vm in cache that not
            // in vdsm
            updateRepository(staleRunningVms);
            // Going over all returned VMs and updting the data structures
            // accordingly

            // checking the db for incoherent vm status;
            // setVmStatusDownForVmNotFound();

            // Handle VM devices were changed (for 3.1 cluster and above)
            if (!VmDeviceCommonUtils.isOldClusterVersion(_vds.getVdsGroupCompatibilityVersion())) {
                handleVmDeviceChange();
            }

            prepareGuestAgentNetworkDevicesForUpdate();

            updateLunDisks();
        }
    }

    /**
     * fetch running VMs and populate the internal structure. if we fail, handle the error
     * @return true if we could get vms otherwise false
     */
    protected boolean fetchRunningVms() {
        VDSCommandType commandType =
                _vdsManager.getRefreshStatistics()
                        ? VDSCommandType.GetAllVmStats
                        : VDSCommandType.List;
        VDSReturnValue vdsReturnValue =
                getResourceManager().runVdsCommand(commandType, new VdsIdAndVdsVDSCommandParametersBase(_vds));
        _runningVms = (Map<Guid, VmInternalData>) vdsReturnValue.getReturnValue();

        if (!vdsReturnValue.getSucceeded()) {
            RuntimeException callException = vdsReturnValue.getExceptionObject();
            if (callException != null) {
                if (callException instanceof VDSErrorException) {
                    log.errorFormat("Failed vds listing,  vds = {0} : {1}, error = {2}", _vds.getId(),
                            _vds.getName(), vdsReturnValue.getExceptionString());
                } else if (callException instanceof VDSNetworkException) {
                    _saveVdsDynamic = _vdsManager.handleNetworkException((VDSNetworkException) callException, _vds);
                } else if (callException instanceof VDSProtocolException) {
                    log.errorFormat("Failed vds listing,  vds = {0} : {1}, error = {2}", _vds.getId(),
                            _vds.getName(), vdsReturnValue.getExceptionString());
                }
                throw callException;
            } else {
                log.errorFormat("{0} failed with no exception!", commandType.name());
            }
        }

        return vdsReturnValue.getSucceeded();
    }

    /**
     * Prepare the VM Guest Agent network devices for update. <br>
     * The evaluation of the network devices for update is done by comparing the calculated hash of the network devices
     * from VDSM to the latest hash kept on engine side.
     */
    private void prepareGuestAgentNetworkDevicesForUpdate() {
        for (VmInternalData vmInternalData : _runningVms.values()) {
            VmDynamic vmDynamic = vmInternalData.getVmDynamic();
            if (vmDynamic != null) {
                VM vm = _vmDict.get(vmDynamic.getId());
                if (vm != null) {
                    List<VmGuestAgentInterface> vmGuestAgentInterfaces = vmInternalData.getVmGuestAgentInterfaces();
                    int guestAgentNicHash = vmGuestAgentInterfaces == null ? 0 : vmGuestAgentInterfaces.hashCode();
                    if (guestAgentNicHash != vmDynamic.getGuestAgentNicsHash()) {
                        vmGuestAgentNics.put(vmDynamic.getId(), vmGuestAgentInterfaces);

                        // update new hash value
                        if (_vmDynamicToSave.containsKey(vm.getId())) {
                            updateGuestAgentInterfacesChanges(_vmDynamicToSave.get(vm.getId()),
                                    vmGuestAgentInterfaces,
                                    guestAgentNicHash);
                        } else {
                            updateGuestAgentInterfacesChanges(vmDynamic, vmGuestAgentInterfaces, guestAgentNicHash);
                            addVmDynamicToList(vmDynamic);
                        }
                    }
                }
            }
        }
    }

    protected void updateLunDisks() {
        // Looping only over powering up VMs as LUN device size
        // is updated by VDSM only once when running a VM.
        for (VmDynamic vmDynamic : getPoweringUpVms()) {
            VmInternalData vmInternalData = getRunningVms().get(vmDynamic.getId());
            if (vmInternalData != null) {
                Map<String, LUNs> lunsMap = vmInternalData.getLunsMap();
                if (lunsMap.isEmpty()) {
                    // LUNs list from getVmStats hasn't been updated yet or VDSM doesn't support LUNs list retrieval.
                    continue;
                }

                List<Disk> vmDisks = getDbFacade().getDiskDao().getAllForVm(vmDynamic.getId(), true);
                for (Disk disk : vmDisks) {
                    if (disk.getDiskStorageType() != DiskStorageType.LUN) {
                        continue;
                    }

                    LUNs lunFromDB = ((LunDisk) disk).getLun();
                    LUNs lunFromMap = lunsMap.get(lunFromDB.getId());

                    // LUN's device size might be returned as zero in case of an error in VDSM;
                    // Hence, verify before updating.
                    if (lunFromMap.getDeviceSize() != 0 && lunFromMap.getDeviceSize() != lunFromDB.getDeviceSize()) {
                        // Found a mismatch - set LUN for update
                        log.infoFormat("Updated LUN device size - ID: {0}, previous size: {1}, new size: {2}.",
                                lunFromDB.getLUN_id(), lunFromDB.getDeviceSize(), lunFromMap.getDeviceSize());

                        lunFromDB.setDeviceSize(lunFromMap.getDeviceSize());
                        vmLunDisksToSave.add(lunFromDB);
                    }
                }
            }
        }
    }

    private void updateGuestAgentInterfacesChanges(VmDynamic vmDynamic,
            List<VmGuestAgentInterface> vmGuestAgentInterfaces,
            int guestAgentNicHash) {
        vmDynamic.setGuestAgentNicsHash(guestAgentNicHash);
        vmDynamic.setVmIp(extractVmIpsFromGuestAgentInterfaces(vmGuestAgentInterfaces));
    }

    private String extractVmIpsFromGuestAgentInterfaces(List<VmGuestAgentInterface> nics) {
        if (nics == null || nics.isEmpty()) {
            return null;
        }

        List<String> ips = new ArrayList<String>();
        for (VmGuestAgentInterface nic : nics) {
            if (nic.getIpv4Addresses() != null) {
                ips.addAll(nic.getIpv4Addresses());
            }
        }
        return ips.isEmpty() ? null : StringUtils.join(ips, " ");
    }

    /**
     * Handle changes in all VM devices
     */
    private void handleVmDeviceChange() {
        // Go over all the vms and determine which ones require updating
        // Update only running VMs
        List<String> vmsToUpdateFromVds = new ArrayList<String>();
        for (VmInternalData vmInternalData : _runningVms.values()) {
            VmDynamic vmDynamic = vmInternalData.getVmDynamic();
            if (vmDynamic != null && vmDynamic.getStatus() != VMStatus.MigratingTo) {
                VM vm = _vmDict.get(vmDynamic.getId());
                if (vm != null) {
                    String dbHash = vm.getHash();
                    if ((dbHash == null && vmDynamic.getHash() != null) || (dbHash != null)
                            && !dbHash.equals(vmDynamic.getHash())) {
                        vmsToUpdateFromVds.add(vmDynamic.getId().toString());
                        // update new hash value
                        if (_vmDynamicToSave.containsKey(vm.getId())) {
                            _vmDynamicToSave.get(vm.getId()).setHash(vmDynamic.getHash());
                        } else {
                            addVmDynamicToList(vmDynamic);
                        }
                    }
                }
            }
        }

        if (vmsToUpdateFromVds.size() > 0) {
            // If there are vms that require updating,
            // get the new info from VDSM in one call, and then update them all
            updateVmDevices(vmsToUpdateFromVds);
        }
    }

    /**
     * Update the given list of VMs properties in DB
     *
     * @param vmsToUpdate
     */
    protected void updateVmDevices(List<String> vmsToUpdate) {
        Map[] vms = getVmInfo(vmsToUpdate);
        if (vms != null) {
            for (Map vm : vms) {
                processVmDevices(vm);
            }
        }
    }

    /**
     * gets VM full information for the given list of VMs
     *
     * @param vmsToUpdate
     * @return
     */
    protected Map[] getVmInfo(List<String> vmsToUpdate) {
        return (Map[]) (new FullListVdsCommand<FullListVDSCommandParameters>(
                new FullListVDSCommandParameters(_vds, vmsToUpdate)).executeWithReturnValue());
    }

    private boolean shouldLogDeviceDetails(String deviceType) {
        return !StringUtils.equalsIgnoreCase(deviceType, VmDeviceType.FLOPPY.getName());
    }

    private void logDeviceInformation(Guid vmId, Map device) {
        String message = "Received a {0} Device without an address when processing VM {1} devices, skipping device";
        String deviceType = getDeviceType(device);

        if (shouldLogDeviceDetails(deviceType)) {
            Map<String, Object> deviceInfo = device;
            log.infoFormat(message + ": {2}", StringUtils.defaultString(deviceType), vmId, deviceInfo);
        } else {
            log.infoFormat(message, StringUtils.defaultString(deviceType), vmId);
        }
    }

    /**
     * Actually process the VM device update in DB.
     *
     * @param vm
     */
    private void processVmDevices(Map vm) {
        if (vm == null || vm.get(VdsProperties.vm_guid) == null) {
            log.error("Received NULL VM or VM id when processing VM devices, abort.");
            return;
        }

        Guid vmId = new Guid((String) vm.get(VdsProperties.vm_guid));
        Set<Guid> processedDevices = new HashSet<Guid>();
        List<VmDevice> devices = getDbFacade().getVmDeviceDao().getVmDeviceByVmId(vmId);
        Map<VmDeviceId, VmDevice> deviceMap = Entities.businessEntitiesById(devices);

        for (Object o : (Object[]) vm.get(VdsProperties.Devices)) {
            Map device = (Map<String, Object>) o;
            if (device.get(VdsProperties.Address) == null) {
                logDeviceInformation(vmId, device);
                continue;
            }

            Guid deviceId = getDeviceId(device);
            VmDevice vmDevice = deviceMap.get(new VmDeviceId(deviceId, vmId));
            if (deviceId == null || vmDevice == null) {
                deviceId = addNewVmDevice(vmId, device);
            } else {
                vmDevice.setAddress(((Map<String, String>) device.get(VdsProperties.Address)).toString());
                vmDevice.setAlias(StringUtils.defaultString((String) device.get(VdsProperties.Alias)));
                addVmDeviceToList(vmDevice);
            }

            processedDevices.add(deviceId);
        }

        handleRemovedDevices(vmId, processedDevices, devices);
    }

    /**
     * Removes unmanaged devices from DB if were removed by libvirt. Empties device address with isPlugged = false
     *
     * @param vmId
     * @param processedDevices
     */
    private void handleRemovedDevices(Guid vmId, Set<Guid> processedDevices, List<VmDevice> devices) {
        for (VmDevice device : devices) {
            if (processedDevices.contains(device.getDeviceId())) {
                continue;
            }

            if (device.getIsManaged()) {
                if (device.getIsPlugged()) {
                    device.setAddress("");
                    addVmDeviceToList(device);
                    log.debugFormat("VM {0} managed pluggable device was unplugged : {1}", vmId, device);
                } else if (!devicePluggable(device)) {
                    log.errorFormat("VM {0} managed non pluggable device was removed unexpectedly from libvirt: {1}",
                            vmId, device);
                }
            } else {
                removedDeviceIds.add(device.getId());
                log.debugFormat("VM {0} unmanaged device was marked for remove : {1}", vmId, device);
            }
        }
    }

    private boolean devicePluggable(VmDevice device) {
        return VmDeviceCommonUtils.isDisk(device) || VmDeviceCommonUtils.isBridge(device);
    }

    /**
     * Adds new devices recognized by libvirt
     *
     * @param vmId
     * @param device
     */
    private Guid addNewVmDevice(Guid vmId, Map device) {
        Guid newDeviceId = Guid.Empty;
        String typeName = (String) device.get(VdsProperties.Type);
        String deviceName = (String) device.get(VdsProperties.Device);

        // do not allow null or empty device or type values
        if (StringUtils.isEmpty(typeName) || StringUtils.isEmpty(deviceName)) {
            log.errorFormat("Empty or NULL values were passed for a VM {0} device, Device is skipped", vmId);
        } else {
            String address = ((Map<String, String>) device.get(VdsProperties.Address)).toString();
            String alias = StringUtils.defaultString((String) device.get(VdsProperties.Alias));
            Object o = device.get(VdsProperties.SpecParams);
            newDeviceId = Guid.newGuid();
            VmDeviceId id = new VmDeviceId(newDeviceId, vmId);
            VmDevice newDevice = new VmDevice(id, VmDeviceGeneralType.forValue(typeName), deviceName, address,
                    0,
                    o == null ? new HashMap<String, Object>() : (Map<String, Object>) o,
                    false,
                    true,
                    Boolean.getBoolean((String) device.get(VdsProperties.ReadOnly)),
                    alias,
                    null,
                    null);
            newVmDevices.add(newDevice);
            log.debugFormat("New device was marked for adding to VM {0} Devices : {1}", vmId, newDevice);
        }

        return newDeviceId;
    }

    /**
     * gets the device id from the structure returned by VDSM device ids are stored in specParams map
     *
     * @param device
     * @return
     */
    private static Guid getDeviceId(Map device) {
        String deviceId = (String) device.get(VdsProperties.DeviceId);
        return deviceId == null ? null : new Guid(deviceId);
    }

    /**
     * gets the device type from the structure returned by VDSM
     *
     * @param device
     * @return
     */
    private static String getDeviceType(Map device) {
        return (String) device.get(VdsProperties.Device);
    }

    // if not statistics check if status changed return a list of those
    protected List<Guid> checkVmsStatusChanged() {
        List<Guid> staleRunningVms = new ArrayList<>();
        if (!_vdsManager.getRefreshStatistics()) {
            List<VmDynamic> tempRunningList = new ArrayList<VmDynamic>();
            for (VmInternalData runningVm : _runningVms.values()) {
                tempRunningList.add(runningVm.getVmDynamic());
            }
            for (VmDynamic runningVm : tempRunningList) {
                VM vmToUpdate = _vmDict.get(runningVm.getId());

                boolean statusChanged = false;
                if (vmToUpdate == null
                        || (vmToUpdate.getStatus() != runningVm.getStatus() &&
                        !(vmToUpdate.getStatus() == VMStatus.PreparingForHibernate && runningVm.getStatus() == VMStatus.Up))) {
                    VDSReturnValue vmStats =
                            getResourceManager().runVdsCommand(
                                    VDSCommandType.GetVmStats,
                                    new GetVmStatsVDSCommandParameters(_vds, runningVm.getId()));
                    if (vmStats.getSucceeded()) {
                        _runningVms.put(runningVm.getId(), (VmInternalData) vmStats.getReturnValue());
                        statusChanged = true;
                    } else {
                        if (vmToUpdate != null) {
                            log.errorFormat(
                                    "failed to fetch {0} stats. status remain unchanged ({1})",
                                    vmToUpdate.getName(),
                                    vmToUpdate.getStatus());
                        }
                    }
                }

                if (!statusChanged) {
                    // status not changed move to next vm
                    staleRunningVms.add(runningVm.getId());
                    _runningVms.remove(runningVm.getId());
                }
            }
        }
        return staleRunningVms;
    }

    private void proceedWatchdogEvents() {
        for (VmInternalData vmInternalData : _runningVms.values()) {
            VmDynamic vmDynamic = vmInternalData.getVmDynamic();
            VM vmTo = _vmDict.get(vmDynamic.getId());
            if (isNewWatchdogEvent(vmDynamic, vmTo)) {
                AuditLogableBase auditLogable = new AuditLogableBase();
                auditLogable.setVmId(vmDynamic.getId());
                auditLogable.addCustomValue("wdaction", vmDynamic.getLastWatchdogAction());
                // for the interpretation of vdsm's response see http://docs.python.org/2/library/time.html
                auditLogable.addCustomValue("wdevent",
                        ObjectUtils.toString(new Date(vmDynamic.getLastWatchdogEvent().longValue() * 1000)));
                AuditLogDirector.log(auditLogable, AuditLogType.WATCHDOG_EVENT);
            }
        }
    }

    private void proceedGuaranteedMemoryCheck() {
        for (VmInternalData vmInternalData : _runningVms.values()) {
            VM savedVm = _vmDict.get(vmInternalData.getVmDynamic().getId());
            if (savedVm == null) {
                continue;
            }
            VmStatistics vmStatistics = vmInternalData.getVmStatistics();
            if (vmStatistics != null && vmStatistics.getVmBalloonInfo().getCurrentMemory() != null &&
                    vmStatistics.getVmBalloonInfo().getCurrentMemory() > 0 &&
                    savedVm.getMinAllocatedMem() > vmStatistics.getVmBalloonInfo().getCurrentMemory() / TO_MEGA_BYTES) {
                AuditLogableBase auditLogable = new AuditLogableBase();
                auditLogable.addCustomValue("VmName", savedVm.getName());
                auditLogable.addCustomValue("VdsName", this._vds.getName());
                auditLogable.addCustomValue("MemGuaranteed", String.valueOf(savedVm.getMinAllocatedMem()));
                auditLogable.addCustomValue("MemActual",
                        Long.toString((vmStatistics.getVmBalloonInfo().getCurrentMemory() / TO_MEGA_BYTES)));
                auditLog(auditLogable, AuditLogType.VM_MEMORY_UNDER_GUARANTEED_VALUE);
            }

        }
    }

    private void proceedBalloonCheck() {
        if (isBalloonActiveOnHost()) {
            for (VmInternalData vmInternalData : _runningVms.values()) {
                VmBalloonInfo balloonInfo = vmInternalData.getVmStatistics().getVmBalloonInfo();
                Guid vmId = vmInternalData.getVmDynamic().getId();
                if (_vmDict.get(vmId) == null) {
                    continue; // if vm is unknown - continue
                }

                if (isBalloonDeviceActiveOnVm(vmInternalData)
                        && (Objects.equals(balloonInfo.getCurrentMemory(), balloonInfo.getBalloonMaxMemory())
                        || !Objects.equals(balloonInfo.getCurrentMemory(), balloonInfo.getBalloonTargetMemory()))) {
                    vmBalloonDriverIsRequestedAndUnavailable(vmId);
                } else {
                    vmBalloonDriverIsNotRequestedOrAvailable(vmId);
                }

                if (vmInternalData.getVmStatistics().getusage_mem_percent() != null
                        && vmInternalData.getVmStatistics().getusage_mem_percent() == 0 // guest agent is down
                        && balloonInfo.isBalloonDeviceEnabled() // check if the device is present
                        && !Objects.equals(balloonInfo.getCurrentMemory(), balloonInfo.getBalloonMaxMemory())) {
                    guestAgentIsDownAndBalloonInfalted(vmId);
                } else {
                    guestAgentIsUpOrBalloonDeflated(vmId);
                }

            }
        }
    }

    // remove the vm from the list of vms with uncontrolled inflated balloon
    private void guestAgentIsUpOrBalloonDeflated(Guid vmId) {
        vmsWithUncontrolledBalloon.remove(vmId);
    }

    // add the vm to the list of vms with uncontrolled inflated balloon or increment its counter
    // if it is already in the list
    private void guestAgentIsDownAndBalloonInfalted(Guid vmId) {
        Integer currentVal = vmsWithUncontrolledBalloon.get(vmId);
        if (currentVal == null) {
            vmsWithUncontrolledBalloon.put(vmId, 1);
        } else {
            vmsWithUncontrolledBalloon.put(vmId, currentVal + 1);
            if (currentVal >= Config.<Integer> getValue(ConfigValues.IterationsWithBalloonProblem)) {
                AuditLogableBase auditLogable = new AuditLogableBase();
                auditLogable.setVmId(vmId);
                AuditLogDirector.log(auditLogable, AuditLogType.VM_BALLOON_DRIVER_UNCONTROLLED);
                vmsWithUncontrolledBalloon.put(vmId, 0);
            }
        }
    }

    // remove the vm from the list of vms with balloon driver problem
    private void vmBalloonDriverIsNotRequestedOrAvailable(Guid vmId) {
        vmsWithBalloonDriverProblem.remove(vmId);
    }

    // add the vm to the list of vms with balloon driver problem or increment its counter
    // if it is already in the list
    private void vmBalloonDriverIsRequestedAndUnavailable(Guid vmId) {
        Integer currentVal = vmsWithBalloonDriverProblem.get(vmId);
        if (currentVal == null) {
            vmsWithBalloonDriverProblem.put(vmId, 1);
        } else {
            vmsWithBalloonDriverProblem.put(vmId, currentVal + 1);
            if (currentVal >= Config.<Integer> getValue(ConfigValues.IterationsWithBalloonProblem)) {
                AuditLogableBase auditLogable = new AuditLogableBase();
                auditLogable.setVmId(vmId);
                AuditLogDirector.log(auditLogable, AuditLogType.VM_BALLOON_DRIVER_ERROR);
                vmsWithBalloonDriverProblem.put(vmId, 0);
            }
        }
    }

    private boolean isBalloonActiveOnHost() {
        VDSGroup cluster = getDbFacade().getVdsGroupDao().get(_vds.getVdsGroupId());
        return cluster != null && cluster.isEnableBallooning();
    }

    private boolean isBalloonDeviceActiveOnVm(VmInternalData vmInternalData) {
        VM savedVm = _vmDict.get(vmInternalData.getVmDynamic().getId());

        if (savedVm != null) {
            VmBalloonInfo balloonInfo = vmInternalData.getVmStatistics().getVmBalloonInfo();
            return savedVm.getMinAllocatedMem() < savedVm.getMemSizeMb() // minimum allocated mem of VM == total mem, ballooning is impossible
                    && balloonInfo.isBalloonDeviceEnabled()
                    && balloonInfo.getBalloonTargetMemory().intValue() != balloonInfo.getBalloonMaxMemory().intValue(); // ballooning was not requested/enabled on this VM
        }
        return false;
    }

    private Long toMegaByte(Long kilobytes) {
        if (kilobytes != null && kilobytes > 0) {
            return kilobytes / TO_MEGA_BYTES;
        }
        return 0L;
    }

    protected static boolean isNewWatchdogEvent(VmDynamic vmDynamic, VM vmTo) {
        Long lastWatchdogEvent = vmDynamic.getLastWatchdogEvent();
        return vmTo != null && lastWatchdogEvent != null
                && (vmTo.getLastWatchdogEvent() == null || vmTo.getLastWatchdogEvent() < lastWatchdogEvent);
    }

    /**
     * Delete all vms with status Down
     */
    private void proceedDownVms() {
        for (VmInternalData vmInternalData : _runningVms.values()) {
            VmDynamic vm = vmInternalData.getVmDynamic();
            if (vm.getStatus() != VMStatus.Down) {
                continue;
            }

            VM vmTo = _vmDict.get(vm.getId());
            VMStatus status = VMStatus.Unassigned;
            if (vmTo != null) {
                status = vmTo.getStatus();
                proceedVmBeforeDeletion(vmTo, vm);

                // when going to suspend, delete vm from cache later
                if (status == VMStatus.SavingState) {
                    ResourceManager.getInstance().InternalSetVmStatus(vmTo, VMStatus.Suspended);
                }

                clearVm(vmTo, vmInternalData.getVmDynamic().getExitStatus(), vmInternalData.getVmDynamic()
                        .getExitMessage());
            }

            VmStatistics vmStatistics = getDbFacade().getVmStatisticsDao().get(vm.getId());
            if (vmStatistics != null) {
                DestroyVDSCommand<DestroyVmVDSCommandParameters> vdsBrokerCommand =
                        new DestroyVDSCommand<DestroyVmVDSCommandParameters>(new DestroyVmVDSCommandParameters(
                                _vds.getId(), vm.getId(), false, false, 0));
                vdsBrokerCommand.execute();

                if (vmTo != null && status == VMStatus.SavingState) {
                    afterSuspendTreatment(vm);
                } else if (status != VMStatus.MigratingFrom) {
                    handleVmOnDown(vmTo, vm, vmStatistics);
                }
            }
        }
    }

    private void handleVmOnDown(VM cacheVm, VmDynamic vmDynamic, VmStatistics vmStatistics) {
        VmExitStatus exitStatus = vmDynamic.getExitStatus();

        if (exitStatus != VmExitStatus.Normal) {

            auditVmOnDownEvent(exitStatus, vmDynamic.getExitMessage(), vmStatistics.getId());

            // Vm failed to run - try to rerun it on other Vds
            if (cacheVm != null) {
                if (ResourceManager.getInstance().IsVmInAsyncRunningList(vmDynamic.getId())) {
                    log.infoFormat("Running on vds during rerun failed vm: {0}", vmDynamic.getRunOnVds());
                    _vmsToRerun.add(vmDynamic.getId());
                } else if (cacheVm.isAutoStartup()) {
                    _autoVmsToRun.add(vmDynamic.getId());
                }
            }
            // if failed in destination right after migration
            else { // => cacheVm == null
                ResourceManager.getInstance().RemoveAsyncRunningVm(vmDynamic.getId());
                addVmDynamicToList(vmDynamic);
            }
        } else {
            // if went down normally during migration process (either on source or on destination)
            // don't generate an event
            if (cacheVm != null) {
                auditVmOnDownEvent(exitStatus, vmDynamic.getExitMessage(), vmStatistics.getId());
            }

            // Vm moved safely to down status. May be migration - just remove it from Async Running command.
            ResourceManager.getInstance().RemoveAsyncRunningVm(vmDynamic.getId());
        }
    }

    /**
     * Generate an error or information event according to the exit status of a VM in status 'down'
     */
    private void auditVmOnDownEvent(VmExitStatus exitStatus, String exitMessage, Guid vmStatisticsId) {
        AuditLogType type = exitStatus == VmExitStatus.Normal ? AuditLogType.VM_DOWN : AuditLogType.VM_DOWN_ERROR;
        AuditLogableBase logable = new AuditLogableBase(_vds.getId(), vmStatisticsId);
        if (exitMessage != null) {
            logable.addCustomValue("ExitMessage", "Exit message: " + exitMessage);
        }
        auditLog(logable, type);
    }

    private void afterSuspendTreatment(VmDynamic vm) {
        AuditLogType type = vm.getExitStatus() == VmExitStatus.Normal ? AuditLogType.USER_SUSPEND_VM_OK
                : AuditLogType.USER_FAILED_SUSPEND_VM;

        AuditLogableBase logable = new AuditLogableBase(_vds.getId(), vm.getId());
        auditLog(logable, type);
        ResourceManager.getInstance().RemoveAsyncRunningVm(vm.getId());
    }

    private void proceedVmBeforeDeletion(VM curVm, VmDynamic vmDynamic) {
        AuditLogType type = AuditLogType.UNASSIGNED;
        AuditLogableBase logable = new AuditLogableBase(_vds.getId(), curVm.getId());
        switch (curVm.getStatus()) {
        case MigratingFrom: {
            // if a VM that was a source host in migration process is now down with normal
            // exit status that's OK, otherwise..
            if (vmDynamic != null && vmDynamic.getExitStatus() != VmExitStatus.Normal) {
                if (curVm.getMigratingToVds() != null) {
                    DestroyVmVDSCommand<DestroyVmVDSCommandParameters> destroyCmd =
                            new DestroyVmVDSCommand<DestroyVmVDSCommandParameters>
                            (new DestroyVmVDSCommandParameters(new Guid(curVm.getMigratingToVds().toString()),
                                    curVm.getId(),
                                    true,
                                    false,
                                    0));
                    destroyCmd.execute();
                    if (destroyCmd.getVDSReturnValue().getSucceeded()) {
                        log.infoFormat("Stopped migrating vm: {0} on vds: {1}", curVm.getName(),
                                curVm.getMigratingToVds());
                    } else {
                        log.infoFormat("Could not stop migrating vm: {0} on vds: {1}, Error: {2}", curVm.getName(),
                                curVm.getMigratingToVds(), destroyCmd.getVDSReturnValue().getExceptionString());
                    }
                }
                // set vm status to down if source vm crushed
                ResourceManager.getInstance().InternalSetVmStatus(curVm,
                        VMStatus.Down,
                        vmDynamic.getExitStatus(),
                        vmDynamic.getExitMessage());
                addVmDynamicToList(curVm.getDynamicData());
                addVmStatisticsToList(curVm.getStatisticsData());
                addVmInterfaceStatisticsToList(curVm.getInterfaces());
                type = AuditLogType.VM_MIGRATION_ABORT;
                logable.addCustomValue("MigrationError", vmDynamic.getExitMessage());

                ResourceManager.getInstance().RemoveAsyncRunningVm(vmDynamic.getId());
            }
            break;
        }
        default:
            break;
        }
        if (type != AuditLogType.UNASSIGNED) {
            auditLog(logable, type);
        }
    }

    protected void processExternallyManagedVms() {
        List<String> vmsToQuery = new ArrayList<String>();
        // Searching for External VMs that run on the host
        for (VmInternalData vmInternalData : _runningVms.values()) {
            VM currentVmData = _vmDict.get(vmInternalData.getVmDynamic().getId());
            if (currentVmData == null) {
                if (getDbFacade().getVmStaticDao().get(vmInternalData.getVmDynamic().getId()) == null) {
                    Guid vmId = vmInternalData.getVmDynamic().getId();
                    vmsToQuery.add(vmId.toString());
                }
            }
        }
        // Fetching for details from the host
        // and marking the VMs for addition
        if (!vmsToQuery.isEmpty()) {
            // Query VDSM for VMs info, and creating a proper VMStatic to be used when importing them
            Map[] vmsInfo = getVmInfo(vmsToQuery);
            for (Map vmInfo : vmsInfo) {
                Guid vmId = Guid.createGuidFromString((String) vmInfo.get(VdsProperties.vm_guid));
                VmStatic vmStatic = new VmStatic();
                vmStatic.setId(vmId);
                vmStatic.setCreationDate(new Date());
                vmStatic.setVdsGroupId(_vds.getVdsGroupId());
                String vmNameOnHost = (String) vmInfo.get(VdsProperties.vm_name);

                if (StringUtils.equals(HOSTED_ENGINE_VM_NAME, vmNameOnHost)) {
                    vmStatic.setName(vmNameOnHost);
                    vmStatic.setOrigin(OriginType.HOSTED_ENGINE);
                    vmStatic.setMigrationSupport(MigrationSupport.IMPLICITLY_NON_MIGRATABLE);
                } else {
                    vmStatic.setName(String.format(EXTERNAL_VM_NAME_FORMAT, vmNameOnHost));
                    vmStatic.setOrigin(OriginType.EXTERNAL);
                }

                vmStatic.setNumOfSockets(parseIntVdsProperty(vmInfo.get(VdsProperties.num_of_cpus)));
                vmStatic.setMemSizeMb(parseIntVdsProperty(vmInfo.get(VdsProperties.mem_size_mb)));
                vmStatic.setSingleQxlPci(false);

                _externalVmsToAdd.add(vmStatic);
                log.infoFormat("Importing VM {0} as {1}, as it is running on the on Host, but does not exist in the engine.", vmNameOnHost, vmStatic.getName());
            }
        }
    }

    // Some properties were changed recently from String to Integer
    // This method checks what type is the property, and returns int
    private int parseIntVdsProperty(Object vdsProperty) {
        if (vdsProperty instanceof Integer) {
            return (Integer) vdsProperty;
        } else {
            return Integer.parseInt((String) vdsProperty);
        }
    }

    private void updateRepository(List<Guid> staleRunningVms) {
        for (VmInternalData vmInternalData : _runningVms.values()) {
            VmDynamic runningVm = vmInternalData.getVmDynamic();
            VM vmToUpdate = _vmDict.get(runningVm.getId());

            // if not migrating here and not down
            if (!inMigrationTo(runningVm, vmToUpdate) && runningVm.getStatus() != VMStatus.Down) {
                if (vmToUpdate != null) {
                    if (_vmDict.containsKey(vmToUpdate.getId())
                            && !StringUtils.equals(runningVm.getClientIp(), vmToUpdate.getClientIp())) {
                        _vmsClientIpChanged.put(vmToUpdate, runningVm);
                    }
                }
                if (vmToUpdate != null) {
                    logVmStatusTransition(vmToUpdate, runningVm);
                    // open spice for dedicated VMs
                    if (vmToUpdate.getStatus() != VMStatus.Up && runningVm.getStatus() == VMStatus.Up
                            || vmToUpdate.getStatus() != VMStatus.PoweringUp
                            && runningVm.getStatus() == VMStatus.PoweringUp) {
                        // Vm moved to powering Up or up status - launch spice
                        // if no current client ip already connected.
                        if (runningVm.getDisplay() != null) {
                            _poweringUpVms.add(runningVm);
                        } else {
                            log.error("UpdateRepository - runningVm.display is null, cannot start spice for it");
                        }
                    }

                    // Generate an event for those machines that transition from "PoweringDown" to
                    // "Up" as this means that the power down operation failed:
                    if (vmToUpdate.getStatus() == VMStatus.PoweringDown && runningVm.getStatus() == VMStatus.Up) {
                        AuditLogableBase logable = new AuditLogableBase(_vds.getId(), vmToUpdate.getId());
                        auditLog(logable, AuditLogType.VM_POWER_DOWN_FAILED);
                    }

                    if (vmToUpdate.getStatus() != VMStatus.Up && vmToUpdate.getStatus() != VMStatus.MigratingFrom
                            && runningVm.getStatus() == VMStatus.Up) {
                        // Vm moved to Up status - remove its record from Async
                        // reportedAndUnchangedVms handling
                        if (log.isDebugEnabled()) {
                            log.debugFormat("removing VM {0} from successful run VMs list", vmToUpdate.getId());
                        }
                        if (!_succededToRunVms.contains(vmToUpdate.getId())) {
                            _succededToRunVms.add(vmToUpdate.getId());
                        }
                    }
                    afterMigrationFrom(runningVm, vmToUpdate);

                    if (vmToUpdate.getStatus() != VMStatus.NotResponding
                            && runningVm.getStatus() == VMStatus.NotResponding) {
                        AuditLogableBase logable = new AuditLogableBase(_vds.getId(), vmToUpdate.getId());
                        auditLog(logable, AuditLogType.VM_NOT_RESPONDING);
                    }
                    // check if vm is suspended and remove it from async list
                    else if (runningVm.getStatus() == VMStatus.Paused) {
                        _vmsToRemoveFromAsync.add(vmToUpdate.getId());
                        if (vmToUpdate.getStatus() != VMStatus.Paused) {
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
                                auditLog(logable, logType);
                            }
                        }

                    }
                }
                if (vmToUpdate != null || runningVm.getStatus() != VMStatus.MigratingFrom) {
                    RefObject<VM> tempRefObj = new RefObject<VM>(vmToUpdate);
                    boolean updateSucceed = updateVmRunTimeInfo(tempRefObj, runningVm);
                    vmToUpdate = tempRefObj.argvalue;
                    if (updateSucceed) {
                        addVmDynamicToList(vmToUpdate.getDynamicData());
                    }
                }
                if (vmToUpdate != null) {
                    updateVmStatistics(vmToUpdate);
                    if (_vmDict.containsKey(runningVm.getId())) {
                        staleRunningVms.add(runningVm.getId());
                        if (!_vdsManager.getInitialized()) {
                            ResourceManager.getInstance().RemoveVmFromDownVms(_vds.getId(), runningVm.getId());
                        }
                    }
                }
            } else {
                if (runningVm.getStatus() == VMStatus.MigratingTo) {
                    staleRunningVms.add(runningVm.getId());
                }

                VmDynamic vmDynamic = getDbFacade().getVmDynamicDao().get(runningVm.getId());
                if (vmDynamic == null || vmDynamic.getStatus() != VMStatus.Unknown) {
                    _vmDynamicToSave.remove(runningVm.getId());
                }
            }
        }
        // compare between vm in cache and vm from vdsm
        removeVmsFromCache(staleRunningVms);
    }

    private static void logVmStatusTransition(VM vmToUpdate, VmDynamic runningVm) {
        if (vmToUpdate.getStatus() != runningVm.getStatus()) {
            log.infoFormat("VM {0} {1} moved from {2} --> {3}",
                    vmToUpdate.getName(),
                    vmToUpdate.getId(),
                    vmToUpdate.getStatus().name(),
                    runningVm.getStatus().name());

            if (vmToUpdate.getStatus() == VMStatus.Unknown) {
                logVmStatusTransionFromUnknown(vmToUpdate, runningVm);
            }
        }
    }

    // del from cache all vms that not in vdsm
    private void removeVmsFromCache(List<Guid> staleRunningVms) {
        Guid vmGuid;
        for (VM vmToRemove : _vmDict.values()) {
            if (staleRunningVms.contains(vmToRemove.getId())) {
                continue;
            }
            proceedVmBeforeDeletion(vmToRemove, null);
            boolean isInMigration = false;
            if (vmToRemove.getStatus() == VMStatus.MigratingFrom) {
                isInMigration = true;
                handOverVM(vmToRemove);
            } else {
                clearVm(vmToRemove,
                        VmExitStatus.Error,
                        String.format("Could not find VM %s on host, assuming it went down unexpectedly",
                                vmToRemove.getName()));
            }

            log.infoFormat("VM {0} ({1}) is running in db and not running in VDS {2}",
                    vmToRemove.getName(), vmToRemove.getId(), _vds.getName());

            vmGuid = vmToRemove.getId();
            if (!isInMigration && !_vmsToRerun.contains(vmGuid)
                    && ResourceManager.getInstance().IsVmInAsyncRunningList(vmGuid)) {
                _vmsToRerun.add(vmGuid);
                log.infoFormat("add VM {0} to rerun treatment", vmToRemove.getName());
            }
            // vm should be auto startup
            // not already in start up list
            // not in reported from vdsm at all
            // or reported from vdsm with error code
            else if (vmToRemove.isAutoStartup()
                    && !_autoVmsToRun.contains(vmGuid)
                    && (!_runningVms.containsKey(vmGuid) || (_runningVms.containsKey(vmGuid) && _runningVms.get(vmGuid)
                            .getVmDynamic()
                            .getExitStatus() != VmExitStatus.Normal))) {
                _autoVmsToRun.add(vmGuid);
                log.infoFormat("add VM {0} to HA rerun treatment", vmToRemove.getName());
            }
        }
    }

    private void handOverVM(VM vmToRemove) {
        Guid destinationHostId = vmToRemove.getMigratingToVds();

        // when the destination VDS is NonResponsive put the VM to Uknown like the rest of its VMs, else MigratingTo
        VMStatus newVmStatus =
                (VDSStatus.NonResponsive == getDbFacade().getVdsDao().get(destinationHostId).getStatus())
                        ? VMStatus.Unknown
                        : VMStatus.MigratingTo;

        // handing over the VM to the DST by marking it running on it. it will now be its SRC host.
        vmToRemove.setRunOnVds(destinationHostId);

        log.infoFormat("Handing over VM {0} {1} to Host {2}. Setting VM to status {3}",
                vmToRemove.getName(),
                vmToRemove.getId(),
                destinationHostId,
                newVmStatus);

        // if the DST host goes unresponsive it will take care all MigratingTo and unknown VMs
        ResourceManager.getInstance().InternalSetVmStatus(vmToRemove, newVmStatus);

        // save the VM state
        addVmDynamicToList(vmToRemove.getDynamicData());
        addVmStatisticsToList(vmToRemove.getStatisticsData());
        addVmInterfaceStatisticsToList(vmToRemove.getInterfaces());
    }

    private boolean inMigrationTo(VmDynamic runningVm, VM vmToUpdate) {
        boolean returnValue = false;
        if (runningVm.getStatus() == VMStatus.MigratingTo) {
            // in migration
            log.infoFormat(
                    "RefreshVmList vm id '{0}' is migrating to vds '{1}' ignoring it in the refresh until migration is done",
                    runningVm.getId(),
                    _vds.getName());
            returnValue = true;
        } else if ((vmToUpdate == null && runningVm.getStatus() != VMStatus.MigratingFrom)) {
            // check if the vm exists on another vds
            VmDynamic vmDynamic = getDbFacade().getVmDynamicDao().get(runningVm.getId());
            if (vmDynamic != null && vmDynamic.getRunOnVds() != null
                    && !vmDynamic.getRunOnVds().equals(_vds.getId()) && runningVm.getStatus() != VMStatus.Up) {
                log.infoFormat(
                        "RefreshVmList vm id '{0}' status = {1} on vds {2} ignoring it in the refresh until migration is done",
                        runningVm.getId(),
                        runningVm.getStatus(),
                        _vds.getName());
                returnValue = true;
            }
        }
        return returnValue;
    }

    private void afterMigrationFrom(VmDynamic runningVm, VM vmToUpdate) {
        VMStatus oldVmStatus = vmToUpdate.getStatus();
        VMStatus currentVmStatus = runningVm.getStatus();

        // if the VM's status on source host was MigratingFrom and now the VM is running and its status
        // is not MigratingFrom, it means the migration failed
        if (oldVmStatus == VMStatus.MigratingFrom && currentVmStatus != VMStatus.MigratingFrom
                && currentVmStatus.isRunning()) {
            _vmsToRerun.add(runningVm.getId());
            log.infoFormat("Adding VM {0} to re-run list", runningVm.getId());
            vmToUpdate.setMigratingToVds(null);
        }
    }

    private void refreshCommitedMemory() {
        Integer memCommited = _vds.getGuestOverhead();
        int vmsCoresCount = 0;
        for (VmInternalData runningVm : _runningVms.values()) {
            VmDynamic vmDynamic = runningVm.getVmDynamic();
            // VMs' pending resources are cleared in powering up, so in launch state
            // we shouldn't include them as committed.
            if (vmDynamic.getStatus() != VMStatus.WaitForLaunch &&
                    vmDynamic.getStatus() != VMStatus.Down) {
                VM vm = _vmDict.get(vmDynamic.getId());
                if (vm != null) {
                    memCommited += vm.getVmMemSizeMb();
                    memCommited += _vds.getGuestOverhead();
                    vmsCoresCount += vm.getNumOfCpus();
                }
            }
        }
        if (memCommited == null || !memCommited.equals(_vds.getMemCommited())) {
            _vds.setMemCommited(memCommited);
            _saveVdsDynamic = true;
        }
        if (_vds.getVmsCoresCount() == null || !_vds.getVmsCoresCount().equals(vmsCoresCount)) {
            _vds.setVmsCoresCount(vmsCoresCount);
            _saveVdsDynamic = true;
        }
    }

    private void moveVDSToMaintenanceIfNeeded() {
        if (_vds.getStatus() == VDSStatus.PreparingForMaintenance) {
            if (monitoringStrategy.canMoveToMaintenance(_vds)) {
                _vdsManager.setStatus(VDSStatus.Maintenance, _vds);
                _saveVdsDynamic = true;
                _saveVdsStatistics = true;
                log.infoFormat(
                        "Updated vds status from 'Preparing for Maintenance' to 'Maintenance' in database,  vds = {0} : {1}",
                        _vds.getId(),
                        _vds.getName());
            } else {
                vdsMaintenanceTimeoutOccurred = _vdsManager.isTimeToRetryMaintenance();
            }
        }
    }

    private boolean updateVmRunTimeInfo(RefObject<VM> vmToUpdate, VmDynamic vmNewDynamicData) {
        boolean returnValue = false;
        if (vmToUpdate.argvalue == null) {
            vmToUpdate.argvalue = getDbFacade().getVmDao().get(vmNewDynamicData.getId());
            // if vm exists in db update info
            if (vmToUpdate.argvalue != null) {
                // TODO: This is done to keep consistency with VmDAO.getById(Guid).
                // It should probably be removed, but some research is required.
                vmToUpdate.argvalue.setInterfaces(getDbFacade()
                        .getVmNetworkInterfaceDao()
                        .getAllForVm(vmToUpdate.argvalue.getId()));

                _vmDict.put(vmToUpdate.argvalue.getId(), vmToUpdate.argvalue);
                if (vmNewDynamicData.getStatus() == VMStatus.Up) {
                    if (!_succededToRunVms.contains(vmToUpdate.argvalue.getId())) {
                        _succededToRunVms.add(vmToUpdate.argvalue.getId());
                    }
                }
            }
        }
        if (vmToUpdate.argvalue != null) {
            // check if dynamic data changed - update cache and DB
            List<String> props = ObjectIdentityChecker.GetChangedFields(
                    vmToUpdate.argvalue.getDynamicData(), vmNewDynamicData);
            // dont check fields:
            props.remove("vmHost");
            props.remove("runOnVds");
            props.remove("disks");
            props.remove("bootSequence");
            props.remove("lastVdsRunOn");
            props.remove("hibernationVolHandle");
            props.remove("exitMessage");
            props.remove("lastStartTime");
            props.remove("consoleUserId");
            props.remove("consoleCurrentUserName");
            props.remove("runOnce");
            props.remove("cpuName");
            props.remove("lastStopTime");
            if (vmNewDynamicData.getStatus() != VMStatus.Up) {
                props.remove("appList");
                vmNewDynamicData.setAppList(vmToUpdate.argvalue.getAppList());
            } else if (props.contains("status")
                    && vmToUpdate.argvalue.getDynamicData().getStatus() == VMStatus.PreparingForHibernate) {
                vmNewDynamicData.setStatus(VMStatus.PreparingForHibernate);
                props.remove("status");
            }
            // if anything else changed
            if (props.size() > 0) {
                vmToUpdate.argvalue.updateRunTimeDynamicData(vmNewDynamicData, _vds.getId(), _vds.getName());
                returnValue = true;
            }
        } else {
            // This should only happened when someone run a VM from command
            // line.
            if (Config.<Boolean> getValue(ConfigValues.DebugTimerLogging)) {
                log.info("VDS::UpdateVmRunTimeInfo Error: found VM on a VDS that is not in the database!");
            }
        }

        return returnValue;
    }

    private void updateVmStatistics(VM vmToUpdate) {
        // check if time for vm statistics refresh - update cache and DB
        if (_vdsManager.getRefreshStatistics()) {
            VmStatistics vmStatistics = _runningVms.get(vmToUpdate.getId()).getVmStatistics();
            vmToUpdate.updateRunTimeStatisticsData(vmStatistics, vmToUpdate);
            addVmStatisticsToList(vmToUpdate.getStatisticsData());
            updateInterfaceStatistics(vmToUpdate, vmStatistics);

            _vmDiskImageDynamicToSave.addAll(_runningVms.get(vmToUpdate.getId()).getVmDynamic().getDisks());
        }
    }

    private void updateInterfaceStatistics(VM vm, VmStatistics statistics) {
        if (statistics.getInterfaceStatistics() == null) {
            return;
        }

        if (vm.getInterfaces() == null || vm.getInterfaces().isEmpty()) {
            vm.setInterfaces(getDbFacade().getVmNetworkInterfaceDao().getAllForVm(vm.getId()));
        }
        List<String> macs = new ArrayList<String>();

        vm.setUsageNetworkPercent(0);

        for (VmNetworkInterface ifStats : statistics.getInterfaceStatistics()) {
            boolean firstTime = !macs.contains(ifStats.getMacAddress());

            VmNetworkInterface vmIface = null;
            for (VmNetworkInterface tempIf : vm.getInterfaces()) {
                if (tempIf.getMacAddress().equals(ifStats.getMacAddress())) {
                    vmIface = tempIf;
                    break;
                }
            }
            if (vmIface == null) {
                continue;
            }

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

            if (ifStats.getSpeed() != null && vmIface.getStatistics().getReceiveRate() != null
                    && vmIface.getStatistics().getReceiveRate() > 0) {

                double rx_percent = vmIface.getStatistics().getReceiveRate();
                double tx_percent = vmIface.getStatistics().getTransmitRate();

                vm.setUsageNetworkPercent(Math.max(vm.getUsageNetworkPercent(),
                        (int) Math.max(rx_percent, tx_percent)));
            }

            if (firstTime) {
                macs.add(ifStats.getMacAddress());
            }
        }

        Integer maxPercent = 100;
        vm.setUsageNetworkPercent((vm.getUsageNetworkPercent() > maxPercent) ? maxPercent : vm.getUsageNetworkPercent());
        addVmInterfaceStatisticsToList(vm.getInterfaces());
    }

    /**
     * Add or update vmDynamic to save list
     *
     * @param vmDynamic
     */
    private void addVmDynamicToList(VmDynamic vmDynamic) {
        _vmDynamicToSave.put(vmDynamic.getId(), vmDynamic);
    }

    /**
     * Add or update vmStatistics to save list
     *
     * @param vmStatistics
     */
    private void addVmStatisticsToList(VmStatistics vmStatistics) {
        _vmStatisticsToSave.put(vmStatistics.getId(), vmStatistics);
    }

    private void addVmInterfaceStatisticsToList(List<VmNetworkInterface> list) {
        if (list.size() <= 0) {
            return;
        }
        _vmInterfaceStatisticsToSave.put(list.get(0).getVmId(), list);
    }

    /**
     * Add or update vmDynamic to save list
     *
     * @param vmDevice
     */
    private void addVmDeviceToList(VmDevice vmDevice) {
        vmDeviceToSave.put(vmDevice.getId(), vmDevice);
    }

    private void clearVm(VM vm, VmExitStatus exitStatus, String exitMessage) {
        if (vm.getStatus() != VMStatus.MigratingFrom) {
            // we must check that vm.getStatus() != VMStatus.Down because if it was set to down
            // the exit status and message were set, and we don't want to override them here.
            // we will add it to _vmDynamicToSave though because it might been removed from it in #updateRepository
            if (vm.getStatus() != VMStatus.Suspended && vm.getStatus() != VMStatus.Down) {
                ResourceManager.getInstance().InternalSetVmStatus(vm, VMStatus.Down, exitStatus, exitMessage);
            }
            addVmDynamicToList(vm.getDynamicData());
            addVmStatisticsToList(vm.getStatisticsData());
            addVmInterfaceStatisticsToList(vm.getInterfaces());
            if (!ResourceManager.getInstance().IsVmInAsyncRunningList(vm.getId())) {
                _vmsMovedToDown.add(vm.getId());
            }
        }
    }

    /**
     * An access method for test usages
     *
     * @return The devices to be added to the database
     */
    protected List<VmDevice> getNewVmDevices() {
        return Collections.unmodifiableList(newVmDevices);
    }

    /**
     * An access method for test usages
     *
     * @return The devices to be removed from the database
     */
    protected List<VmDeviceId> getRemovedVmDevices() {
        return Collections.unmodifiableList(removedDeviceIds);
    }

    /**
     * An access method for test usages
     *
     * @return The LUNs to update in DB
     */
    protected List<LUNs> getVmLunDisksToSave() {
        return Collections.unmodifiableList(vmLunDisksToSave);
    }

    protected List<VmDynamic> getPoweringUpVms() {
        return _poweringUpVms;
    }

    protected Map<Guid, VmInternalData> getRunningVms() {
        return _runningVms;
    }

    protected void auditLog(AuditLogableBase auditLogable, AuditLogType logType) {
        AuditLogDirector.log(auditLogable, logType);
    }

    public DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    private static void logVmStatusTransionFromUnknown(VM vmToUpdate, VmDynamic runningVm) {
        final AuditLogableBase auditLogable = new AuditLogableBase();
        auditLogable.setVmId(vmToUpdate.getId());
        auditLogable.addCustomValue("VmStatus", runningVm.getStatus().toString());
        AuditLogDirector.log(auditLogable, AuditLogType.VM_STATUS_RESTORED);
    }

    protected ResourceManager getResourceManager() {
        return ResourceManager.getInstance();
    }
}

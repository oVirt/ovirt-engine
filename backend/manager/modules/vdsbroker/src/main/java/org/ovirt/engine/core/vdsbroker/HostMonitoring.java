package org.ovirt.engine.core.vdsbroker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.CpuStatistics;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.IVdsEventListener;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkStatistics;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
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
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSErrorException;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSNetworkException;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSRecoveringException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "synthetic-access", "unchecked", "rawtypes" })
public class HostMonitoring {
    private boolean _saveVdsDynamic;
    private VDSStatus _firstStatus = VDSStatus.forValue(0);
    private boolean _saveVdsStatistics;
    private final VdsManager _vdsManager;
    private final MonitoringStrategy monitoringStrategy;
    private final VDS _vds;

    private boolean processHardwareCapsNeeded;
    private boolean refreshedCapabilities = false;
    private VmsMonitoring vmsMonitoring;
    private static Map<Guid, Long> hostDownTimes = new HashMap<>();
    private boolean vdsMaintenanceTimeoutOccurred;
    private Map<String, InterfaceStatus> oldInterfaceStatus = new HashMap<String, InterfaceStatus>();

    private static final Logger log = LoggerFactory.getLogger(HostMonitoring.class);

    public HostMonitoring(VdsManager vdsManager, VDS vds, MonitoringStrategy monitoringStrategy) {
        _vdsManager = vdsManager;
        _vds = vds;
        _firstStatus = _vds.getStatus();
        this.monitoringStrategy = monitoringStrategy;
        vmsMonitoring = new VmsMonitoring(vdsManager, vds);
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
                    log.debug("vds '{}' ({}) firing up event.", _vds.getName(), _vds.getId());
                    _vdsManager.setIsSetNonOperationalExecuted(!getVdsEventListener().vdsUpEvent(_vds));
                }
                // save all data to db
                saveDataToDb();
            } catch (IRSErrorException ex) {
                logFailureMessage("ResourceManager::refreshVdsRunTimeInfo:", ex);
                log.debug("Exception", ex);
            } catch (RuntimeException ex) {
                logFailureMessage("ResourceManager::refreshVdsRunTimeInfo:", ex);
                log.error("Exception", ex);
            }
        }
    }

    public void refreshVdsRunTimeInfo() {
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
            vmsMonitoring.refreshVmStats();
        } catch (VDSRecoveringException e) {
            // if PreparingForMaintenance and vds is in install failed keep to
            // move vds to maintenance
            if (_vds.getStatus() != VDSStatus.PreparingForMaintenance) {
                throw e;
            }
        } catch (ClassCastException cce) {
            // This should occur only if the vdsm API is not the same as the cluster API (version mismatch)
            log.error("Failure to refresh Vds '{}' runtime info. Incorrect vdsm version for cluster '{}': {}",
                    _vds.getName(),
                    _vds.getVdsGroupName(), cce.getMessage());
            log.debug("Exception", cce);
            if (_vds.getStatus() != VDSStatus.PreparingForMaintenance && _vds.getStatus() != VDSStatus.Maintenance) {
                ResourceManager.getInstance().runVdsCommand(VDSCommandType.SetVdsStatus,
                        new SetVdsStatusVDSCommandParameters(_vds.getId(), VDSStatus.Error));
            }
        } catch (Throwable t) {
            log.error("Failure to refresh Vds runtime info: {}", t.getMessage());
            log.error("Exception", t);
            throw t;
        }
        moveVDSToMaintenanceIfNeeded();
    }

    private void saveDataToDb() {
        if (_saveVdsDynamic) {
            _vdsManager.updateDynamicData(_vds.getDynamicData());
            if (refreshedCapabilities) {
                _vdsManager.updateNumaData(_vds);
            }
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
            saveCpuStatisticsDataToDb();
            saveNumaStatisticsDataToDb();
        }

        vmsMonitoring.saveVmsToDb();
    }

    private void saveCpuStatisticsDataToDb() {
        final List<CpuStatistics> cpuStatisticsToSave = new ArrayList<>();

        cpuStatisticsToSave.addAll(_vds.getStatisticsData().getCpuCoreStatistics());
        if (!cpuStatisticsToSave.isEmpty()) {
            List<CpuStatistics> dbCpuStats = getDbFacade().getVdsCpuStatisticsDAO()
                    .getAllCpuStatisticsByVdsId(_vds.getId());
            if (dbCpuStats.isEmpty()) {
                TransactionSupport.executeInScope(TransactionScopeOption.Required,
                        new TransactionMethod<Void>() {
                            @Override
                            public Void runInTransaction() {
                                getDbFacade().getVdsCpuStatisticsDAO().massSaveCpuStatistics(
                                        cpuStatisticsToSave, _vds.getId());
                                return null;
                            }
                        });
            }
            else {
                boolean needRemoveAndSave = isRemvoeAndSaveVdsCpuStatsNeeded(cpuStatisticsToSave, dbCpuStats);
                if (needRemoveAndSave) {
                    TransactionSupport.executeInScope(TransactionScopeOption.Required,
                            new TransactionMethod<Void>() {
                                @Override
                                public Void runInTransaction() {
                                    getDbFacade().getVdsCpuStatisticsDAO().removeAllCpuStatisticsByVdsId(_vds.getId());
                                    getDbFacade().getVdsCpuStatisticsDAO().massSaveCpuStatistics(
                                            cpuStatisticsToSave, _vds.getId());
                                    return null;
                                }
                            });
                }
                else {
                    TransactionSupport.executeInScope(TransactionScopeOption.Required,
                            new TransactionMethod<Void>() {
                                @Override
                                public Void runInTransaction() {
                                    getDbFacade().getVdsCpuStatisticsDAO().massUpdateCpuStatistics(
                                            cpuStatisticsToSave, _vds.getId());
                                    return null;
                                }
                            });
                }
            }
        }
    }

    private boolean isRemvoeAndSaveVdsCpuStatsNeeded(final List<CpuStatistics> cpuStatisticsToSave,
            List<CpuStatistics> dbCpuStats) {
        boolean needRemoveAndSave = false;
        if (dbCpuStats.size() != cpuStatisticsToSave.size()) {
            needRemoveAndSave = true;
        }
        else {
            HashSet<Integer> vdsCpuStats = new HashSet<>();
            for (CpuStatistics cpuStat : dbCpuStats) {
                vdsCpuStats.add(cpuStat.getCpuId());
            }
            for (CpuStatistics cpuStat : cpuStatisticsToSave) {
                if (!vdsCpuStats.contains(cpuStat.getCpuId())) {
                    needRemoveAndSave = true;
                    break;
                }
            }
        }
        return needRemoveAndSave;
    }

    private void saveNumaStatisticsDataToDb() {
        final List<VdsNumaNode> vdsNumaNodesToSave = new ArrayList<>();
        List<VdsNumaNode> updateNumaNodes = _vds.getNumaNodeList();
        if (!updateNumaNodes.isEmpty()) {
            List<VdsNumaNode> dbVdsNumaNodes = getDbFacade().getVdsNumaNodeDAO()
                    .getAllVdsNumaNodeByVdsId(_vds.getId());
            Map<Integer, VdsNumaNode> nodesMap = new HashMap<>();
            for (VdsNumaNode node : dbVdsNumaNodes) {
                nodesMap.put(node.getIndex(), node);
            }
            for (VdsNumaNode node : updateNumaNodes) {
                if (nodesMap.containsKey(node.getIndex())) {
                    node.setId(nodesMap.get(node.getIndex()).getId());
                    if (node.getNumaNodeStatistics() != null) {
                        vdsNumaNodesToSave.add(node);
                    }
                }
            }
        }
        if (!vdsNumaNodesToSave.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required,
                    new TransactionMethod<Void>() {
                        @Override
                        public Void runInTransaction() {
                            getDbFacade().getVdsNumaNodeDAO().massUpdateNumaNodeStatistics(vdsNumaNodesToSave);
                            return null;
                        }
                    });
        }
    }

    /**
     * check if value is less than configurable threshold , if yes , generated event list message
     *
     * @param stat
     */
    private void checkVdsMemoryThreshold(VdsStatistics stat) {

        Integer minAvailableThreshold = Config.getValue(ConfigValues.LogPhysicalMemoryThresholdInMB);
        Integer maxUsedPercentageThreshold =
                Config.getValue(ConfigValues.LogMaxPhysicalMemoryUsedThresholdInPercentage);

        if (stat.getMemFree() == null || stat.getUsageMemPercent() == null) {
            return;
        }

        AuditLogType valueToLog = stat.getMemFree() < minAvailableThreshold ?
                AuditLogType.VDS_LOW_MEM :
                AuditLogType.VDS_HIGH_MEM_USE;

        if ((stat.getMemFree() < minAvailableThreshold && Version.v3_2.compareTo(_vds.getVersion()) <= 0)
                || stat.getUsageMemPercent() > maxUsedPercentageThreshold) {
            AuditLogableBase logable = new AuditLogableBase(stat.getId());
            logable.addCustomValue("HostName", _vds.getName());
            logable.addCustomValue("AvailableMemory", stat.getMemFree().toString());
            logable.addCustomValue("UsedMemory", stat.getUsageMemPercent().toString());
            logable.addCustomValue("Threshold", stat.getMemFree() < minAvailableThreshold ?
                    minAvailableThreshold.toString() :
                    maxUsedPercentageThreshold.toString());
            auditLog(logable, valueToLog);
        }
    }

    /**
     * check if value is less than configurable threshold , if yes , generated event list message
     *
     * @param stat
     */
    private void checkVdsCpuThreshold(VdsStatistics stat) {

        Integer maxUsedPercentageThreshold = Config.getValue(ConfigValues.LogMaxCpuUsedThresholdInPercentage);
        if (stat.getUsageCpuPercent() != null
                && stat.getUsageCpuPercent() > maxUsedPercentageThreshold) {
            AuditLogableBase logable = new AuditLogableBase(stat.getId());
            logable.addCustomValue("HostName", _vds.getName());
            logable.addCustomValue("UsedCpu", stat.getUsageCpuPercent().toString());
            logable.addCustomValue("Threshold", maxUsedPercentageThreshold.toString());
            auditLog(logable, AuditLogType.VDS_HIGH_CPU_USE);
        }
    }

    /**
     * check if value is less than configurable threshold , if yes , generated event list message
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
     * check if value is less than configurable threshold , if yes , generated event list message
     *
     * @param stat
     */
    private void checkVdsSwapThreshold(VdsStatistics stat) {

        final double THRESHOLD = 0.98;
        Integer minAvailableThreshold = Config.getValue(ConfigValues.LogPhysicalMemoryThresholdInMB);
        Integer maxUsedPercentageThreshold =
                Config.getValue(ConfigValues.LogMaxPhysicalMemoryUsedThresholdInPercentage);

        if (stat.getSwapTotal() == null || stat.getSwapFree() == null || stat.getSwapTotal() == 0) {
            return;
        }

        Long swapUsedPercent = (stat.getSwapTotal() - stat.getSwapFree()) / stat.getSwapTotal();

        // Allow the space to be up to 2% lower than as defined in configuration
        Long allowedMinAvailableThreshold = Math.round(minAvailableThreshold.doubleValue() * THRESHOLD);
        AuditLogType valueToLog = stat.getSwapFree() <  allowedMinAvailableThreshold ?
                AuditLogType.VDS_LOW_SWAP :
                AuditLogType.VDS_HIGH_SWAP_USE;

        if (stat.getSwapFree() < allowedMinAvailableThreshold || swapUsedPercent > maxUsedPercentageThreshold) {
            AuditLogableBase logable = new AuditLogableBase(stat.getId());
            logable.addCustomValue("HostName", _vds.getName());
            logable.addCustomValue("UsedSwap", swapUsedPercent.toString());
            logable.addCustomValue("AvailableSwapMemory", stat.getSwapFree().toString());
            logable.addCustomValue("Threshold", stat.getSwapFree() < allowedMinAvailableThreshold ?
                    minAvailableThreshold.toString() : maxUsedPercentageThreshold.toString());
            auditLog(logable, valueToLog);
        }
    }

    private void logFailureMessage(String messagePrefix, RuntimeException ex) {
        log.error("{} vds={}({}): {}",
                messagePrefix,
                _vds.getName(),
                _vds.getId(),
                ex.getMessage());
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
                    log.error("Host encounter a problem moving to maintenance mode, probably error during " +
                        "disconnecting it from pool. The Host will stay in Maintenance: {}",
                            ex.getMessage());
                    log.debug("Exception", ex);
                }
            } else if (_vds.getStatus() == VDSStatus.NonOperational && _firstStatus != VDSStatus.NonOperational) {

                if (!_vdsManager.isSetNonOperationalExecuted()) {
                    getVdsEventListener().vdsNonOperational(_vds.getId(), _vds.getNonOperationalReason(), true, Guid.Empty);
                } else {
                    log.info("Host '{}'({}) is already in NonOperational status for reason '{}'. SetNonOperationalVds command is skipped.",
                            _vds.getName(),
                            _vds.getId(),
                            (_vds.getNonOperationalReason() != null) ? _vds.getNonOperationalReason().name()
                                    : "unknown");
                }
            }
            vmsMonitoring.afterVMsRefreshTreatment();
        } catch (IRSErrorException ex) {
            logFailureMessage("Could not finish afterRefreshTreatment", ex);
            log.debug("Exception", ex);
        } catch (RuntimeException ex) {
            logFailureMessage("Could not finish afterRefreshTreatment", ex);
            log.error("Exception", ex);
        }
    }

    private void handleVdsMaintenanceTimeout() {
        getVdsEventListener().handleVdsMaintenanceTimeout(_vds.getId());
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

    public void refreshVdsStats() {
        if (Config.<Boolean> getValue(ConfigValues.DebugTimerLogging)) {
            log.debug("vdsManager::refreshVdsStats entered, vds='{}'({})",
                    _vds.getName(), _vds.getId());
        }
        // get statistics data, images checks and vm_count data (dynamic)
        fetchHostInterfaces();
        VDSReturnValue statsReturnValue = getResourceManager().runVdsCommand(VDSCommandType.GetStats,
                new VdsIdAndVdsVDSCommandParametersBase(_vds));
        getVdsEventListener().updateSchedulingStats(_vds);
        if (!statsReturnValue.getSucceeded()
                && statsReturnValue.getExceptionObject() != null) {
            VDSNetworkException ex =
                    (VDSNetworkException) ((statsReturnValue.getExceptionObject() instanceof VDSNetworkException)
                            ? statsReturnValue.getExceptionObject()
                            : null);
            if (ex != null) {
                if (_vdsManager.handleNetworkException(ex, _vds)) {
                    _saveVdsDynamic = true;
                }
                log.error("vds::refreshVdsStats Failed getVdsStats,  vds='{}'({}): {}",
                        _vds.getName(), _vds.getId(), ex.getMessage());
            } else {
                log.error("vds::refreshVdsStats Failed getVdsStats,  vds='{}'({}): {}",
                        _vds.getName(), _vds.getId(), statsReturnValue.getExceptionString());
            }
            throw statsReturnValue.getExceptionObject();
        }
        // save also dynamic because vm_count data and image_check getting with
        // statistics data
        // TODO: omer- one day remove dynamic save when possible please check if vdsDynamic changed before save
        _saveVdsDynamic = true;
        _saveVdsStatistics = true;

        alertIfLowDiskSpaceOnHost();
        checkVdsInterfaces();

        if (Config.<Boolean> getValue(ConfigValues.DebugTimerLogging)) {
            log.debug("vds::refreshVdsStats\n{0}", this);
        }
    }

    private void fetchHostInterfaces() {
        List<VdsNetworkInterface> nics;
        if (_vds.getInterfaces().isEmpty()) {
             nics = getDbFacade().getInterfaceDao().getAllInterfacesForVds(_vds.getId());
            _vds.getInterfaces().addAll(nics);
        } else {
            nics = _vds.getInterfaces();
        }

        // cache previous state of interfaces for comparison with those reported by host
        oldInterfaceStatus.clear();
        for (VdsNetworkInterface nic : nics) {
            oldInterfaceStatus.put(nic.getName(), nic.getStatistics().getStatus());
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

        Map<String, Set<String>> problematicNicsWithNetworks = new HashMap<String, Set<String>>();
        try {
            reportNicStatusChanges();
            problematicNicsWithNetworks = NetworkMonitoringHelper.determineProblematicNics(_vds.getInterfaces(),
                    getDbFacade().getNetworkDao().getAllForCluster(_vds.getVdsGroupId()));
        } catch (Exception e) {
            log.error("Failure on checkInterfaces on update runtimeinfo for vds: '{}': {}", _vds.getName(), e.getMessage());
            log.error("Exception", e);
        } finally {
            if (!problematicNicsWithNetworks.isEmpty()) {
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
                    String problematicNicsWithNetworksString =
                            constructNicsWithNetworksString(problematicNicsWithNetworks);

                    _vds.setNonOperationalReason(NonOperationalReason.NETWORK_INTERFACE_IS_DOWN);
                    _vdsManager.setStatus(VDSStatus.NonOperational, _vds);
                    log.info("Host '{}' moved to Non-Operational state because interface/s which are down are needed by required network/s in the current cluster: '{}'",
                            _vds.getName(),
                            problematicNicsWithNetworksString);

                    AuditLogableBase logable = new AuditLogableBase(_vds.getId());
                    logable.addCustomValue("NicsWithNetworks", problematicNicsWithNetworksString);
                    logable.setCustomId(problematicNicsWithNetworksString);
                    auditLog(logable, AuditLogType.VDS_SET_NONOPERATIONAL_IFACE_DOWN);
                } catch (Exception e) {
                    log.error("checkInterface: Failure on moving host: '{}' to non-operational: {}",
                            _vds.getName(), e.getMessage());
                    log.error("Exception", e);
                }
            } else {
                // no nics are down, remove from list if exists
                hostDownTimes.remove(_vds.getId());
            }
        }
    }

    private String constructNicsWithNetworksString(Map<String, Set<String>> nicsWithNetworks) {
        List<String> reportedNics = new ArrayList<>(nicsWithNetworks.size());
        for (Entry<String, Set<String>> nicToNetworks : nicsWithNetworks.entrySet()) {
            reportedNics.add(String.format("%s (%s)",
                    nicToNetworks.getKey(),
                    StringUtils.join(nicToNetworks.getValue(), ", ")));
        }

        return StringUtils.join(reportedNics, ", ");
    }

    private void reportNicStatusChanges() {
        List<VdsNetworkInterface> interfaces = _vds.getInterfaces();
        Set<VdsNetworkInterface> slaves = new HashSet<>();
        Map<String, VdsNetworkInterface> monitoredInterfaces = new HashMap<String, VdsNetworkInterface>();
        Map<String, VdsNetworkInterface> interfaceByName = Entities.entitiesByName(interfaces);

        for (VdsNetworkInterface iface : interfaces) {
            if (iface.getBondName() != null) {
                slaves.add(iface);
            }

            String baseIfaceName = NetworkUtils.stripVlan(iface);

            // If the parent interface already marked as monitored- no need to check it again
            if (monitoredInterfaces.containsKey(baseIfaceName)) {
                continue;
            }

            // The status of the interface should be monitored only if it has networks attached to it or has labels
            if (StringUtils.isNotEmpty(iface.getNetworkName()) || NetworkUtils.isLabeled(iface)) {
                VdsNetworkInterface baseIface = iface;
                // If vlan find the parent interface
                if (iface.getVlanId() != null) {
                    baseIface = interfaceByName.get(baseIfaceName);
                }

                monitoredInterfaces.put(baseIfaceName, baseIface);
            }
        }

        // Slaves should be monitored if the bond is monitored
        for (VdsNetworkInterface slave : slaves) {
            if (monitoredInterfaces.containsKey(slave.getBondName())) {
                monitoredInterfaces.put(slave.getName(), slave);
            }
        }

        for (Map.Entry<String, InterfaceStatus> entry : oldInterfaceStatus.entrySet()) {
            VdsNetworkInterface iface = monitoredInterfaces.get(entry.getKey());
            InterfaceStatus oldStatus = entry.getValue();
            InterfaceStatus status;
            if (iface != null) {
                status = iface.getStatistics().getStatus();
                if (oldStatus != InterfaceStatus.NONE
                        && oldStatus != status) {
                    AuditLogableBase logable = new AuditLogableBase(_vds.getId());
                    logable.setCustomId(iface.getName());
                    if (iface.getBondName() != null) {
                        logable.addCustomValue("SlaveName", iface.getName());
                        logable.addCustomValue("BondName", iface.getBondName());
                        auditLog(logable, status == InterfaceStatus.UP ? AuditLogType.HOST_BOND_SLAVE_STATE_UP
                                : AuditLogType.HOST_BOND_SLAVE_STATE_DOWN);
                    } else {
                        logable.addCustomValue("InterfaceName", iface.getName());
                        auditLog(logable, status == InterfaceStatus.UP ? AuditLogType.HOST_INTERFACE_STATE_UP
                                : AuditLogType.HOST_INTERFACE_STATE_DOWN);
                    }
                }
            }
        }
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
        // show status UP in audit only when InitVdsOnUpCommand finished successfully
        if (_vds.getStatus() != VDSStatus.Up) {
            AuditLogableBase logable = new AuditLogableBase(_vds.getId());
            logable.addCustomValue("HostStatus", _vds.getStatus().toString());
            auditLog(logable, AuditLogType.VDS_DETECTED);
        }
    }

    private void moveVDSToMaintenanceIfNeeded() {
        if (_vds.getStatus() == VDSStatus.PreparingForMaintenance) {
            if (monitoringStrategy.canMoveToMaintenance(_vds)) {
                _vdsManager.setStatus(VDSStatus.Maintenance, _vds);
                _saveVdsDynamic = true;
                _saveVdsStatistics = true;
                log.info(
                        "Updated vds status from 'Preparing for Maintenance' to 'Maintenance' in database,  vds '{}'({})",
                        _vds.getName(),
                        _vds.getId());
            } else {
                vdsMaintenanceTimeoutOccurred = _vdsManager.isTimeToRetryMaintenance();
            }
        }
    }

    protected void auditLog(AuditLogableBase auditLogable, AuditLogType logType) {
        AuditLogDirector.log(auditLogable, logType);
    }

    public DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    public ResourceManager getResourceManager() {
        return ResourceManager.getInstance();
    }
}

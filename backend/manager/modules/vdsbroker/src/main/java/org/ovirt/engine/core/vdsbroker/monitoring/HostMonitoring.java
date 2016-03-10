package org.ovirt.engine.core.vdsbroker.monitoring;

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
import org.ovirt.engine.core.common.businessentities.V2VJobInfo;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkStatistics;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSErrorException;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSRecoveringException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostMonitoring {
    private final VDS vds;
    private final VdsManager vdsManager;
    private VDSStatus firstStatus = VDSStatus.forValue(0);
    private final MonitoringStrategy monitoringStrategy;
    private boolean saveVdsDynamic;
    private boolean saveVdsStatistics;
    private boolean processHardwareCapsNeeded;
    private boolean refreshedCapabilities = false;
    private static Map<Guid, Long> hostDownTimes = new HashMap<>();
    private boolean vdsMaintenanceTimeoutOccurred;
    private Map<String, InterfaceStatus> oldInterfaceStatus = new HashMap<>();
    private final ResourceManager resourceManager;
    private final DbFacade dbFacade;
    private final AuditLogDirector auditLogDirector;
    private static final Logger log = LoggerFactory.getLogger(HostMonitoring.class);

    public HostMonitoring(VdsManager vdsManager,
            VDS vds,
            MonitoringStrategy monitoringStrategy,
            ResourceManager resourceManager,
            DbFacade dbFacade,
            AuditLogDirector auditLogDirector) {
        this.vdsManager = vdsManager;
        this.vds = vds;
        firstStatus = vds.getStatus();
        this.monitoringStrategy = monitoringStrategy;
        this.resourceManager = resourceManager;
        this.dbFacade = dbFacade;
        this.auditLogDirector = auditLogDirector;
    }

    public void refresh() {
        try {
            refreshVdsRunTimeInfo();
        } finally {
            try {
                if (firstStatus != vds.getStatus() && vds.getStatus() == VDSStatus.Up) {
                    // use this lock in order to allow only one host updating DB and
                    // calling UpEvent in a time
                    vdsManager.cancelRecoveryJob();
                    log.debug("vds '{}' ({}) firing up event.", vds.getName(), vds.getId());
                    vdsManager.setIsSetNonOperationalExecuted(!getVdsEventListener().vdsUpEvent(vds));
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
        boolean isVdsUpOrGoingToMaintenance = vds.getStatus() == VDSStatus.Up
                || vds.getStatus() == VDSStatus.PreparingForMaintenance || vds.getStatus() == VDSStatus.Error
                || vds.getStatus() == VDSStatus.NonOperational;
        try {
            if (isVdsUpOrGoingToMaintenance) {
                // check if its time for statistics refresh
                if (vdsManager.isTimeToRefreshStatistics() || vds.getStatus() == VDSStatus.PreparingForMaintenance) {
                    refreshVdsStats();
                }
            } else {
                // refresh dynamic data
                final AtomicBoolean processHardwareNeededAtomic = new AtomicBoolean();
                VDSStatus refreshReturnStatus =
                        vdsManager.refreshCapabilities(processHardwareNeededAtomic, vds);
                processHardwareCapsNeeded = processHardwareNeededAtomic.get();
                refreshedCapabilities = true;
                if (refreshReturnStatus != VDSStatus.NonOperational) {
                    vdsManager.setStatus(VDSStatus.Up, vds);
                }
                saveVdsDynamic = true;
            }
            beforeFirstRefreshTreatment(isVdsUpOrGoingToMaintenance);
            if (vdsManager.isTimeToRefreshStatistics()) {
                saveVdsDynamic |= refreshCommitedMemory(vds, vdsManager.getLastVmsList());
            }
        } catch (VDSRecoveringException e) {
            // if PreparingForMaintenance and vds is in install failed keep to
            // move vds to maintenance
            if (vds.getStatus() != VDSStatus.PreparingForMaintenance) {
                throw e;
            }
        } catch (ClassCastException cce) {
            // This should occur only if the vdsm API is not the same as the cluster API (version mismatch)
            log.error("Failure to refresh Vds '{}' runtime info. Incorrect vdsm version for cluster '{}': {}",
                    vds.getName(),
                    vds.getClusterName(), cce.getMessage());
            log.debug("Exception", cce);
            if (vds.getStatus() != VDSStatus.PreparingForMaintenance && vds.getStatus() != VDSStatus.Maintenance) {
                resourceManager.runVdsCommand(VDSCommandType.SetVdsStatus,
                        new SetVdsStatusVDSCommandParameters(vds.getId(), VDSStatus.Error));
            }
        } catch (Throwable t) {
            log.error("Failure to refresh Vds runtime info: {}", t.getMessage());
            log.error("Exception", t);
            throw t;
        }
        moveVDSToMaintenanceIfNeeded();
    }

    private void saveDataToDb() {
        if (saveVdsDynamic) {
            vdsManager.updateDynamicData(vds.getDynamicData());
            if (refreshedCapabilities) {
                vdsManager.updateNumaData(vds);
            }
        }

        if (saveVdsStatistics) {
            VdsStatistics stat = vds.getStatisticsData();
            vdsManager.updateStatisticsData(stat);
            checkVdsMemoryThreshold(stat);
            checkVdsCpuThreshold(stat);
            checkVdsNetworkThreshold(stat);
            checkVdsSwapThreshold(stat);

            final List<VdsNetworkStatistics> statistics = new LinkedList<>();
            for (VdsNetworkInterface iface : vds.getInterfaces()) {
                statistics.add(iface.getStatistics());
            }
            if (!statistics.isEmpty()) {
                TransactionSupport.executeInScope(TransactionScopeOption.Required,
                        () -> {
                            getDbFacade().getInterfaceDao().massUpdateStatisticsForVds(statistics);
                            return null;
                        });
            }
            saveCpuStatisticsDataToDb();
            saveNumaStatisticsDataToDb();
        }
    }

    private void saveCpuStatisticsDataToDb() {
        final List<CpuStatistics> cpuStatisticsToSave = new ArrayList<>();

        cpuStatisticsToSave.addAll(vds.getStatisticsData().getCpuCoreStatistics());
        if (!cpuStatisticsToSave.isEmpty()) {
            List<CpuStatistics> dbCpuStats = getDbFacade().getVdsCpuStatisticsDao()
                    .getAllCpuStatisticsByVdsId(vds.getId());
            if (dbCpuStats.isEmpty()) {
                TransactionSupport.executeInScope(TransactionScopeOption.Required,
                        () -> {
                            getDbFacade().getVdsCpuStatisticsDao().massSaveCpuStatistics(
                                    cpuStatisticsToSave, vds.getId());
                            return null;
                        });
            }
            else {
                boolean needRemoveAndSave = isRemvoeAndSaveVdsCpuStatsNeeded(cpuStatisticsToSave, dbCpuStats);
                if (needRemoveAndSave) {
                    TransactionSupport.executeInScope(TransactionScopeOption.Required,
                            () -> {
                                getDbFacade().getVdsCpuStatisticsDao().removeAllCpuStatisticsByVdsId(vds.getId());
                                getDbFacade().getVdsCpuStatisticsDao().massSaveCpuStatistics(
                                        cpuStatisticsToSave, vds.getId());
                                return null;
                            });
                }
                else {
                    TransactionSupport.executeInScope(TransactionScopeOption.Required,
                            () -> {
                                getDbFacade().getVdsCpuStatisticsDao().massUpdateCpuStatistics(
                                        cpuStatisticsToSave, vds.getId());
                                return null;
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
        List<VdsNumaNode> updateNumaNodes = vds.getNumaNodeList();
        if (!updateNumaNodes.isEmpty()) {
            List<VdsNumaNode> dbVdsNumaNodes = getDbFacade().getVdsNumaNodeDao()
                    .getAllVdsNumaNodeByVdsId(vds.getId());
            Map<Integer, VdsNumaNode> nodesMap = new HashMap<>();
            for (VdsNumaNode node : dbVdsNumaNodes) {
                nodesMap.put(node.getIndex(), node);
            }
            for (VdsNumaNode node : updateNumaNodes) {
                VdsNumaNode dbNode = nodesMap.get(node.getIndex());
                if (dbNode != null) {
                    if (node.getNumaNodeStatistics() != null) {
                        dbNode.setNumaNodeStatistics(node.getNumaNodeStatistics());
                        vdsNumaNodesToSave.add(dbNode);
                    }
                }
            }
        }
        if (!vdsNumaNodesToSave.isEmpty()) {
            getDbFacade().getVdsNumaNodeDao().massUpdateNumaNodeStatistics(vdsNumaNodesToSave);
        }
    }

    /**
     * check if value is less than configurable threshold , if yes , generated event list message
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

        if (stat.getMemFree() < minAvailableThreshold || stat.getUsageMemPercent() > maxUsedPercentageThreshold) {
            AuditLogableBase logable = new AuditLogableBase(stat.getId());
            logable.addCustomValue("HostName", vds.getName());
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
     */
    private void checkVdsCpuThreshold(VdsStatistics stat) {

        Integer maxUsedPercentageThreshold = Config.getValue(ConfigValues.LogMaxCpuUsedThresholdInPercentage);
        if (stat.getUsageCpuPercent() != null
                && stat.getUsageCpuPercent() > maxUsedPercentageThreshold) {
            AuditLogableBase logable = new AuditLogableBase(stat.getId());
            logable.addCustomValue("HostName", vds.getName());
            logable.addCustomValue("UsedCpu", stat.getUsageCpuPercent().toString());
            logable.addCustomValue("Threshold", maxUsedPercentageThreshold.toString());
            auditLog(logable, AuditLogType.VDS_HIGH_CPU_USE);
        }
    }

    /**
     * check if value is less than configurable threshold , if yes , generated event list message
     */
    private void checkVdsNetworkThreshold(VdsStatistics stat) {
        Integer maxUsedPercentageThreshold = Config.getValue(ConfigValues.LogMaxNetworkUsedThresholdInPercentage);
        for (VdsNetworkInterface iface : vds.getInterfaces()) {
            Double transmitRate = iface.getStatistics().getTransmitRate();
            Double receiveRate = iface.getStatistics().getReceiveRate();
            if ((transmitRate != null && iface.getStatistics().getTransmitRate().intValue() > maxUsedPercentageThreshold)
                    || (receiveRate != null && iface.getStatistics().getReceiveRate().intValue() > maxUsedPercentageThreshold)) {
                AuditLogableBase logable = new AuditLogableBase(vds.getId());
                logable.setCustomId(iface.getName());
                logable.addCustomValue("HostName", vds.getName());
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
     */
    private void checkVdsSwapThreshold(VdsStatistics stat) {

        final double THRESHOLD = 0.98;
        Integer minAvailableThreshold = Config.getValue(ConfigValues.LogSwapMemoryThresholdInMB);
        Integer maxUsedPercentageThreshold =
                Config.getValue(ConfigValues.LogMaxSwapMemoryUsedThresholdInPercentage);

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
            logable.addCustomValue("HostName", vds.getName());
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
                vds.getName(),
                vds.getId(),
                ex.getMessage());
    }

    protected IVdsEventListener getVdsEventListener() {
        return resourceManager.getEventListener();
    }

    public void afterRefreshTreatment() {
        try {
            if (processHardwareCapsNeeded) {
                monitoringStrategy.processHardwareCapabilities(vds);
                markIsSetNonOperationalExecuted();
            }

            if (refreshedCapabilities) {
                getVdsEventListener().handleVdsVersion(vds.getId());
                markIsSetNonOperationalExecuted();
            }

            if (vdsMaintenanceTimeoutOccurred) {
                handleVdsMaintenanceTimeout();
            }

            if (vds.getStatus() == VDSStatus.Maintenance) {
                try {
                    getVdsEventListener().vdsMovedToMaintenance(vds);
                } catch (RuntimeException ex) {
                    log.error("Host encounter a problem moving to maintenance mode, probably error during " +
                        "disconnecting it from pool. The Host will stay in Maintenance: {}",
                            ex.getMessage());
                    log.debug("Exception", ex);
                }
            } else if (vds.getStatus() == VDSStatus.NonOperational && firstStatus != VDSStatus.NonOperational) {

                if (!vdsManager.isSetNonOperationalExecuted()) {
                    getVdsEventListener().vdsNonOperational(vds.getId(), vds.getNonOperationalReason(), true, Guid.Empty);
                } else {
                    log.info("Host '{}'({}) is already in NonOperational status for reason '{}'. SetNonOperationalVds command is skipped.",
                            vds.getName(),
                            vds.getId(),
                            (vds.getNonOperationalReason() != null) ? vds.getNonOperationalReason().name() : "unknown");
                }
            }
        } catch (IRSErrorException ex) {
            logFailureMessage("Could not finish afterRefreshTreatment", ex);
            log.debug("Exception", ex);
        } catch (RuntimeException ex) {
            logFailureMessage("Could not finish afterRefreshTreatment", ex);
            log.error("Exception", ex);
        }
    }

    private void handleVdsMaintenanceTimeout() {
        getVdsEventListener().handleVdsMaintenanceTimeout(vds.getId());
        vdsManager.calculateNextMaintenanceAttemptTime();
    }

    private void markIsSetNonOperationalExecuted() {
        if (!vdsManager.isSetNonOperationalExecuted()) {
            VdsDynamic vdsDynamic = getDbFacade().getVdsDynamicDao().get(vds.getId());
            if (vdsDynamic.getStatus() == VDSStatus.NonOperational) {
                vdsManager.setIsSetNonOperationalExecuted(true);
            }
        }
    }

    public void refreshVdsStats() {
        if (Config.<Boolean> getValue(ConfigValues.DebugTimerLogging)) {
            log.debug("vdsManager::refreshVdsStats entered, vds='{}'({})",
                    vds.getName(), vds.getId());
        }
        // get statistics data, images checks and vm_count data (dynamic)
        fetchHostInterfaces();
        VDSReturnValue statsReturnValue = resourceManager.runVdsCommand(VDSCommandType.GetStats,
                new VdsIdAndVdsVDSCommandParametersBase(vds));
        if (!statsReturnValue.getSucceeded()
                && statsReturnValue.getExceptionObject() != null) {
            log.error(" Failed getting vds stats,  vds='{}'({}): {}",
                    vds.getName(), vds.getId(), statsReturnValue.getExceptionString());
            throw statsReturnValue.getExceptionObject();
        }
        getVdsEventListener().updateSchedulingStats(vds);
        updateV2VJobs();
        // save also dynamic because vm_count data and image_check getting with
        // statistics data
        // TODO: omer- one day remove dynamic save when possible please check if vdsDynamic changed before save
        saveVdsDynamic = true;
        saveVdsStatistics = true;

        alertIfLowDiskSpaceOnHost();
        checkVdsInterfaces();

        if (Config.<Boolean> getValue(ConfigValues.DebugTimerLogging)) {
            log.debug("vds::refreshVdsStats\n{}", this);
        }
    }

    protected void updateV2VJobs() {
        List<V2VJobInfo> v2vJobInfos = vds.getV2VJobs();
        if (v2vJobInfos != null) {
            vdsManager.updateV2VJobInfos(v2vJobInfos);
        }
    }

    private void fetchHostInterfaces() {
        List<VdsNetworkInterface> nics;
        if (vds.getInterfaces().isEmpty()) {
             nics = getDbFacade().getInterfaceDao().getAllInterfacesForVds(vds.getId());
            vds.getInterfaces().addAll(nics);
        } else {
            nics = vds.getInterfaces();
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
        Map<String, Long> disksUsage = vds.getLocalDisksUsage();
        if (disksUsage == null || disksUsage.isEmpty()) {
            return;
        }

        List<String> disksWithLowSpace = new ArrayList<>();
        List<String> disksWithCriticallyLowSpace = new ArrayList<>();
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
            AuditLogableBase logable = new AuditLogableBase(vds.getId());
            logable.addCustomValue("DiskSpace", lowSpaceThreshold.toString());
            logable.addCustomValue("Disks", StringUtils.join(disksWithLowSpace, ", "));
            auditLog(logable, logType);
        }
    }

    // Check if one of the Host interfaces is down, we set the host to non-operational
    // We cannot have Host that don't have all networks in cluster in status Up
    private void checkVdsInterfaces() {
        if (vds.getStatus() != VDSStatus.Up) {
            return;
        }

        Map<String, Set<String>> problematicNicsWithNetworks = new HashMap<>();
        try {
            reportNicStatusChanges();
            problematicNicsWithNetworks = NetworkMonitoringHelper.determineProblematicNics(vds.getInterfaces(),
                    getDbFacade().getNetworkDao().getAllForCluster(vds.getClusterId()));
        } catch (Exception e) {
            log.error("Failure on checkInterfaces on update runtimeinfo for vds: '{}': {}", vds.getName(), e.getMessage());
            log.error("Exception", e);
        } finally {
            if (!problematicNicsWithNetworks.isEmpty()) {
                // we give 1 minutes to a nic to get up in case the nic get the ip from DHCP server
                if (!hostDownTimes.containsKey(vds.getId())) {
                    hostDownTimes.put(vds.getId(), System.currentTimeMillis());
                    return;
                }

                // if less then 1 minutes, still waiting for DHCP
                int delay = Config.<Integer> getValue(ConfigValues.NicDHCPDelayGraceInMS) * 1000;
                if (System.currentTimeMillis() < hostDownTimes.get(vds.getId()) + delay) {
                    return;
                }

                // if we could retrieve it within the timeout, remove from map (for future checks) and set the host to
                // non-operational
                hostDownTimes.remove(vds.getId());

                try {
                    String problematicNicsWithNetworksString =
                            constructNicsWithNetworksString(problematicNicsWithNetworks);

                    vds.setNonOperationalReason(NonOperationalReason.NETWORK_INTERFACE_IS_DOWN);
                    vdsManager.setStatus(VDSStatus.NonOperational, vds);
                    log.info("Host '{}' moved to Non-Operational state because interface/s which are down are needed by required network/s in the current cluster: '{}'",
                            vds.getName(),
                            problematicNicsWithNetworksString);

                    AuditLogableBase logable = new AuditLogableBase(vds.getId());
                    logable.addCustomValue("NicsWithNetworks", problematicNicsWithNetworksString);
                    logable.setCustomId(problematicNicsWithNetworksString);
                    auditLog(logable, AuditLogType.VDS_SET_NONOPERATIONAL_IFACE_DOWN);
                } catch (Exception e) {
                    log.error("checkInterface: Failure on moving host: '{}' to non-operational: {}",
                            vds.getName(), e.getMessage());
                    log.error("Exception", e);
                }
            } else {
                // no nics are down, remove from list if exists
                hostDownTimes.remove(vds.getId());
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
        List<VdsNetworkInterface> interfaces = vds.getInterfaces();
        Set<VdsNetworkInterface> slaves = new HashSet<>();
        Map<String, VdsNetworkInterface> monitoredInterfaces = new HashMap<>();
        Map<String, VdsNetworkInterface> interfaceByName = Entities.entitiesByName(interfaces);

        for (VdsNetworkInterface iface : interfaces) {
            if (iface.getBondName() != null) {
                slaves.add(iface);
            }

            String baseIfaceName = NetworkCommonUtils.stripVlan(iface);

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
                    AuditLogableBase logable = new AuditLogableBase(vds.getId());
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
        if (vdsManager.getbeforeFirstRefresh()) {
            boolean flagsChanged = false;
            final AtomicBoolean processHardwareCapsNeededTemp = new AtomicBoolean();
            vdsManager.refreshCapabilities(processHardwareCapsNeededTemp, vds);
            flagsChanged = processHardwareCapsNeededTemp.get();
            vdsManager.setbeforeFirstRefresh(false);
            refreshedCapabilities = true;
            saveVdsDynamic = true;
            // change the _cpuFlagsChanged flag only if it was false,
            // because get capabilities is called twice on a new server in same
            // loop!
            processHardwareCapsNeeded = processHardwareCapsNeeded ? processHardwareCapsNeeded : flagsChanged;
        } else if (isVdsUpOrGoingToMaintenance || vds.getStatus() == VDSStatus.Error) {
            return;
        }
        // show status UP in audit only when InitVdsOnUpCommand finished successfully
        if (vds.getStatus() != VDSStatus.Up) {
            AuditLogableBase logable = new AuditLogableBase(vds.getId());
            logable.addCustomValue("HostStatus", vds.getStatus().toString());
            auditLog(logable, AuditLogType.VDS_DETECTED);
        }
    }

    private void moveVDSToMaintenanceIfNeeded() {
        if (vds.getStatus() == VDSStatus.PreparingForMaintenance) {
            if (monitoringStrategy.canMoveToMaintenance(vds)) {
                VdsDynamic dbVds = getDbFacade().getVdsDynamicDao().get(vds.getId());
                vds.setMaintenanceReason(dbVds.getMaintenanceReason());
                vdsManager.setStatus(VDSStatus.Maintenance, vds);
                saveVdsDynamic = true;
                saveVdsStatistics = true;
                log.info(
                        "Updated vds status from 'Preparing for Maintenance' to 'Maintenance' in database,  vds '{}'({})",
                        vds.getName(),
                        vds.getId());
            } else {
                vdsMaintenanceTimeoutOccurred = vdsManager.isTimeToRetryMaintenance();
            }
        }
    }

    /**
     * calculate the memory and cpus used by vms based on the number of the running VMs. only DB vms counted currently as
     * we know their provisioned memory value.
     * only vms we know their memory definition are calculated, thus
     * external VMs are added to db on the 1st cycle they appear, and then being added to this calculation
     */
    public static boolean refreshCommitedMemory(VDS host, List<VM> vms) {
        boolean memoryUpdated = false;

        int memCommited = host.getGuestOverhead();
        int vmsCoresCount = 0;

        for (VM vm : vms) {
            // VMs' pending resources are cleared in powering up, so in launch state
            // we shouldn't include them as committed.
            if (vm != null && vm.getStatus() != VMStatus.WaitForLaunch &&
                    vm.getStatus() != VMStatus.Down) {
                memCommited += vm.getVmMemSizeMb();
                memCommited += host.getGuestOverhead();
                vmsCoresCount += vm.getNumOfCpus();
            }
        }

        if (memCommited != host.getMemCommited()) {
            host.setMemCommited(memCommited);
            memoryUpdated = true;
        }
        if (vmsCoresCount != host.getVmsCoresCount()) {
            host.setVmsCoresCount(vmsCoresCount);
            memoryUpdated = true;
        }

        return memoryUpdated;
    }

    private void auditLog(AuditLogableBase auditLogable, AuditLogType logType) {
        auditLogDirector.log(auditLogable, logType);
    }

    private DbFacade getDbFacade() {
        return dbFacade;
    }
}

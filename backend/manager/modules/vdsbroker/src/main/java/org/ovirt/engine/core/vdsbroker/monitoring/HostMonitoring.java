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
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.IVdsEventListener;
import org.ovirt.engine.core.common.businessentities.LogMaxMemoryUsedThresholdType;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.V2VJobInfo;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
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
import org.ovirt.engine.core.common.vdscommands.BrokerCommandCallback;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.VmManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSRecoveringException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostMonitoring implements HostMonitoringInterface {
    private final VDS vds;
    private final VdsManager vdsManager;
    private final VDSStatus firstStatus;
    private final MonitoringStrategy monitoringStrategy;
    private final AtomicBoolean saveVdsDynamic = new AtomicBoolean();
    private final AtomicBoolean processHardwareCapsNeeded = new AtomicBoolean();
    private volatile boolean saveVdsStatistics;
    private volatile boolean refreshedCapabilities = false;
    private static final Map<Guid, Long> hostDownTimes = new HashMap<>();
    private volatile boolean vdsMaintenanceTimeoutOccurred;
    private final Map<String, InterfaceStatus> oldInterfaceStatus = new HashMap<>();
    private final ResourceManager resourceManager;
    private final AuditLogDirector auditLogDirector;
    private final ClusterDao clusterDao;
    private final VdsDynamicDao vdsDynamicDao;
    private final InterfaceDao interfaceDao;
    private final VdsNumaNodeDao vdsNumaNodeDao;
    private final NetworkDao networkDao;
    private static final Logger log = LoggerFactory.getLogger(HostMonitoring.class);

    public HostMonitoring(VdsManager vdsManager,
            VDS vds,
            MonitoringStrategy monitoringStrategy,
            ResourceManager resourceManager,
            ClusterDao clusterDao,
            VdsDynamicDao vdsDynamicDao,
            InterfaceDao interfaceDao,
            VdsNumaNodeDao vdsNumaNodeDao,
            NetworkDao networkDao,
            AuditLogDirector auditLogDirector) {
        this.vdsManager = vdsManager;
        this.vds = vds;
        firstStatus = vds.getStatus();
        this.monitoringStrategy = monitoringStrategy;
        this.resourceManager = resourceManager;
        this.clusterDao = clusterDao;
        this.vdsDynamicDao = vdsDynamicDao;
        this.interfaceDao = interfaceDao;
        this.vdsNumaNodeDao = vdsNumaNodeDao;
        this.networkDao = networkDao;
        this.auditLogDirector = auditLogDirector;
    }

    public void refresh() {
        refreshVdsRunTimeInfo();
    }

    public void postProcessRefresh(boolean succeeded) {
        try {
            try {
                moveVDSToMaintenanceIfNeeded();
                if (firstStatus != vds.getStatus() && vds.getStatus() == VDSStatus.Up) {
                    // use this lock in order to allow only one host updating DB and
                    // calling UpEvent in a time
                    vdsManager.cancelRecoveryJob();
                    if (saveVdsDynamic.get()) {
                        vdsDynamicDao.updateStatus(vds.getId(), vds.getStatus());
                    }
                    log.debug("Host '{}' ({}) firing up event.", vds.getName(), vds.getId());
                    vdsManager.setIsSetNonOperationalExecuted(!getVdsEventListener().vdsUpEvent(vds));
                }
            } finally {
                // save all data to db
                saveDataToDb();
            }
        } catch (Throwable t) {
            logFailureMessage("ResourceManager::refreshVdsRunTimeInfo:", t);
            log.debug("Exception", t);
        }  finally {
            vdsManager.afterRefreshTreatment(succeeded);
        }
    }

    private void refreshVdsRunTimeInfo() {
        try {
            VDSStatus vdsStatus = vds.getStatus();
            boolean isVdsUpOrGoingToMaintenance = vdsStatus == VDSStatus.Up
                    || vdsStatus == VDSStatus.PreparingForMaintenance || vdsStatus == VDSStatus.Error
                    || vdsStatus == VDSStatus.NonOperational;
            if (isVdsUpOrGoingToMaintenance) {
                // check if its time for statistics refresh
                if (vdsManager.isTimeToRefreshStatistics() || vdsStatus == VDSStatus.PreparingForMaintenance) {
                    refreshVdsStats(true);
                } else {
                    refreshVdsRunTimeInfo(true);
                }
            } else {
                refreshCapabilities();
            }
        } catch (VDSRecoveringException e) {
            handleVDSRecoveringException(vds, e);
        } catch (ClassCastException cce) {
            handleClassCastException(cce);
        } catch (Throwable t) {
            log.error("Failure to refresh host '{}' runtime info: {}", vds.getName(), t.getMessage());
            log.debug("Exception", t);
            throw t;
        }
    }

    private void refreshVdsRunTimeInfo(boolean isVdsUpOrGoingToMaintenance) {
        boolean succeeded = false;
        boolean executingAsyncVdsCommand = false;
        try {
            executingAsyncVdsCommand = beforeFirstRefreshTreatment(isVdsUpOrGoingToMaintenance);
            if (!executingAsyncVdsCommand && vdsManager.isTimeToRefreshStatistics()) {
                log.debug("[{}] About to refresh VDS runtime info", vds.getHostName());
                saveVdsDynamic.compareAndSet(false,
                        refreshCommitedMemory(vds, vdsManager.getLastVmsList(), resourceManager));
            }
            succeeded = true;
        } catch (VDSRecoveringException e) {
            handleVDSRecoveringException(vds, e);
        } catch (ClassCastException cce) {
            handleClassCastException(cce);
        } catch (Throwable t) {
            log.error("Failure to refresh host '{}' runtime info: {}", vds.getName(), t.getMessage());
            log.debug("Exception", t);
            throw t;
        } finally {
            if (!executingAsyncVdsCommand) {
                postProcessRefresh(succeeded);
            }
        }
    }

    private void refreshCapabilities() {
        // refresh dynamic data
        vdsManager.refreshCapabilities(vds, new RefreshCapabilitiesCallback(vds));
    }

    class RefreshCapabilitiesCallback implements BrokerCommandCallback {

        private final VDS vds;
        private final VDS oldVds;

        RefreshCapabilitiesCallback(VDS vds) {
            this.vds = vds;
            this.oldVds = vds.clone();
        }

        @Override
        public void onResponse(Map<String, Object> response) {
            try {
                final AtomicBoolean processHardwareNeededAtomic = new AtomicBoolean();
                VDSReturnValue caps = (VDSReturnValue) response.get("result");
                vdsManager.invokeGetHardwareInfo(vds, caps);
                VDSStatus refreshReturnStatus = vdsManager.processRefreshCapabilitiesResponse(processHardwareNeededAtomic,
                        vds,
                        oldVds,
                        caps);
                processRefreshCapabilitiesResponse(processHardwareNeededAtomic);
                if (refreshReturnStatus != VDSStatus.NonOperational) {
                    refreshVdsStats(false);
                } else {
                    refreshVdsRunTimeInfo(false);
                }
            } catch (Throwable t) {
                onFailure(t);
            }
        }

        @Override
        public void onFailure(Throwable t) {
            log.error("Unable to RefreshCapabilities: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
            postProcessRefresh(false);
        }
    }


    private void processRefreshCapabilitiesResponse(AtomicBoolean processHardwareNeededAtomic) {
        processHardwareCapsNeeded.set(processHardwareNeededAtomic.get());
        refreshedCapabilities = true;
        saveVdsDynamic.set(true);
    }

    private void handleVDSRecoveringException(VDS vds, VDSRecoveringException e) {
        // if PreparingForMaintenance and vds is in install failed keep to
        // move vds to maintenance
        if (vds.getStatus() != VDSStatus.PreparingForMaintenance) {
            throw e;
        }
    }

    private void handleClassCastException(ClassCastException cce) {
        // This should occur only if the vdsm API is not the same as the cluster API (version mismatch)
        log.error("Failure to refresh host '{}' runtime info. Incorrect vdsm version for cluster '{}': {}",
                vds.getName(),
                vds.getClusterName(), cce.getMessage());
        log.debug("Exception", cce);
        if (vds.getStatus() != VDSStatus.PreparingForMaintenance && vds.getStatus() != VDSStatus.Maintenance) {
            resourceManager.runVdsCommand(VDSCommandType.SetVdsStatus,
                    new SetVdsStatusVDSCommandParameters(vds.getId(), VDSStatus.Error));
        }
    }

    private void saveDataToDb() {
        if (saveVdsDynamic.get()) {
            vdsManager.updateDynamicData(vds.getDynamicData());
            if (refreshedCapabilities) {
                vdsManager.updateNumaData(vds);
            }
        }

        if (saveVdsStatistics) {
            VdsStatistics stat = vds.getStatisticsData();
            vdsManager.updateStatisticsData(stat);
            checkVdsMemoryThreshold(clusterDao.get(vds.getClusterId()), stat);
            checkVdsCpuThreshold(stat);
            checkVdsNetworkThreshold();
            checkVdsSwapThreshold(stat);

            final List<VdsNetworkStatistics> statistics = new LinkedList<>();
            for (VdsNetworkInterface iface : vds.getInterfaces()) {
                statistics.add(iface.getStatistics());
            }
            if (!statistics.isEmpty()) {
                TransactionSupport.executeInScope(TransactionScopeOption.Required,
                        () -> {
                            interfaceDao.massUpdateStatisticsForVds(statistics);
                            return null;
                        });
            }
            saveNumaStatisticsDataToDb();
        }
    }

    private void saveNumaStatisticsDataToDb() {
        final List<VdsNumaNode> vdsNumaNodesToSave = new ArrayList<>();
        List<VdsNumaNode> updateNumaNodes = vds.getNumaNodeList();
        if (!updateNumaNodes.isEmpty()) {
            List<VdsNumaNode> dbVdsNumaNodes = vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(vds.getId());
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
            vdsNumaNodeDao.massUpdateNumaNodeStatistics(vdsNumaNodesToSave);
        }
    }

    /**
     * check if value is less than configurable threshold , if yes , generated event list message
     */
    private void checkVdsMemoryThreshold(Cluster cluster, VdsStatistics stat) {

        if (stat.getMemFree() == null || stat.getUsageMemPercent() == null) {
            return;
        }

        if (LogMaxMemoryUsedThresholdType.PERCENTAGE == cluster.getLogMaxMemoryUsedThresholdType()) {
            checkVdsMemoryThresholdPercentage(cluster, stat);
        }  else {
            checkVdsMemoryThresholdAbsoluteValue(cluster, stat);
        }
    }

    private void checkVdsMemoryThresholdPercentage(Cluster cluster, VdsStatistics stat) {
        Integer maxUsedPercentageThreshold = cluster.getLogMaxMemoryUsedThreshold();

        if (stat.getUsageMemPercent() > maxUsedPercentageThreshold) {
            logMemoryAuditLog(vds, cluster, stat, AuditLogType.VDS_HIGH_MEM_USE, maxUsedPercentageThreshold);
        }
    }

    private void checkVdsMemoryThresholdAbsoluteValue(Cluster cluster, VdsStatistics stat) {
        Integer maxUsedAbsoluteThreshold =
                cluster.getLogMaxMemoryUsedThreshold();

        if (stat.getMemFree() < maxUsedAbsoluteThreshold) {
            logMemoryAuditLog(vds, cluster, stat, AuditLogType.VDS_LOW_MEM, maxUsedAbsoluteThreshold);
        }
    }

    private void logMemoryAuditLog(VDS vds,
            Cluster cluster,
            VdsStatistics stat,
            AuditLogType valueToLog,
            Integer threshold) {
        AuditLogable logable = createAuditLogableForHost();
        logable.addCustomValue("HostName", vds.getName());
        logable.addCustomValue("Cluster", cluster.getName());
        logable.addCustomValue("AvailableMemory", stat.getMemFree().toString());
        logable.addCustomValue("UsedMemory", stat.getUsageMemPercent().toString());
        logable.addCustomValue("Threshold", threshold.toString());
        auditLog(logable, valueToLog);
    }

    /**
     * check if value is less than configurable threshold , if yes , generated event list message
     */
    private void checkVdsCpuThreshold(VdsStatistics stat) {

        Integer maxUsedPercentageThreshold = Config.getValue(ConfigValues.LogMaxCpuUsedThresholdInPercentage);
        if (stat.getUsageCpuPercent() != null
                && stat.getUsageCpuPercent() > maxUsedPercentageThreshold) {
            AuditLogable logable = createAuditLogableForHost();
            logable.addCustomValue("HostName", vds.getName());
            logable.addCustomValue("UsedCpu", stat.getUsageCpuPercent().toString());
            logable.addCustomValue("Threshold", maxUsedPercentageThreshold.toString());
            auditLog(logable, AuditLogType.VDS_HIGH_CPU_USE);
        }
    }

    /**
     * check if value is less than configurable threshold , if yes , generated event list message
     */
    private void checkVdsNetworkThreshold() {
        Integer maxUsedPercentageThreshold = Config.getValue(ConfigValues.LogMaxNetworkUsedThresholdInPercentage);
        for (VdsNetworkInterface iface : vds.getInterfaces()) {
            Double transmitRate = iface.getStatistics().getTransmitRate();
            Double receiveRate = iface.getStatistics().getReceiveRate();
            if ((transmitRate != null && iface.getStatistics().getTransmitRate().intValue() > maxUsedPercentageThreshold)
                    || (receiveRate != null && iface.getStatistics().getReceiveRate().intValue() > maxUsedPercentageThreshold)) {
                AuditLogable logable = createAuditLogableForHost();
                logable.setCustomId(iface.getName());
                logable.addCustomValue("HostName", vds.getName());
                logable.addCustomValue("InterfaceName", iface.getName());
                logable.addCustomValue("Threshold", maxUsedPercentageThreshold.toString());
                logable.addCustomValue("TransmitRate",
                        transmitRate == null ? "" : String.valueOf(transmitRate.intValue()));
                logable.addCustomValue("ReceiveRate",
                        receiveRate == null ? "" : String.valueOf(receiveRate.intValue()));
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

        long swapUsedPercent = (stat.getSwapTotal() - stat.getSwapFree()) / stat.getSwapTotal();

        // Allow the space to be up to 2% lower than as defined in configuration
        Long allowedMinAvailableThreshold = Math.round(minAvailableThreshold.doubleValue() * THRESHOLD);
        AuditLogType valueToLog = stat.getSwapFree() <  allowedMinAvailableThreshold ?
                AuditLogType.VDS_LOW_SWAP :
                AuditLogType.VDS_HIGH_SWAP_USE;

        if (stat.getSwapFree() < allowedMinAvailableThreshold || swapUsedPercent > maxUsedPercentageThreshold) {
            AuditLogable logable = createAuditLogableForHost();
            logable.addCustomValue("HostName", vds.getName());
            logable.addCustomValue("UsedSwap", Long.toString(swapUsedPercent));
            logable.addCustomValue("AvailableSwapMemory", stat.getSwapFree().toString());
            logable.addCustomValue("Threshold", stat.getSwapFree() < allowedMinAvailableThreshold ?
                    minAvailableThreshold.toString() : maxUsedPercentageThreshold.toString());
            auditLog(logable, valueToLog);
        }
    }

    private void logFailureMessage(String messagePrefix, Throwable t) {
        log.error("{} host={}({}): {}",
                messagePrefix,
                vds.getName(),
                vds.getId(),
                t.getMessage());
    }

    protected IVdsEventListener getVdsEventListener() {
        return resourceManager.getEventListener();
    }

    public void afterRefreshTreatment() {
        try {
            if (processHardwareCapsNeeded.get()) {
                monitoringStrategy.processHardwareCapabilities(vds);
                markIsSetNonOperationalExecuted();
            }

            if (refreshedCapabilities) {
                getVdsEventListener().handleVdsVersion(vds.getId());
                getVdsEventListener().handleVdsFips(vds.getId());
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
        } catch (RuntimeException ex) {
            logFailureMessage("Could not finish afterRefreshTreatment", ex);
            log.debug("Exception", ex);
        }
    }

    private void handleVdsMaintenanceTimeout() {
        getVdsEventListener().handleVdsMaintenanceTimeout(vds.getId());
        vdsManager.calculateNextMaintenanceAttemptTime();
    }

    private void markIsSetNonOperationalExecuted() {
        if (!vdsManager.isSetNonOperationalExecuted()) {
            VdsDynamic vdsDynamic = vdsDynamicDao.get(vds.getId());
            if (vdsDynamic.getStatus() == VDSStatus.NonOperational) {
                vdsManager.setIsSetNonOperationalExecuted(true);
            }
        }
    }

    public void refreshVdsStats(boolean isVdsUpOrGoingToMaintenance) {
        if (Config.<Boolean>getValue(ConfigValues.DebugTimerLogging)) {
            log.debug("vdsManager::refreshVdsStats entered, host='{}'({})",
                    vds.getName(), vds.getId());
        }
        // get statistics data, images checks and vm_count data (dynamic)
        fetchHostInterfaces();
        log.debug("[{}] About to refresh VDS Stats", vds.getHostName());
        resourceManager.runVdsCommand(VDSCommandType.GetStatsAsync,
                new VdsIdAndVdsVDSCommandParametersBase(vds).withCallback(new GetStatsAsyncCallback(isVdsUpOrGoingToMaintenance)));
    }

    class GetStatsAsyncCallback implements BrokerCommandCallback {

        private final boolean vdsUpOrGoingToMaintenance;

        GetStatsAsyncCallback(boolean vdsUpOrGoingToMaintenance) {
            this.vdsUpOrGoingToMaintenance = vdsUpOrGoingToMaintenance;
        }

        @Override
        public void onResponse(Map<String, Object> response) {
            try {
                processRefreshVdsStatsResponse((VDSReturnValue) response.get("result"));
                if (!vdsUpOrGoingToMaintenance) {
                    vdsManager.setStatus(VDSStatus.Up, vds);
                }
                refreshVdsRunTimeInfo(vdsUpOrGoingToMaintenance);
            } catch(Throwable t) {
                onFailure(t);
            }
        }

        @Override
        public void onFailure(Throwable t) {
            log.error("Unable to GetStats: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
            postProcessRefresh(false);
        }
    }

    private void processRefreshVdsStatsResponse(VDSReturnValue statsReturnValue) {
        if (!statsReturnValue.getSucceeded()
                && statsReturnValue.getExceptionObject() != null) {
            log.error("Failed getting vds stats, host='{}'({}): {}",
                    vds.getName(), vds.getId(), statsReturnValue.getExceptionString());
            throw statsReturnValue.getExceptionObject();
        }
        getVdsEventListener().updateSchedulingStats(vds);
        updateV2VJobs();
        // save also dynamic because vm_count data and image_check getting with
        // statistics data
        // TODO: omer- one day remove dynamic save when possible please check if vdsDynamic changed before save
        saveVdsDynamic.set(true);
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
             nics = interfaceDao.getAllInterfacesForVds(vds.getId());
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
            AuditLogable logable = createAuditLogableForHost();
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
            NetworkMonitoringHelper networkMonitoringHelper = new NetworkMonitoringHelper();
            reportNicStatusChanges();
            problematicNicsWithNetworks = networkMonitoringHelper.determineProblematicNics(vds.getInterfaces(),
                    networkDao.getAllForCluster(vds.getClusterId()));
        } catch (Exception e) {
            log.error("Failure on checkInterfaces on update runtime info for host '{}': {}",
                    vds.getName(), e.getMessage());
            log.debug("Exception", e);
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

                    AuditLogable logable = createAuditLogableForHost();
                    logable.addCustomValue("NicsWithNetworks", problematicNicsWithNetworksString);
                    logable.setCustomId(problematicNicsWithNetworksString);
                    auditLog(logable, AuditLogType.VDS_SET_NONOPERATIONAL_IFACE_DOWN);
                } catch (Exception e) {
                    log.error("checkInterface: Failure on moving host: '{}' to non-operational: {}",
                            vds.getName(), e.getMessage());
                    log.debug("Exception", e);
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
                    AuditLogable logable = createAuditLogableForHost();
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

    private boolean beforeFirstRefreshTreatment(boolean isVdsUpOrGoingToMaintenance) {
        boolean executingAsyncVdsCommand = false;
        if (vdsManager.getbeforeFirstRefresh()) {
            executingAsyncVdsCommand = true;
            vdsManager.refreshCapabilities(vds, new BeforeFirstRefreshTreatmentCallback(vds));
        } else if (isVdsUpOrGoingToMaintenance || vds.getStatus() == VDSStatus.Error) {
            return false;
        }
        // show status UP in audit only when InitVdsOnUpCommand finished successfully
        VDSStatus status = vds.getStatus();
        if (status != VDSStatus.Up) {
            AuditLogable logable = createAuditLogableForHost();
            logable.addCustomValue("HostStatus", status.toString());
            auditLog(logable, AuditLogType.VDS_DETECTED);
        }
        return executingAsyncVdsCommand;
    }

    class BeforeFirstRefreshTreatmentCallback implements BrokerCommandCallback {

        private final VDS vds;
        private final VDS oldVds;

        BeforeFirstRefreshTreatmentCallback(VDS vds) {
            this.vds = vds;
            this.oldVds = vds.clone();
        }

        @Override
        public void onResponse(Map<String, Object> response) {
            boolean succeeded = true;
            try {
                final AtomicBoolean processHardwareCapsNeededTemp = new AtomicBoolean();
                VDSReturnValue caps = (VDSReturnValue) response.get("result");
                vdsManager.invokeGetHardwareInfo(vds, caps);
                vdsManager.processRefreshCapabilitiesResponse(processHardwareCapsNeededTemp,
                        vds,
                        oldVds,
                        caps);
                processBeforeFirstRefreshTreatmentResponse(processHardwareCapsNeededTemp);
                if (vdsManager.isTimeToRefreshStatistics()) {
                    saveVdsDynamic.compareAndSet(false,
                            refreshCommitedMemory(vds, vdsManager.getLastVmsList(), resourceManager));
                }
            } catch (Throwable t) {
                succeeded = false;
                onFailure(t);
            } finally {
                if (succeeded) {
                    postProcessRefresh(true);
                }
            }

        }

        @Override
        public void onFailure(Throwable t) {
            log.error("Unable to RefreshCapabilities beforeFirstRefreshTreatment: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
            postProcessRefresh(false);
        }
    }

    private void processBeforeFirstRefreshTreatmentResponse(AtomicBoolean processHardwareCapsNeededTemp) {
        boolean flagsChanged = processHardwareCapsNeededTemp.get();
        vdsManager.setbeforeFirstRefresh(false);
        refreshedCapabilities = true;
        saveVdsDynamic.set(true);
        // change the _cpuFlagsChanged flag only if it was false,
        // because get capabilities is called twice on a new server in same
        // loop!
        processHardwareCapsNeeded.compareAndSet(false, flagsChanged);

    }

    private AuditLogable createAuditLogableForHost() {
        AuditLogable logable = new AuditLogableImpl();
        logable.setVdsId(vds.getId());
        logable.setVdsName(vds.getName());
        logable.setClusterId(vds.getClusterId());
        logable.setClusterName(vds.getClusterName());
        return logable;
    }

    private void moveVDSToMaintenanceIfNeeded() {
        if (vds.getStatus() == VDSStatus.PreparingForMaintenance) {
            if (monitoringStrategy.canMoveToMaintenance(vds)) {
                VdsDynamic dbVds = vdsDynamicDao.get(vds.getId());
                vds.setMaintenanceReason(dbVds.getMaintenanceReason());
                vdsManager.setStatus(VDSStatus.Maintenance, vds);
                saveVdsDynamic.set(true);
                saveVdsStatistics = true;
                log.info(
                        "Updated host status from 'Preparing for Maintenance' to 'Maintenance' in database, host '{}'({})",
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
    public static boolean refreshCommitedMemory(VDS host, Map<Guid, VMStatus> vmIdToStatus, ResourceManager resourceManager) {
        boolean memoryUpdated = false;

        int memCommited = host.getGuestOverhead();
        int vmsCoresCount = 0;

        for (Map.Entry<Guid, VMStatus> entry : vmIdToStatus.entrySet()) {
            VMStatus status = entry.getValue();
            // VMs' pending resources are cleared in powering up, so in launch state
            // we shouldn't include them as committed.
            if (status != VMStatus.WaitForLaunch && status != VMStatus.Down) {
                VmManager vmManager = resourceManager.getVmManager(entry.getKey());
                memCommited += vmManager.getVmMemoryWithOverheadInMB();
                vmsCoresCount += vmManager.getNumOfCpus();
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

    private void auditLog(AuditLogable auditLogable, AuditLogType logType) {
        auditLogDirector.log(auditLogable, logType);
    }
}

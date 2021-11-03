package org.ovirt.engine.core.vdsbroker.monitoring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollVmStatsRefresher extends VmStatsRefresher {

    private static final Logger log = LoggerFactory.getLogger(PollVmStatsRefresher.class);
    public static final long VMS_REFRESH_RATE = Config.<Long> getValue(ConfigValues.VdsRefreshRate) * 1000L;
    public static final int NUMBER_VMS_REFRESHES_BEFORE_SAVE = Config.<Integer> getValue(ConfigValues.NumberVmRefreshesBeforeSave);
    private static final Map<Guid, Integer> vdsIdToNumOfVms = new HashMap<>();

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService schedulerService;
    @Inject
    private ResourceManager resourceManager;
    @Inject
    protected VmDynamicDao vmDynamicDao;
    private ScheduledFuture vmsMonitoringJob;

    public PollVmStatsRefresher(VdsManager vdsManager) {
        super(vdsManager);
    }

    public void poll() {
        try {
            if (isMonitoringNeeded(vdsManager.getStatus())) {
                long fetchTime = System.nanoTime();
                List<Pair<VmDynamic, VdsmVm>> fetchedVms = fetchVms();
                if (fetchedVms == null) {
                    log.info("Failed to fetch vms info for host '{}'({}) - skipping VMs monitoring.", vdsManager.getVdsName(), vdsManager.getVdsId());
                    return;
                }

                getVmsMonitoring().perform(fetchedVms, fetchTime, vdsManager, isStatistics());
                processDevices(filterVmsToDevicesMonitoring(fetchedVms), fetchTime);
                processExternalData(filterVmsToDevicesMonitoring(fetchedVms));
            }
        } catch (Throwable t) {
            log.error("Failed during vms monitoring on host '{}'({}) error is: {}",
                    vdsManager.getVdsName(),
                    vdsManager.getVdsId(),
                    ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception:", t);
        }
    }

    protected boolean isStatistics() {
        return true;
    }

    protected Stream<VdsmVm> filterVmsToDevicesMonitoring(List<Pair<VmDynamic, VdsmVm>> polledVms) {
        return polledVms.stream()
                // we only want to monitor vm devices of vms that already exist in the db
                .filter(monitoredVm -> monitoredVm.getFirst() != null && monitoredVm.getSecond() != null)
                .map(Pair::getSecond);
    }

    public void startMonitoring() {
        vmsMonitoringJob =
                schedulerService.scheduleWithFixedDelay(
                        this::poll,
                        getRefreshRate(),
                        getRefreshRate(),
                        TimeUnit.MILLISECONDS);
    }

    protected long getRefreshRate() {
        return VMS_REFRESH_RATE * NUMBER_VMS_REFRESHES_BEFORE_SAVE;
    }

    public void stopMonitoring() {
        try {
            vmsMonitoringJob.cancel(true);
        } catch (Throwable t) {
            log.debug("Exception stopping VM monitoring: {}", ExceptionUtils.getRootCauseMessage(t));
        }
    }

    /* visible for testing only */
    boolean isMonitoringNeeded(VDSStatus status) {
        switch (status) {
        default:
            return false;
        case Up:
        case Error:
        case NonOperational:
        case PreparingForMaintenance:
            return true;
        }
    }

    private List<Pair<VmDynamic, VdsmVm>> fetchVms() {
        VDSReturnValue returnValue = getAllVmStats();
        if (!returnValue.getSucceeded()) {
            return null;
        }

        List<VdsmVm> vdsmVms = (List<VdsmVm>) returnValue.getReturnValue();
        List<Pair<VmDynamic, VdsmVm>> pairs = matchVms(vdsmVms);
        saveLastVmsList(pairs);
        logNumOfVmsIfChanged(vdsmVms.size());
        return pairs;
    }

    private void logNumOfVmsIfChanged(int numOfReportedVms) {
        Guid vdsId = vdsManager.getVdsId();
        Integer prevNumOfVms = vdsIdToNumOfVms.put(vdsId, numOfReportedVms);
        if (prevNumOfVms == null || prevNumOfVms != numOfReportedVms) {
            log.info("Fetched {} VMs from VDS '{}'", numOfReportedVms, vdsId);
        }
    }

    private void saveLastVmsList(List<Pair<VmDynamic, VdsmVm>> pairs) {
        Map<Guid, VMStatus> vmIdToStatus = pairs.stream()
                .filter(pair -> pair.getFirst() != null)
                .map(Pair::getSecond)
                .filter(Objects::nonNull)
                .map(VdsmVm::getVmDynamic)
                .collect(Collectors.toMap(VmDynamic::getId, VmDynamic::getStatus));
        vdsManager.setLastVmsList(vmIdToStatus);
    }

    protected List<Pair<VmDynamic, VdsmVm>> matchVms(List<VdsmVm> vdsmVms) {
        Map<Guid, VmDynamic> dbVms = vmDynamicDao.getAllRunningForVds(vdsManager.getVdsId()).stream()
                .collect(Collectors.toMap(VmDynamic::getId, vm -> vm));
        StringBuilder logBuilder = log.isDebugEnabled() ? new StringBuilder() : null;
        List<Pair<VmDynamic, VdsmVm>> pairs = vdsmVms.stream()
                .map(vdsmVm -> {
                    if (logBuilder != null) {
                        logBuilder.append(String.format("%s:%s ",
                                vdsmVm.getVmDynamic().getId().toString().substring(0, 8),
                                vdsmVm.getVmDynamic().getStatus()));
                    }

                    VmDynamic dbVm = dbVms.remove(vdsmVm.getId());
                    return new Pair<>(dbVm, vdsmVm);
                })
                .collect(Collectors.toList());

        // the remaining db vms with no corresponding vdsm vm are added accordingly
        dbVms.values().forEach(dbVm -> pairs.add(new Pair<>(dbVm, null)));

        if (logBuilder != null) {
            log.debug(logBuilder.toString());
        }

        return pairs;
    }

    protected VDSReturnValue getAllVmStats() {
        return resourceManager.runVdsCommand(
                VDSCommandType.GetAllVmStats,
                new VdsIdVDSCommandParametersBase(vdsManager.getVdsId()));
    }

    private void processExternalData(Stream<VdsmVm> vms) {
        VmExternalDataMonitoring dataMonitoring = getVmExternalDataMonitoring();
        vms.forEach(vm -> dataMonitoring.updateVm(vm.getId(), vdsManager.getVdsId(), vm.getTpmDataHash(),
                vm.getNvramDataHash()));
    }
}

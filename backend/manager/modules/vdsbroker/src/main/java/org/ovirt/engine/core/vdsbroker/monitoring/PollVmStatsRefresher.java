package org.ovirt.engine.core.vdsbroker.monitoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
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
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollVmStatsRefresher extends VmStatsRefresher {

    private static final Logger log = LoggerFactory.getLogger(PollVmStatsRefresher.class);
    protected static final long VMS_REFRESH_RATE = Config.<Long> getValue(ConfigValues.VdsRefreshRate) * 1000L;
    protected static final int NUMBER_VMS_REFRESHES_BEFORE_SAVE = Config.<Integer> getValue(ConfigValues.NumberVmRefreshesBeforeSave);
    private static final Map<Guid, Integer> vdsIdToNumOfVms = new HashMap<>();

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService schedulerService;
    @Inject
    private ResourceManager resourceManager;
    @Inject
    private VmDynamicDao vmDynamicDao;
    private ScheduledFuture vmsMonitoringJob;
    private StringBuilder logBuilder;

    public PollVmStatsRefresher(VdsManager vdsManager) {
        super(vdsManager);
    }

    @OnTimerMethodAnnotation("poll")
    public void poll() {
        if (isMonitoringNeeded(vdsManager.getStatus())) {
            long fetchTime = System.nanoTime();
            List<Pair<VmDynamic, VdsmVm>> fetchedVms = fetchVms();
            if (fetchedVms == null) {
                log.info("Failed to fetch vms info for host '{}' - skipping VMs monitoring.", vdsManager.getVdsName());
                return;
            }

            getVmsMonitoring().perform(fetchedVms, fetchTime, vdsManager, true);
            Stream<VdsmVm> vdsmVmsToMonitor = filterVmsToDevicesMonitoring(fetchedVms);
            processDevices(vdsmVmsToMonitor, fetchTime);
        }
    }

    private Stream<VdsmVm> filterVmsToDevicesMonitoring(List<Pair<VmDynamic, VdsmVm>> polledVms) {
        return polledVms.stream()
                // we only want to monitor vm devices of vms that already exist in the db
                .filter(monitoredVm -> monitoredVm.getFirst() != null && monitoredVm.getSecond() != null)
                .map(Pair::getSecond);
    }

    public void startMonitoring() {
        vmsMonitoringJob =
                schedulerService.scheduleWithFixedDelay(
                        this::poll,
                        VMS_REFRESH_RATE * NUMBER_VMS_REFRESHES_BEFORE_SAVE,
                        VMS_REFRESH_RATE * NUMBER_VMS_REFRESHES_BEFORE_SAVE,
                        TimeUnit.MILLISECONDS);
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
        return onFetchVms(vdsmVms);
    }

    protected List<Pair<VmDynamic, VdsmVm>> onFetchVms(List<VdsmVm> vdsmVms) {
        if (log.isDebugEnabled()) {
            logBuilder = new StringBuilder();
        }
        List<Pair<VmDynamic, VdsmVm>> pairs = matchVms(vdsmVms);
        saveLastVmsList(pairs);
        logNumOfVmsIfChanged(vdsmVms);
        if (log.isDebugEnabled()) {
            log.debug(logBuilder.toString());
        }
        return pairs;
    }

    private void logNumOfVmsIfChanged(List<VdsmVm> vdsmVms) {
        int numOfVms = vdsmVms.size();
        Guid vdsId = vdsManager.getVdsId();
        Integer prevNumOfVms = vdsIdToNumOfVms.put(vdsId, numOfVms);
        if (prevNumOfVms == null || prevNumOfVms.intValue() != numOfVms) {
            log.info("Fetched {} VMs from VDS '{}'", numOfVms, vdsId);
        }
    }

    private void saveLastVmsList(List<Pair<VmDynamic, VdsmVm>> pairs) {
        List<VmDynamic> vms = pairs.stream()
                .filter(pair -> pair.getFirst() != null)
                .map(pair -> pair.getSecond().getVmDynamic())
                .collect(Collectors.toList());
        vdsManager.setLastVmsList(vms);
    }

    protected List<Pair<VmDynamic, VdsmVm>> matchVms(List<VdsmVm> vdsmVms) {
        Map<Guid, VmDynamic> dbVms = vmDynamicDao.getAllRunningForVds(vdsManager.getVdsId()).stream()
                .collect(Collectors.toMap(VmDynamic::getId, Function.identity()));
        List<Pair<VmDynamic, VdsmVm>> pairs = new ArrayList<>(dbVms.size());
        for (VdsmVm vdsmVm : vdsmVms) {
            VmDynamic dbVm = dbVms.remove(vdsmVm.getId());
            pairs.add(new Pair<>(dbVm, vdsmVm));

            if (log.isDebugEnabled()) {
                logBuilder.append(String.format("%s:%s ",
                        vdsmVm.getVmDynamic().getId().toString().substring(0, 8),
                        vdsmVm.getVmDynamic().getStatus()));
            }
        }
        for (VmDynamic dbVm : dbVms.values()) {
            // non running vms are also treated as changed VMs
            pairs.add(new Pair<>(dbVm, null));
        }
        return pairs;
    }

    protected VDSReturnValue getAllVmStats() {
        return resourceManager.runVdsCommand(
                VDSCommandType.GetAllVmStats,
                new VdsIdVDSCommandParametersBase(vdsManager.getVdsId()));
    }
}

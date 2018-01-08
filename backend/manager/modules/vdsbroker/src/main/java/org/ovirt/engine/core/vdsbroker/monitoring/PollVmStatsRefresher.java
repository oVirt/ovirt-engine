package org.ovirt.engine.core.vdsbroker.monitoring;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollVmStatsRefresher extends VmStatsRefresher {

    private static final Logger log = LoggerFactory.getLogger(PollVmStatsRefresher.class);
    protected static final long VMS_REFRESH_RATE = Config.<Long> getValue(ConfigValues.VdsRefreshRate) * 1000L;
    protected static final int NUMBER_VMS_REFRESHES_BEFORE_SAVE = Config.<Integer> getValue(ConfigValues.NumberVmRefreshesBeforeSave);

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService schedulerService;
    private ScheduledFuture vmsMonitoringJob;

    public PollVmStatsRefresher(VdsManager vdsManager) {
        super(vdsManager);
    }

    @OnTimerMethodAnnotation("poll")
    public void poll() {
        if (isMonitoringNeeded(vdsManager.getStatus())) {
            VmsListFetcher fetcher = new VmsStatisticsFetcher(vdsManager);

            long fetchTime = System.nanoTime();
            if (fetcher.fetch()) {
                getVmsMonitoring().perform(fetcher.getChangedVms(), fetchTime, vdsManager, true);
                Stream<VdsmVm> vdsmVmsToMonitor = filterVmsToDevicesMonitoring(fetcher.getChangedVms());
                processDevices(vdsmVmsToMonitor, fetchTime);
            } else {
                log.info("Failed to fetch vms info for host '{}' - skipping VMs monitoring.", vdsManager.getVdsName());
            }
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
                        0,
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
        // FIXME The below statuses are probably not relevant
        // for monitoring but it's currently not final.
        case NonResponsive:
        case Initializing:
        case Connecting:
            return true;
        }
    }
}

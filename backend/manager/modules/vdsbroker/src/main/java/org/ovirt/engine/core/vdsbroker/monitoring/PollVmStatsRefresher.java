package org.ovirt.engine.core.vdsbroker.monitoring;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollVmStatsRefresher extends VmStatsRefresher {

    private static final Logger log = LoggerFactory.getLogger(PollVmStatsRefresher.class);
    protected static final int VMS_REFRESH_RATE = Config.<Integer> getValue(ConfigValues.VdsRefreshRate) * 1000;
    protected static final int NUMBER_VMS_REFRESHES_BEFORE_SAVE = Config.<Integer> getValue(ConfigValues.NumberVmRefreshesBeforeSave);

    @Inject
    private SchedulerUtilQuartzImpl scheduler;
    private String vmsMonitoringJobId;

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
                //we only want to monitor vm devices for vms that already exist in the db
                Stream<VdsmVm> vdsmVmsToMonitor = fetcher.getChangedVms().stream().
                        filter(monitoredVm -> monitoredVm.getFirst() != null).
                        map(Pair::getSecond);
                processDevices(vdsmVmsToMonitor, fetchTime);
            } else {
                log.info("Failed to fetch vms info for host '{}' - skipping VMs monitoring.", vdsManager.getVdsName());
            }
        }
    }

    public void startMonitoring() {
        vmsMonitoringJobId =
                scheduler.scheduleAFixedDelayJob(
                        this,
                        "poll",
                        new Class[0],
                        new Object[0],
                        0,
                        VMS_REFRESH_RATE * NUMBER_VMS_REFRESHES_BEFORE_SAVE,
                        TimeUnit.MILLISECONDS);
    }

    public void stopMonitoring() {
        scheduler.deleteJob(vmsMonitoringJobId);
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

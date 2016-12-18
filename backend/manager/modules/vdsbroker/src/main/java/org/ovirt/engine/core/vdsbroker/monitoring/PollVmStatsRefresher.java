package org.ovirt.engine.core.vdsbroker.monitoring;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
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
        if (vdsManager.isMonitoringNeeded()) {
            VmsListFetcher fetcher = new VmsStatisticsFetcher(vdsManager);

            long fetchTime = System.nanoTime();
            if (fetcher.fetch()) {
                getVmsMonitoring().perform(fetcher.getChangedVms(), fetchTime, vdsManager, true);
                processDevices(fetcher.getVdsmVms().stream(), fetchTime);
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

}

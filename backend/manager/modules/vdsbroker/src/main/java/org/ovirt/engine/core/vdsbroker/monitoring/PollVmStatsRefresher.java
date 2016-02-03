package org.ovirt.engine.core.vdsbroker.monitoring;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PollVmStatsRefresher extends VmStatsRefresher {

    private static final Logger log = LoggerFactory.getLogger(PollVmStatsRefresher.class);
    protected static final int VMS_REFRESH_RATE = Config.<Integer> getValue(ConfigValues.VdsRefreshRate) * 1000;
    protected static final int NUMBER_VMS_REFRESHES_BEFORE_SAVE = Config.<Integer> getValue(ConfigValues.NumberVmRefreshesBeforeSave);

    @Inject
    private SchedulerUtilQuartzImpl scheduler;
    private String vmsMonitoringJobId;
    private final int refreshRate;

    public PollVmStatsRefresher(VdsManager vdsManager, int refreshRate) {
        super(vdsManager);
        this.refreshRate = refreshRate;
    }

    @OnTimerMethodAnnotation("poll")
    public void poll() {
        if (vdsManager.isMonitoringNeeded()) {
            VmsListFetcher fetcher = getVmsFetcher();

            long fetchTime = System.nanoTime();
            if (fetcher.fetch()) {
                getVmsMonitoring().perform(fetcher.getChangedVms(), fetchTime, vdsManager, isTimeToRefreshStatistics());
                processDevices(fetcher.getVdsmVms().stream().map(VmInternalData::getVmDynamic), fetchTime);
            } else {
                log.info("Failed to fetch vms info for host '{}' - skipping VMs monitoring.", vdsManager.getVdsName());
            }
        }
    }

    protected abstract VmsListFetcher getVmsFetcher();
    protected abstract boolean isTimeToRefreshStatistics();

    public void startMonitoring() {
        vmsMonitoringJobId =
                scheduler.scheduleAFixedDelayJob(
                        this,
                        "poll",
                        new Class[0],
                        new Object[0],
                        0,
                        refreshRate,
                        TimeUnit.MILLISECONDS);
    }

    public void stopMonitoring() {
        scheduler.deleteJob(vmsMonitoringJobId);
    }

}

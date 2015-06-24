package org.ovirt.engine.core.vdsbroker;

import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollVMStatsRefresher extends VMStatsRefresher {
    private static final Logger log = LoggerFactory.getLogger(PollVMStatsRefresher.class);
    protected static final int VMS_REFRESH_RATE = Config.<Integer> getValue(ConfigValues.VdsRefreshRate) * 1000;
    protected static final int NUMBER_VMS_REFRESHES_BEFORE_SAVE = Config.<Integer> getValue(ConfigValues.NumberVmRefreshesBeforeSave);

    protected int refreshIteration;
    protected SchedulerUtil scheduler;
    protected String vmsMonitoringJobId;

    public PollVMStatsRefresher(VdsManager vdsManager, AuditLogDirector auditLog, SchedulerUtil scheduler) {
        super(vdsManager, auditLog);
        this.scheduler = scheduler;
    }

    @OnTimerMethodAnnotation("poll")
    public void poll() {
        if (manager.isMonitoringNeeded()) {
            VmsListFetcher fetcher =
                    getRefreshStatistics() ?
                            new VmsStatisticsFetcher(manager) :
                            new VmsListFetcher(manager);
            long fetchTime = System.nanoTime();
            if (fetcher.fetch()) {
                getVmsMonitoring(fetcher, fetchTime).perform();
            } else {
                log.info("Failed to fetch vms info for host '{}' - skipping VMs monitoring.", manager.getVdsName());
            }
        }
        updateIteration();
    }

    private VmsMonitoring getVmsMonitoring(VmsListFetcher fetcher, long fetchTime) {
        return new VmsMonitoring(manager,
                fetcher.getChangedVms(),
                fetcher.getVmsWithChangedDevices(),
                auditLogDirector,
                fetchTime,
                getRefreshStatistics());
    }

    public void startMonitoring() {
        vmsMonitoringJobId =
                scheduler.scheduleAFixedDelayJob(
                        this,
                        "poll",
                        new Class[0],
                        new Object[0],
                        VMS_REFRESH_RATE,
                        VMS_REFRESH_RATE,
                        TimeUnit.MILLISECONDS);
    }

    public void stopMonitoring() {
        scheduler.deleteJob(vmsMonitoringJobId);
    }

    /**
     * Calculates number of refresh iterations.
     */
    protected void updateIteration() {
        refreshIteration =  (++refreshIteration) % NUMBER_VMS_REFRESHES_BEFORE_SAVE;
    }

    /**
     * @return <code>true</code> if vm statistics should be saved or <code>false</code>
     *          otherwise.
     */
    public boolean getRefreshStatistics() {
        return refreshIteration == 0;
    }
}

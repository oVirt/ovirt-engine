package org.ovirt.engine.core.vdsbroker;

import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;


/**
 * <code>VMStatsRefresher</code> provides abstraction for core responsible for
 * fetching statistics for vms.
 *
 */
public abstract class VMStatsRefresher {
    protected static final int VMS_REFRESH_RATE = Config.<Integer> getValue(ConfigValues.VdsRefreshRate) * 1000;
    protected static final int NUMBER_VMS_REFRESHES_BEFORE_SAVE = Config.<Integer> getValue(ConfigValues.NumberVmRefreshesBeforeSave);

    protected VdsManager manager;
    protected SchedulerUtil sched;
    protected String vmsMonitoringJobId;
    protected int refreshIteration;
    protected AuditLogDirector auditLogDirector;

    public VMStatsRefresher(VdsManager vdsManager, AuditLogDirector auditLog, SchedulerUtil scheduler) {
        manager = vdsManager;
        auditLogDirector = auditLog;
        sched = scheduler;
    }

    /**
     * Performs operations required to start monitoring vms.
     */
    public void startMonitoring() {
        vmsMonitoringJobId =
                sched.scheduleAFixedDelayJob(
                        this,
                        "poll",
                        new Class[0],
                        new Object[0],
                        VMS_REFRESH_RATE,
                        VMS_REFRESH_RATE,
                        TimeUnit.MILLISECONDS);
    }

    /**
     * Perform operations required to stop monitoring vms.
     */
    public void stopMonitoring() {
        sched.deleteJob(vmsMonitoringJobId);
    }

    /**
     * Triggers refresh of vm statistics and analyzes the results.
     */
    public abstract void poll();

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

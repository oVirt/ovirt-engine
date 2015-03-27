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
    protected VdsManager manager;
    protected SchedulerUtil sched;
    protected String vmsMonitoringJobId;
    protected int refreshIteration = 1;
    protected final int numberRefreshesBeforeSave = Config.<Integer> getValue(ConfigValues.NumberVmRefreshesBeforeSave);
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
        int refreshRate = Config.<Integer> getValue(ConfigValues.VdsRefreshRate) * 1000;
        refreshIteration = numberRefreshesBeforeSave - 1;

        vmsMonitoringJobId =
                sched.scheduleAFixedDelayJob(
                        this,
                        "perform",
                        new Class[0],
                        new Object[0],
                        refreshRate,
                        refreshRate,
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
    abstract void perform();

    /**
     * Calculates number of refresh iterations.
     */
    public void updateIteration() {
        if (refreshIteration == numberRefreshesBeforeSave) {
            refreshIteration = 1;
        } else {
            refreshIteration++;
        }
    }

    /**
     * @return <code>true</code> if vm statistics should be saved or <code>false</code>
     *          otherwise.
     */
    public boolean getRefreshStatistics() {
        return (refreshIteration == numberRefreshesBeforeSave);
    }

}

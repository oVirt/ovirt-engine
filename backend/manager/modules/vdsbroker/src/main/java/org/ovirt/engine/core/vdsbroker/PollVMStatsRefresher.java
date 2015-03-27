package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;

public class PollVMStatsRefresher extends VMStatsRefresher {

    public PollVMStatsRefresher(VdsManager vdsManager, AuditLogDirector auditLog, SchedulerUtil scheduler) {
        super(vdsManager, auditLog, scheduler);
    }

    @Override
    @OnTimerMethodAnnotation("perform")
    public void perform() {
        if (manager.isMonitoringNeeded()) {
            VmsListFetcher fetcher =
                    getRefreshStatistics() ?
                            new VmsStatisticsFetcher(manager) :
                            new VmsListFetcher(manager);
            long fetchTime = System.nanoTime();
            fetcher.fetch();
            new VmsMonitoring(manager,
                    fetcher.getChangedVms(),
                    fetcher.getVmsWithChangedDevices(),
                    auditLogDirector,
                    fetchTime
            ).perform();
        }
    }
}

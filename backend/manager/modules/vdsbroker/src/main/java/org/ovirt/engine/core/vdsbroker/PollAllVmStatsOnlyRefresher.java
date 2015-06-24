package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;

public class PollAllVmStatsOnlyRefresher extends PollVMStatsRefresher {

    public PollAllVmStatsOnlyRefresher(VdsManager vdsManager, AuditLogDirector auditLog, SchedulerUtil scheduler) {
        super(vdsManager, auditLog, scheduler);
    }

    @Override
    @OnTimerMethodAnnotation("poll")
    public void poll() {
        if (!getRefreshStatistics()) {
            updateIteration();
            return;
        }

        super.poll();
    }
}

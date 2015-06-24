package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;

public class PollListAndAllVmStatsRefresher extends PollVMStatsRefresher {

    private int refreshIteration;

    public PollListAndAllVmStatsRefresher(VdsManager vdsManager, AuditLogDirector auditLog, SchedulerUtil scheduler) {
        super(vdsManager, auditLog, scheduler, VMS_REFRESH_RATE);
    }

    @Override
    protected VmsListFetcher getVmsFetcher() {
        return getRefreshStatistics() ?
                new VmsStatisticsFetcher(manager) :
                new VmsListFetcher(manager);
    }

    @Override
    @OnTimerMethodAnnotation("poll")
    public void poll() {
        super.poll();
        updateIteration();
    }

    private void updateIteration() {
        refreshIteration =  (++refreshIteration) % NUMBER_VMS_REFRESHES_BEFORE_SAVE;
    }

    @Override
    public boolean getRefreshStatistics() {
        return refreshIteration == 0;
    }
}

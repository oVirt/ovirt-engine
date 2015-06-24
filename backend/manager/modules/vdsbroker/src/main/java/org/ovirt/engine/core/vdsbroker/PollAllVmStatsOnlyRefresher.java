package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;

public class PollAllVmStatsOnlyRefresher extends PollVMStatsRefresher {

    public PollAllVmStatsOnlyRefresher(VdsManager vdsManager, AuditLogDirector auditLog, SchedulerUtil scheduler) {
        super(vdsManager, auditLog, scheduler, getRefreshRate());
    }

    private static int getRefreshRate() {
        return VMS_REFRESH_RATE * NUMBER_VMS_REFRESHES_BEFORE_SAVE;
    }

    @Override
    protected VmsListFetcher getVmsFetcher() {
        return new VmsStatisticsFetcher(manager);
    }

    @Override
    protected boolean getRefreshStatistics() {
        return true;
    }
}

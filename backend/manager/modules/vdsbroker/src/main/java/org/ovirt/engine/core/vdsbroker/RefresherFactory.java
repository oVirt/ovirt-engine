package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;


// TODO Use CDI to inject it
public class RefresherFactory {
    public static VMStatsRefresher create(final VdsManager manager, final AuditLogDirector auditLogDirector, final SchedulerUtil scheduler) {
        return new PollVMStatsRefresher(manager, auditLogDirector, scheduler);
    }
}

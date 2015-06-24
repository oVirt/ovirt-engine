package org.ovirt.engine.core.vdsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;


/**
 * <code>VMStatsRefresher</code> provides abstraction for core responsible for
 * fetching statistics for vms.
 *
 */
public abstract class VMStatsRefresher {
    protected VdsManager vdsManager;
    @Inject
    protected AuditLogDirector auditLogDirector;

    public VMStatsRefresher(VdsManager vdsManager) {
        this.vdsManager = vdsManager;
    }

    /**
     * Performs operations required to start monitoring vms.
     */
    public abstract void startMonitoring();

    /**
     * Perform operations required to stop monitoring vms.
     */
    public abstract void stopMonitoring();

}

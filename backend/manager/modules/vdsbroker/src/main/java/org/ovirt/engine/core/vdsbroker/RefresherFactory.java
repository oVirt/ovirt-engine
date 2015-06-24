package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;
import org.ovirt.engine.core.vdsbroker.jsonrpc.EventVMStatsRefresher;



// TODO Use CDI to inject it
public class RefresherFactory {

    public static VMStatsRefresher create(final VdsManager manager, final AuditLogDirector auditLogDirector, final SchedulerUtil scheduler) {
        Version version = manager.getCompatibilityVersion();
        if (FeatureSupported.jsonProtocol(version) && VdsProtocol.STOMP == manager.getCopyVds().getProtocol()
                && FeatureSupported.vmStatsEvents(version)) {
            return new EventVMStatsRefresher(manager, auditLogDirector, scheduler);
        }
        return new PollListAndAllVmStatsRefresher(manager, auditLogDirector, scheduler);
    }
}

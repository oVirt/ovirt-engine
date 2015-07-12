package org.ovirt.engine.core.vdsbroker;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;
import org.ovirt.engine.core.vdsbroker.jsonrpc.EventVMStatsRefresher;


@Singleton
public class RefresherFactory {

    public VMStatsRefresher create(final VdsManager manager, final AuditLogDirector auditLogDirector, final SchedulerUtil scheduler) {
        Version version = manager.getCompatibilityVersion();
        if (FeatureSupported.jsonProtocol(version) && VdsProtocol.STOMP == manager.getCopyVds().getProtocol()
                && FeatureSupported.vmStatsEvents(version)) {
            return new EventVMStatsRefresher(manager, auditLogDirector, scheduler);
        }
        return new PollListAndAllVmStatsRefresher(manager, auditLogDirector, scheduler);
    }
}

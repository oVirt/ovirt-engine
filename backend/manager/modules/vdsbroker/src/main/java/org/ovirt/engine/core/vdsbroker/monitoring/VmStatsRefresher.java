package org.ovirt.engine.core.vdsbroker.monitoring;

import java.util.Comparator;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.vdsbroker.VdsManager;

/**
 * <code>VMStatsRefresher</code> provides abstraction for core responsible for
 * fetching statistics for vms.
 *
 */
public abstract class VmStatsRefresher {

    protected VdsManager vdsManager;
    @Inject
    protected AuditLogDirector auditLogDirector;

    public VmStatsRefresher(VdsManager vdsManager) {
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

    protected void processDevices(Stream<VmDynamic> vms, long fetchTime) {
        // VmDevicesMonitoring may be injected if this class is converted to a managed bean. Currently the injection
        // here is not performed by container and creates circular dependency during ResourceManager initialization.
        VmDevicesMonitoring.Change deviceChange =
                VmDevicesMonitoring.getInstance().createChange(vdsManager.getVdsId(), fetchTime);
        vms.filter(vmDynamic -> vmDynamic != null && vmDynamic.getStatus() != VMStatus.MigratingTo)
                .sorted(Comparator.comparing(VmDynamic::getId)) // Important to avoid deadlock
                .forEach(vmDynamic -> deviceChange.updateVm(vmDynamic.getId(), vmDynamic.getHash()));
        deviceChange.flush();
    }

    protected VmsMonitoring getVmsMonitoring() {
        // VmsMonitoring may be injected if this class is converted to a managed bean. Currently the injection
        // here is not performed by container and creates circular dependency during ResourceManager initialization.
        return VmsMonitoring.getInstance();
    }

}

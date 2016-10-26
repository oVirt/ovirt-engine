package org.ovirt.engine.core.vdsbroker.monitoring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.di.Injector;

@Singleton
public class BalloonMonitoring {

    private final Map<Guid, Integer> vmsWithBalloonDriverProblem;
    private final Map<Guid, Integer> vmsWithUncontrolledBalloon;

    @Inject
    private AuditLogDirector auditLogDirector;

    private BalloonMonitoring() {
        vmsWithBalloonDriverProblem = new HashMap<>();
        vmsWithUncontrolledBalloon = new HashMap<>();
    }

    protected void process(
            List<Guid> vmIdsWithBalloonDriverNotRequestedOrAvailable,
            List<Guid> vmIdsWithBalloonDriverRequestedAndUnavailable,
            List<Guid> vmIdsWithGuestAgentUpOrBalloonDeflated,
            List<Guid> vmIdsWithGuestAgentDownAndBalloonInfalted) {
        vmIdsWithBalloonDriverNotRequestedOrAvailable.forEach(this::vmBalloonDriverIsNotRequestedOrAvailable);
        vmIdsWithBalloonDriverRequestedAndUnavailable.forEach(this::vmBalloonDriverIsRequestedAndUnavailable);
        vmIdsWithGuestAgentUpOrBalloonDeflated.forEach(this::guestAgentIsUpOrBalloonDeflated);
        vmIdsWithGuestAgentDownAndBalloonInfalted.forEach(this::guestAgentIsDownAndBalloonInfalted);
    }

    // remove the vm from the list of vms with uncontrolled inflated balloon
    private void guestAgentIsUpOrBalloonDeflated(Guid vmId) {
        vmsWithUncontrolledBalloon.remove(vmId);
    }

    // add the vm to the list of vms with uncontrolled inflated balloon or increment its counter
    // if it is already in the list
    private void guestAgentIsDownAndBalloonInfalted(Guid vmId) {
        Integer currentVal = vmsWithUncontrolledBalloon.get(vmId);
        if (currentVal == null) {
            vmsWithUncontrolledBalloon.put(vmId, 1);
        } else {
            vmsWithUncontrolledBalloon.put(vmId, currentVal + 1);
            if (currentVal >= Config.<Integer> getValue(ConfigValues.IterationsWithBalloonProblem)) {
                AuditLogableBase auditLogable = Injector.injectMembers(new AuditLogableBase());
                auditLogable.setVmId(vmId);
                auditLog(auditLogable, AuditLogType.VM_BALLOON_DRIVER_UNCONTROLLED);
                vmsWithUncontrolledBalloon.put(vmId, 0);
            }
        }
    }

    // remove the vm from the list of vms with balloon driver problem
    private void vmBalloonDriverIsNotRequestedOrAvailable(Guid vmId) {
        vmsWithBalloonDriverProblem.remove(vmId);
    }

    // add the vm to the list of vms with balloon driver problem or increment its counter
    // if it is already in the list
    private void vmBalloonDriverIsRequestedAndUnavailable(Guid vmId) {
        Integer currentVal = vmsWithBalloonDriverProblem.get(vmId);
        if (currentVal == null) {
            vmsWithBalloonDriverProblem.put(vmId, 1);
        } else {
            vmsWithBalloonDriverProblem.put(vmId, currentVal + 1);
            if (currentVal >= Config.<Integer> getValue(ConfigValues.IterationsWithBalloonProblem)) {
                AuditLogableBase auditLogable = Injector.injectMembers(new AuditLogableBase());
                auditLogable.setVmId(vmId);
                auditLog(auditLogable, AuditLogType.VM_BALLOON_DRIVER_ERROR);
                vmsWithBalloonDriverProblem.put(vmId, 0);
            }
        }
    }

    protected void auditLog(AuditLogableBase auditLogable, AuditLogType logType) {
        auditLogDirector.log(auditLogable, logType);
    }

}

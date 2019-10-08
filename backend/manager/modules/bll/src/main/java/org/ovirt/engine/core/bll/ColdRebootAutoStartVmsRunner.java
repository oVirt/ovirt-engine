package org.ovirt.engine.core.bll;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.compat.Guid;

/**
 * This class represent a job which is responsible for starting VMs as part of
 * cold reboot process.
 */
@Singleton
public class ColdRebootAutoStartVmsRunner extends AutoStartVmsRunner {

    public ColdRebootAutoStartVmsRunner() {
        super(false);
    }

    @Override
    protected Collection<AutoStartVmToRestart> getInitialVmsToStart() {
        return Collections.emptyList();
    }

    @Override
    protected boolean vmNeedsToBeAutoStarted(VM vm) {
        return vm.getStatus() == VMStatus.Down &&
                vm.getExitStatus() == VmExitStatus.Normal;
    }

    @Override
    protected boolean shouldWaitForVmToStart(VM vm) {
        return false;
    }

    @Override
    protected AutoStartVmToRestart createAutoStartVmToRestart(Guid vmId) {
        return new AutoStartVmToRestart(vmId);
    }

    @Override
    protected AuditLogType getRestartFailedAuditLogType() {
        return AuditLogType.COLD_REBOOT_FAILED;
    }

    @Override
    protected AuditLogType getExceededMaxNumOfRestartsAuditLogType() {
        return AuditLogType.EXCEEDED_MAXIMUM_NUM_OF_COLD_REBOOT_VM_ATTEMPTS;
    }
}

package org.ovirt.engine.core.bll;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.compat.Guid;

/**
 * This class represent a job which is responsible for starting VMs as part of
 * cold reboot process.
 */
@Singleton
public class ColdRebootAutoStartVmsRunner extends AutoStartVmsRunner {

    @Override
    protected Collection<AutoStartVmToRestart> getInitialVmsToStart() {
        return Collections.emptyList();
    }

    @Override
    protected boolean isVmNeedsToBeAutoStarted(Guid vmId) {
        VmDynamic vmDynamic = getVmDynamicDao().get(vmId);
        return vmDynamic.getStatus() == VMStatus.Down &&
                vmDynamic.getExitStatus() == VmExitStatus.Normal;
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

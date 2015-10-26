package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.compat.Guid;

/**
 * This class represent a job which is responsible for running HA VMs
 */
@Singleton
public class HaAutoStartVmsRunner extends AutoStartVmsRunner {

    @Override
    protected Collection<AutoStartVmToRestart> getInitialVmsToStart() {
        // There might be HA VMs which went down just before the engine stopped, we detected
        // the failure and updated the DB but didn't made it to rerun the VM. So here we'll
        // take all the HA VMs which are down because of an error and add them to the set
        List<VM> failedAutoStartVms = getVmDao().getAllFailedAutoStartVms();
        ArrayList<AutoStartVmToRestart> initialFailedVms = new ArrayList<>(failedAutoStartVms.size());
        for (VM vm: failedAutoStartVms) {
            log.info("Found HA VM which is down because of an error, trying to restart it, VM '{}' ({})",
                    vm.getName(), vm.getId());
            initialFailedVms.add(new AutoStartVmToRestart(vm.getId()));
        }
        return initialFailedVms;
    }

    @Override
    protected boolean isVmNeedsToBeAutoStarted(Guid vmId) {
        VmDynamic vmDynamic = getVmDynamicDao().get(vmId);
        return vmDynamic.getStatus() == VMStatus.Down &&
                vmDynamic.getExitStatus() == VmExitStatus.Error;
    }

    @Override
    protected AuditLogType getRestartFailedAuditLogType() {
        return AuditLogType.HA_VM_RESTART_FAILED;
    }

    @Override
    protected AuditLogType getExceededMaxNumOfRestartsAuditLogType() {
        return AuditLogType.EXCEEDED_MAXIMUM_NUM_OF_RESTART_HA_VM_ATTEMPTS;
    }
}

package org.ovirt.engine.core.bll;

import java.util.EnumSet;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Version;

public class SuspendedVMClusterEditChecker implements ClusterEditChecker<VM> {

    @Override
    public boolean isApplicable(VDSGroup oldCluster, VDSGroup newCluster) {
        Version newClusterVersion = newCluster.getCompatibilityVersion();
        Version oldClusterVersion = oldCluster.getCompatibilityVersion();

        return !oldClusterVersion.equals(newClusterVersion);
    }

    @Override
    public boolean check(VM vm) {
        return !EnumSet.of(VMStatus.Suspended, VMStatus.SavingState, VMStatus.RestoringState).contains(vm.getStatus());
    }

    @Override
    public String getMainMessage() {
        return EngineMessage.CLUSTER_WARN_VM_DUE_TO_UNSUPPORTED_MEMORY_RESTORE.name();
    }

    @Override
    public String getDetailMessage(VM entity) {
        return null;
    }
}

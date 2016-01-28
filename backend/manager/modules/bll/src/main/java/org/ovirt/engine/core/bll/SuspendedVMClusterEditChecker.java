package org.ovirt.engine.core.bll;

import java.util.EnumSet;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Version;

public class SuspendedVMClusterEditChecker implements ClusterEditChecker<VM> {

    @Override
    public boolean isApplicable(Cluster oldCluster, Cluster newCluster) {
        Version newClusterVersion = newCluster.getCompatibilityVersion();
        Version oldClusterVersion = oldCluster.getCompatibilityVersion();

        return !oldClusterVersion.equals(newClusterVersion);
    }

    @Override
    public boolean check(VM vm) {
        if (EnumSet.of(VMStatus.Suspended, VMStatus.SavingState, VMStatus.RestoringState).contains(vm.getStatus())) {
            return vm.getCustomCompatibilityVersion() != null;
        }

        return true;
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

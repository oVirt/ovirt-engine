package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Version;

public abstract class AbstractWindowsVmClusterEditChecker implements ClusterEditChecker<VM> {

    @Inject
    protected OsRepository osRepository;

    @Override
    public boolean isApplicable(Cluster oldCluster, Cluster newCluster) {
        return oldCluster.getCompatibilityVersion().lessOrEquals(Version.v4_5) &&
                newCluster.getCompatibilityVersion().greaterOrEquals(Version.v4_6);
    }

    protected boolean isLinux(VM vm) {
        return osRepository.isLinux(vm.getOs());
    }

    protected boolean hasCustomCompatibilityVersion(VM vm) {
        return vm.getCustomCompatibilityVersion() != null;
    }

    protected boolean hasCustomEmulatedMachine(VM vm) {
        return vm.getCustomEmulatedMachine() != null;
    }
}

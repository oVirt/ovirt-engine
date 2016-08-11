package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;

public abstract class AbstractRngDeviceClusterEditChecker implements ClusterEditChecker<VDS> {

    private final VmRngDevice.Source source;

    protected AbstractRngDeviceClusterEditChecker(VmRngDevice.Source source) {
        this.source = source;
    }

    @Override
    public boolean isApplicable(Cluster oldCluster, Cluster newCluster) {
        return !oldCluster.getRequiredRngSources().contains(source) && newCluster.getRequiredRngSources().contains(source);
    }

    @Override
    public boolean check(VDS vds) {
        return vds.getSupportedRngSources().contains(source);
    }
}

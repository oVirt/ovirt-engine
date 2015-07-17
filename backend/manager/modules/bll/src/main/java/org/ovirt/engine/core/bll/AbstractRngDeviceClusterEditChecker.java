package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;

public abstract class AbstractRngDeviceClusterEditChecker implements ClusterEditChecker<VDS> {

    private final VmRngDevice.Source source;

    protected AbstractRngDeviceClusterEditChecker(VmRngDevice.Source source) {
        this.source = source;
    }

    @Override
    public boolean isApplicable(VDSGroup oldCluster, VDSGroup newCluster) {
        return !oldCluster.getRequiredRngSources().contains(source) && newCluster.getRequiredRngSources().contains(source);
    }

    @Override
    public boolean check(VDS vds) {
        return vds.getSupportedRngSources().contains(source);
    }

    @Override
    public String getDetailMessage(VDS entity) {
        return null;
    }
}

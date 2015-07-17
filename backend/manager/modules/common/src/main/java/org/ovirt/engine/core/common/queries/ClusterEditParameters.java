package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.VDSGroup;

public class ClusterEditParameters extends IdQueryParameters {

    private VDSGroup newCluster;

    private ClusterEditParameters() {
    }

    public ClusterEditParameters(VDSGroup newCluster) {
        super(newCluster.getId());

        this.newCluster = newCluster;
    }

    public VDSGroup getNewCluster() {
        return newCluster;
    }
}

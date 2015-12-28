package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.Cluster;

public class ClusterEditParameters extends IdQueryParameters {

    private Cluster newCluster;

    private ClusterEditParameters() {
    }

    public ClusterEditParameters(Cluster newCluster) {
        super(newCluster.getId());

        this.newCluster = newCluster;
    }

    public Cluster getNewCluster() {
        return newCluster;
    }
}

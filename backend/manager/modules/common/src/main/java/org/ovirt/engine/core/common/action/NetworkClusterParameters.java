package org.ovirt.engine.core.common.action;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.network_cluster;

/**
 * Parameters which contain a {@link network_cluster} entity to work on.
 */
public class NetworkClusterParameters extends VdsGroupParametersBase {

    /** Used for serialization. */
    private static final long serialVersionUID = -2902181240270944176L;

    @NotNull
    private network_cluster networkCluster;

    public NetworkClusterParameters() {
    }

    public NetworkClusterParameters(network_cluster networkCluster) {
        super(networkCluster.getcluster_id());
        this.networkCluster = networkCluster;
    }

    public network_cluster getNetworkCluster() {
        return networkCluster;
    }
}

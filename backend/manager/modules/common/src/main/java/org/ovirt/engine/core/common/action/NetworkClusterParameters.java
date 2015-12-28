package org.ovirt.engine.core.common.action;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;

/**
 * Parameters which contain a {@link NetworkCluster} entity to work on.
 */
public class NetworkClusterParameters extends ClusterParametersBase {

    /** Used for serialization. */
    private static final long serialVersionUID = -2902181240270944176L;

    @NotNull
    private NetworkCluster networkCluster;

    public NetworkClusterParameters() {
    }

    public NetworkClusterParameters(NetworkCluster networkCluster) {
        super(networkCluster.getClusterId());
        this.networkCluster = networkCluster;
    }

    public NetworkCluster getNetworkCluster() {
        return networkCluster;
    }
}

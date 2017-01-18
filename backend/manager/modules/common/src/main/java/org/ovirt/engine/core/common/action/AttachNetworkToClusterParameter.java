package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.compat.Guid;

public class AttachNetworkToClusterParameter extends NetworkClusterParameters {
    private static final long serialVersionUID = -2874549285727269806L;

    @Valid
    private Network _network;

    public AttachNetworkToClusterParameter(Cluster cluster, Network network) {
        this(cluster.getId(), network);
    }

    public AttachNetworkToClusterParameter(Guid clusterId, Network net) {
        super(createNetworkCluster(clusterId, net));
        _network = net;
    }

    private static NetworkCluster createNetworkCluster(Guid clusterId, Network net) {
        NetworkCluster networkCluster = net.getCluster();
        if (networkCluster == null) {
            return createNetworkClusterWithDefaultValues(clusterId, net);
        }

        return new NetworkCluster(clusterId,
                net.getId(),
                NetworkStatus.NON_OPERATIONAL,
                networkCluster.isDisplay(),
                networkCluster.isRequired(),
                networkCluster.isMigration(),
                networkCluster.isManagement(),
                networkCluster.isGluster(),
                networkCluster.isDefaultRoute()
        );
    }

    // Cluster attachment data can sometimes be missing, so use defaults in that case.
    private static NetworkCluster createNetworkClusterWithDefaultValues(Guid clusterId, Network net) {
        return new NetworkCluster(clusterId,
                net.getId(),
                NetworkStatus.NON_OPERATIONAL,
                false,
                true,
                false,
                false,
                false,
                false
        );
    }

    public Network getNetwork() {
        return _network;
    }

    AttachNetworkToClusterParameter() {
    }
}

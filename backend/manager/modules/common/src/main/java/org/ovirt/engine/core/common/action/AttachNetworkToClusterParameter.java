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
        super(new NetworkCluster(clusterId,
                net.getId(),
                NetworkStatus.NON_OPERATIONAL,

                // Cluster attachment data can sometimes be missing, so use defaults in that case.
                net.getCluster() == null ? false : net.getCluster().isDisplay(),
                net.getCluster() == null ? true : net.getCluster().isRequired(),
                net.getCluster() == null ? false : net.getCluster().isMigration(),
                net.getCluster() == null ? false : net.getCluster().isManagement(),
                net.getCluster() == null ? false : net.getCluster().isGluster()));
        _network = net;
    }

    public Network getNetwork() {
        return _network;
    }

    AttachNetworkToClusterParameter() {
    }
}

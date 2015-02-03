package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.compat.Guid;

public class AttachNetworkToVdsGroupParameter extends NetworkClusterParameters {
    private static final long serialVersionUID = -2874549285727269806L;

    @Valid
    private Network _network;

    public AttachNetworkToVdsGroupParameter(VDSGroup cluster, Network network) {
        this(cluster.getId(), network);
    }

    public AttachNetworkToVdsGroupParameter(Guid clusterId, Network net) {
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

    AttachNetworkToVdsGroupParameter() {
    }
}

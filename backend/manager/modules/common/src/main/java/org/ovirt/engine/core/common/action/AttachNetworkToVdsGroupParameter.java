package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.NetworkCluster;

public class AttachNetworkToVdsGroupParameter extends NetworkClusterParameters {
    private static final long serialVersionUID = -2874549285727269806L;

    @Valid
    private Network _network;

    public AttachNetworkToVdsGroupParameter(VDSGroup group, Network net) {
        super(new NetworkCluster(group.getId(),
                net.getId(),
                NetworkStatus.NonOperational,

                // Cluster attachment data can sometimes be missing, so use defaults in that case.
                net.getCluster() == null ? false : net.getCluster().getis_display(),
                net.getCluster() == null ? true : net.getCluster().isRequired()));
        _network = net;
    }

    public Network getNetwork() {
        return _network;
    }

    public AttachNetworkToVdsGroupParameter() {
    }
}

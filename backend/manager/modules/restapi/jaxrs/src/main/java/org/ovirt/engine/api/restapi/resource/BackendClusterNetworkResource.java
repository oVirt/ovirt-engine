package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.core.common.action.DisplayNetworkToVdsGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionType;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.resource.AssignedNetworkResource;

public class BackendClusterNetworkResource
    extends AbstractBackendNetworkResource
    implements AssignedNetworkResource {

    protected BackendClusterNetworksResource cluster;

    public BackendClusterNetworkResource(String id, BackendClusterNetworksResource parent) {
        super(id, parent);
        this.cluster = parent;
    }

    @Override
    public Network addParents(Network network) {
        return parent.addParents(network);
    }

    @Override
    public Network update(Network incoming) {
        Network oldNetwork = get();
        if (incoming.isSetDisplay() && (!oldNetwork.isSetDisplay() || (oldNetwork.isDisplay() != incoming.isDisplay()))) {
            performAction(VdcActionType.UpdateDisplayToVdsGroup,
                          new DisplayNetworkToVdsGroupParameters(cluster.getVDSGroup(),
                                                                 map(incoming, map(oldNetwork)),
                                                                 incoming.isDisplay()));
            return get();
        }
        return oldNetwork;
    }
}

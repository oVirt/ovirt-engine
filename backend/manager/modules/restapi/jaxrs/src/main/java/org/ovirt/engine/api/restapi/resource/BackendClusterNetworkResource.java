package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.resource.AssignedNetworkResource;
import org.ovirt.engine.core.common.action.NetworkClusterParameters;
import org.ovirt.engine.core.common.action.VdcActionType;

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
    public Network get() {
        return super.get();
    }

    @Override
    public Network update(Network incoming) {
        validateEnums(Network.class, incoming);
        org.ovirt.engine.core.common.businessentities.network.Network network = map(incoming, map(get()));
        network.getCluster().setnetwork_id(network.getId());
        network.getCluster().setcluster_id(cluster.getVDSGroup().getId());
        performAction(VdcActionType.UpdateNetworkOnCluster,
                      new NetworkClusterParameters(network.getCluster()));
        return get();
    }
}

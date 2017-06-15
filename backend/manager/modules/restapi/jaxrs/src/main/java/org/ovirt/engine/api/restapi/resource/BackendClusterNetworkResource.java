package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.resource.ClusterNetworkResource;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachNetworkToClusterParameter;
import org.ovirt.engine.core.common.action.NetworkClusterParameters;

public class BackendClusterNetworkResource
    extends AbstractBackendNetworkResource
    implements ClusterNetworkResource {

    protected BackendClusterNetworksResource cluster;

    public BackendClusterNetworkResource(String id, BackendClusterNetworksResource parent) {
        super(id, parent, ActionType.DetachNetworkToCluster);
        this.cluster = parent;
    }

    @Override
    public Network update(Network incoming) {
        org.ovirt.engine.core.common.businessentities.network.Network network = map(incoming, map(get()));
        network.getCluster().setNetworkId(network.getId());
        network.getCluster().setClusterId(cluster.getCluster().getId());
        performAction(ActionType.UpdateNetworkOnCluster,
                      new NetworkClusterParameters(network.getCluster()));
        return get();
    }

    @Override
    protected ActionParametersBase getRemoveParameters(org.ovirt.engine.core.common.businessentities.network.Network entity) {
        return new AttachNetworkToClusterParameter(cluster.getCluster(), entity);
    }
}

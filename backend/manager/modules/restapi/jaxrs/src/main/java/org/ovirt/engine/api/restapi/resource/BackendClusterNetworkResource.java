package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.resource.ClusterNetworkResource;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachNetworkToClusterParameter;
import org.ovirt.engine.core.common.action.NetworkClusterParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendClusterNetworkResource
    extends AbstractBackendNetworkResource
    implements ClusterNetworkResource {

    private String clusterId;

    public BackendClusterNetworkResource(String id, BackendClusterNetworksResource parent) {
        super(id, parent, ActionType.DetachNetworkToCluster);
        clusterId = parent.clusterId;
    }

    @Override
    public Network get() {
        org.ovirt.engine.core.common.businessentities.network.Network entity = parent.lookupNetwork(guid);
        if (entity == null) {
            return notFound();
        }
        return addLinks(map(entity), Cluster.class);
    }

    @Override
    public Network update(Network incoming) {
        org.ovirt.engine.core.common.businessentities.network.Network network = map(incoming, map(get()));
        network.getCluster().setNetworkId(network.getId());
        network.getCluster().setClusterId(Guid.createGuidFromString(clusterId));
        performAction(ActionType.UpdateNetworkOnCluster,
                      new NetworkClusterParameters(network.getCluster()));
        return get();
    }

    @Override
    public Network addParents(Network network) {
        network.setCluster(new org.ovirt.engine.api.model.Cluster());
        network.getCluster().setId(clusterId);
        return network;
    }

    @Override
    protected ActionParametersBase getRemoveParameters(org.ovirt.engine.core.common.businessentities.network.Network entity) {
        return new AttachNetworkToClusterParameter(Guid.createGuidFromString(clusterId), entity);
    }
}

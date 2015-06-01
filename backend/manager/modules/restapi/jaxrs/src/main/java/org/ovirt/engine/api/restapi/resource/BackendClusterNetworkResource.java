package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.resource.AssignedNetworkResource;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.action.NetworkClusterParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;

public class BackendClusterNetworkResource
    extends AbstractBackendNetworkResource
    implements AssignedNetworkResource {

    protected BackendClusterNetworksResource cluster;

    public BackendClusterNetworkResource(String id, BackendClusterNetworksResource parent) {
        super(id, parent, VdcActionType.DetachNetworkToVdsGroup);
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
        network.getCluster().setNetworkId(network.getId());
        network.getCluster().setClusterId(cluster.getVDSGroup().getId());
        performAction(VdcActionType.UpdateNetworkOnCluster,
                      new NetworkClusterParameters(network.getCluster()));
        return get();
    }

    @Override
    protected VdcActionParametersBase getRemoveParameters(org.ovirt.engine.core.common.businessentities.network.Network entity) {
        return new AttachNetworkToVdsGroupParameter(cluster.getVDSGroup(), entity);
    }
}

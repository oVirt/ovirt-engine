package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.resource.ClusterNetworkResource;
import org.ovirt.engine.core.common.action.AttachNetworkToClusterParameter;
import org.ovirt.engine.core.common.action.NetworkClusterParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendClusterNetworkResource
    extends AbstractBackendSubResource<Network, org.ovirt.engine.core.common.businessentities.network.Network>
    implements ClusterNetworkResource {

    private Guid clusterId;

    public BackendClusterNetworkResource(Guid clusterId, String networkId) {
        super(networkId, Network.class, org.ovirt.engine.core.common.businessentities.network.Network.class);
        this.clusterId = clusterId;
    }

    @Override
    public Network get() {
        return performGet(VdcQueryType.GetNetworkById, new IdQueryParameters(guid));
    }

    @Override
    public Network update(Network incoming) {
        return performUpdate(
            incoming,
            new UpdatedNetworkResolver(),
            VdcActionType.UpdateNetworkOnCluster,
            new UpdateParametersProvider()
        );
    }

    private class UpdateParametersProvider
        implements ParametersProvider<Network, org.ovirt.engine.core.common.businessentities.network.Network> {

        @Override
        public VdcActionParametersBase getParameters(Network incoming, org.ovirt.engine.core.common.businessentities.network.Network entity) {
            NetworkCluster cluster = getNetwork().getCluster();
            cluster.setNetworkId(guid);
            cluster.setClusterId(clusterId);
            return new NetworkClusterParameters(cluster);
        }
    }

    private class UpdatedNetworkResolver extends EntityIdResolver<Guid> {
        @Override
        public org.ovirt.engine.core.common.businessentities.network.Network lookupEntity(Guid ignore)
            throws BackendFailureException {
            return getEntity(
                org.ovirt.engine.core.common.businessentities.network.Network.class,
                VdcQueryType.GetNetworkById,
                new IdQueryParameters(guid),
                id
            );
        }
    }

    @Override
    public Response remove() {
        get();
        return performAction(
            VdcActionType.DetachNetworkToCluster,
            new AttachNetworkToClusterParameter(getCluster(), getNetwork())
        );
    }

    @Override
    protected Network addParents(Network model) {
        return BackendNetworkHelper.addParents(model);
    }

    private Cluster getCluster() {
        return getEntity(
            Cluster.class,
            VdcQueryType.GetClusterById,
            new IdQueryParameters(clusterId),
            clusterId.toString()
        );
    }

    private org.ovirt.engine.core.common.businessentities.network.Network getNetwork() {
        return getEntity(
            org.ovirt.engine.core.common.businessentities.network.Network.class,
            VdcQueryType.GetNetworkById,
            new IdQueryParameters(guid),
            id
        );
    }
}

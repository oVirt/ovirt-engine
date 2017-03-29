package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.api.resource.ClusterNetworkResource;
import org.ovirt.engine.api.resource.ClusterNetworksResource;
import org.ovirt.engine.core.common.action.AttachNetworkToClusterParameter;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendClusterNetworksResource
    extends AbstractBackendCollectionResource<Network, org.ovirt.engine.core.common.businessentities.network.Network>
    implements ClusterNetworksResource {

    private Guid clusterId;

    public BackendClusterNetworksResource(Guid clusterId) {
        super(Network.class, org.ovirt.engine.core.common.businessentities.network.Network.class);
        this.clusterId = clusterId;
    }

    @Override
    public Networks list() {
        List<org.ovirt.engine.core.common.businessentities.network.Network> entities = getNetworks();
        Networks networks = mapCollection(entities);
        for (Network network : networks.getNetworks()) {
            network.setDisplay(null);
        }
        return networks;
    }

    private Networks mapCollection(List<org.ovirt.engine.core.common.businessentities.network.Network> entities) {
        Networks collection = new Networks();
        for (org.ovirt.engine.core.common.businessentities.network.Network entity : entities) {
            collection.getNetworks().add(addLinks(map(entity)));
        }
        return collection;
    }

    @Override
    public Response add(Network network) {
        validateParameters(network, "id|name");

        List<org.ovirt.engine.core.common.businessentities.network.Network> networks = getNetworks();

        org.ovirt.engine.core.common.businessentities.network.Network net = null;
        if (network.isSetId()) {
            net = getNetworkById(network.getId(), networks);
        }
        else if (network.isSetName()) {
            net = getNetworkByName(network.getName(), networks);
        }
        if (net == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        return performCreate(
            VdcActionType.AttachNetworkToCluster,
            new AttachNetworkToClusterParameter(getCluster(), net),
            new AttachedNetworkResolver(net.getId())
        );
    }

    private org.ovirt.engine.core.common.businessentities.network.Network getNetworkById(String networkId,
            List<org.ovirt.engine.core.common.businessentities.network.Network> networks) {
        for (org.ovirt.engine.core.common.businessentities.network.Network network : networks) {
            if (network.getId().toString().equals(networkId)) {
                return network;
            }
        }
        return null;
    }

    private org.ovirt.engine.core.common.businessentities.network.Network getNetworkByName(String networkName,
            List<org.ovirt.engine.core.common.businessentities.network.Network> networks) {
        for (org.ovirt.engine.core.common.businessentities.network.Network network : networks) {
            if (network.getName().equals(networkName)) {
                return network;
            }
        }
        return null;
    }

    private class AttachedNetworkResolver extends EntityIdResolver<Guid> {
        private Guid guid;

        public AttachedNetworkResolver(Guid guid) {
            this.guid = guid;
        }

        @Override
        public org.ovirt.engine.core.common.businessentities.network.Network lookupEntity(Guid ignore)
            throws BackendFailureException {
            return getEntity(
                org.ovirt.engine.core.common.businessentities.network.Network.class,
                VdcQueryType.GetNetworkById,
                new IdQueryParameters(guid),
                guid.toString()
            );
        }
    }

    @Override
    public Network addParents(Network model) {
        model = BackendNetworkHelper.addParents(model);
        model.setCluster(new org.ovirt.engine.api.model.Cluster());
        model.getCluster().setId(clusterId.toString());
        return model;
    }

    @Override
    public ClusterNetworkResource getNetworkResource(String id) {
        return inject(new BackendClusterNetworkResource(clusterId, id));
    }

    private List<org.ovirt.engine.core.common.businessentities.network.Network> getNetworks() {
        Guid dataCenterId = getCluster().getStoragePoolId();
        IdQueryParameters params = new IdQueryParameters(dataCenterId);
        return getBackendCollection(VdcQueryType.GetAllNetworks, params);
    }

    private Cluster getCluster() {
        return getEntity(
            Cluster.class,
            VdcQueryType.GetClusterById,
            new IdQueryParameters(clusterId),
            clusterId.toString()
        );
    }
}

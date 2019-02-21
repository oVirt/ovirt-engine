package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.api.resource.ClusterNetworkResource;
import org.ovirt.engine.api.resource.ClusterNetworksResource;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachNetworkToClusterParameter;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendClusterNetworksResource
    extends AbstractBackendNetworksResource
    implements ClusterNetworksResource {

    String clusterId;

    public BackendClusterNetworksResource(String clusterId) {
        super(QueryType.GetAllNetworksByClusterId, ActionType.AttachNetworkToCluster);
        this.clusterId = clusterId;
    }

    @Override
    public Networks list() {
        return mapCollection(getBackendCollection(queryType, getQueryParameters()),
                org.ovirt.engine.api.model.Cluster.class);
    }

    @Override
    public Response add(Network network) {
        validateParameters(network, "id|name");

        String networkName = null;
        List<org.ovirt.engine.core.common.businessentities.network.Network> networks = getNetworks();

        if (network.isSetId()) {
            org.ovirt.engine.core.common.businessentities.network.Network net =
                    getNetworkById(network.getId(), networks);
            if (net == null) {
                notFound(Network.class);
            } else {
                networkName = net.getName();
            }
        }

        String networkId = null;
        if (network.isSetName()) {
            org.ovirt.engine.core.common.businessentities.network.Network net =
                    getNetworkByName(network.getName(), networks);
            if (net == null) {
                notFound(Network.class);
            } else {
                networkId = net.getId().toString();
            }
        }

        if (!network.isSetId()) {
            network.setId(networkId);
        } else if (network.isSetName() && !network.getId().equals(networkId)) {
            badRequest("Network ID provided does not match the ID for network with name: " + network.getName());
        }

        org.ovirt.engine.core.common.businessentities.network.Network entity = map(network);
        return performCreate(addAction,
                               getAddParameters(network, entity),
                               new NetworkIdResolver(StringUtils.defaultIfEmpty(network.getName(), networkName)));
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

    @Override
    protected QueryParametersBase getQueryParameters() {
        return new IdQueryParameters(asGuid(clusterId));
    }

    @Override
    protected ActionParametersBase getAddParameters(Network network,
            org.ovirt.engine.core.common.businessentities.network.Network entity) {
        return new AttachNetworkToClusterParameter(getCluster(), entity);
    }

    @Override
    public Network addParents(Network network) {
        network.setCluster(new org.ovirt.engine.api.model.Cluster());
        network.getCluster().setId(clusterId);
        return network;
    }

    protected Cluster getCluster() {
        return getEntity(Cluster.class,
                         QueryType.GetClusterById,
                         new IdQueryParameters(asGuid(clusterId)),
                         clusterId);
    }

    @Override
    public ClusterNetworkResource getNetworkResource(String id) {
        return inject(new BackendClusterNetworkResource(id, this));
    }

    private List<org.ovirt.engine.core.common.businessentities.network.Network> getNetworks() {
        Guid dataCenterId = getCluster().getStoragePoolId();
        IdQueryParameters params = new IdQueryParameters(dataCenterId);
        return getBackendCollection(QueryType.GetAllNetworks, params);
    }
}

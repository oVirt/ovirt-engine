package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.resource.AssignedNetworkResource;
import org.ovirt.engine.api.resource.AssignedNetworksResource;
import org.ovirt.engine.core.common.action.AttachNetworkToClusterParameter;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendClusterNetworksResource
    extends AbstractBackendNetworksResource
    implements AssignedNetworksResource {

    private String clusterId;

    public BackendClusterNetworksResource(String clusterId) {
        super(VdcQueryType.GetAllNetworksByClusterId, VdcActionType.AttachNetworkToCluster);
        this.clusterId = clusterId;
    }

    @Override
    public Response add(Network network) {
        validateParameters(network, "id|name");

        String networkName = null;
        List<org.ovirt.engine.core.common.businessentities.network.Network> networks = getNetworks(clusterId);

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
    protected VdcQueryParametersBase getQueryParameters() {
        return new IdQueryParameters(asGuid(clusterId));
    }

    @Override
    protected VdcActionParametersBase getAddParameters(Network network,
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
                         VdcQueryType.GetClusterByClusterId,
                         new IdQueryParameters(asGuid(clusterId)),
                         clusterId);
    }

    @Override
    public AssignedNetworkResource getNetworkResource(String id) {
        return inject(new BackendClusterNetworkResource(id, this));
    }

    private List<org.ovirt.engine.core.common.businessentities.network.Network> getNetworks(String clusterId) {
        Guid dataCenterId =
                getEntity(Cluster.class,
                        VdcQueryType.GetClusterById,
                        new IdQueryParameters(asGuid(clusterId)),
                        null).getStoragePoolId();
        IdQueryParameters params = new IdQueryParameters(dataCenterId);
        return getBackendCollection(VdcQueryType.GetAllNetworks, params);
    }
}

package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.resource.AssignedNetworkResource;
import org.ovirt.engine.api.resource.AssignedNetworksResource;

import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.queries.GetAllNetworkQueryParamenters;
import org.ovirt.engine.core.common.queries.GetVdsGroupByIdParameters;
import org.ovirt.engine.core.common.queries.GetVdsGroupByVdsGroupIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdsGroupQueryParamenters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.NGuid;

public class BackendClusterNetworksResource
    extends AbstractBackendNetworksResource
    implements AssignedNetworksResource {

    private String clusterId;

    public BackendClusterNetworksResource(String clusterId) {
        super(VdcQueryType.GetAllNetworksByClusterId,
              VdcActionType.AttachNetworkToVdsGroup,
              VdcActionType.DetachNetworkToVdsGroup);
        this.clusterId = clusterId;
    }

    @Override
    public Response add(Network network) {
        validateParameters(network, "name"); //right now, name is mandatory (future - id alone will be enough)
        if (!network.isSetId()) {
            network.setId(getNetworkId(network.getName(), clusterId));
        }
        org.ovirt.engine.core.common.businessentities.network.Network entity = map(network);
        return performCreation(addAction,
                               getActionParameters(network, entity),
                               new NetworkIdResolver(network.getName()));
    }


    @Override
    protected VdcQueryParametersBase getQueryParameters() {
        return new VdsGroupQueryParamenters(asGuid(clusterId));
    }

    @Override
    protected VdcActionParametersBase getActionParameters(Network network, org.ovirt.engine.core.common.businessentities.network.Network entity) {
        return new AttachNetworkToVdsGroupParameter(getVDSGroup(), entity);
    }

    protected String[] getRequiredAddFields() {
        return new String[] { "id" };
    }

    @Override
    public Network addParents(Network network) {
        network.setCluster(new Cluster());
        network.getCluster().setId(clusterId);
        return network;
    }

    protected VDSGroup getVDSGroup() {
        return getEntity(VDSGroup.class,
                         VdcQueryType.GetVdsGroupByVdsGroupId,
                         new GetVdsGroupByVdsGroupIdParameters(asGuid(clusterId)),
                         clusterId);
    }

    @Override
    @SingleEntityResource
    public AssignedNetworkResource getAssignedNetworkSubResource(String id) {
        return inject(new BackendClusterNetworkResource(id, this));
    }

    private String getNetworkId(String networkName, String clusterId) {
            NGuid dataCenterId = getEntity(VDSGroup.class, VdcQueryType.GetVdsGroupById, new GetVdsGroupByIdParameters(asGuid(clusterId)), null).getStoragePoolId();
            GetAllNetworkQueryParamenters params = new GetAllNetworkQueryParamenters(asGuid(dataCenterId));
            List<org.ovirt.engine.core.common.businessentities.network.Network> networks = getBackendCollection(VdcQueryType.GetAllNetworks, params);
            for (org.ovirt.engine.core.common.businessentities.network.Network nw: networks) {
                if (nw.getname().equals(networkName)) {
                    return nw.getId().toString();
                }
            }
            return null;
        }
}

package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.resource.AssignedNetworkResource;
import org.ovirt.engine.api.resource.AssignedNetworksResource;

import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.queries.GetVdsGroupByVdsGroupIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdsGroupQueryParamenters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

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
    protected VdcQueryParametersBase getQueryParameters() {
        return new VdsGroupQueryParamenters(asGuid(clusterId));
    }

    @Override
    protected VdcActionParametersBase getActionParameters(Network network, network entity) {
        return new AttachNetworkToVdsGroupParameter(getVDSGroup(), entity);
    }

    @Override
    protected String[] getRequiredAddFields() {
        return new String[] { "id", "name" };
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
}

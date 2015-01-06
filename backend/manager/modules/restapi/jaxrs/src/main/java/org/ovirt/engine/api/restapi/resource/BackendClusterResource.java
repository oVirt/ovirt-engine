package org.ovirt.engine.api.restapi.resource;


import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.resource.AffinityGroupsResource;
import org.ovirt.engine.api.resource.AssignedCpuProfilesResource;
import org.ovirt.engine.api.resource.AssignedNetworksResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.ClusterResource;
import org.ovirt.engine.api.resource.gluster.GlusterHooksResource;
import org.ovirt.engine.api.resource.gluster.GlusterVolumesResource;
import org.ovirt.engine.api.restapi.resource.gluster.BackendGlusterHooksResource;
import org.ovirt.engine.api.restapi.resource.gluster.BackendGlusterVolumesResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ManagementNetworkOnClusterOperationParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static org.ovirt.engine.api.restapi.resource.BackendClustersResource.SUB_COLLECTIONS;

public class BackendClusterResource<P extends BackendClustersResource>
        extends AbstractBackendActionableResource<Cluster, VDSGroup> implements ClusterResource {

    protected final P parent;
    private final ManagementNetworkFinder managementNetworkFinder;

    public BackendClusterResource(String id, P parent) {
        super(id, Cluster.class, VDSGroup.class, SUB_COLLECTIONS);
        this.parent = parent;
        managementNetworkFinder = new ManagementNetworkFinder(this);
    }

    @Override
    public Cluster get() {
        return performGet(VdcQueryType.GetVdsGroupById, new IdQueryParameters(guid));
    }

    @Override
    public Cluster update(Cluster incoming) {
        validateEnums(Cluster.class, incoming);
        return performUpdate(incoming,
                             new QueryIdResolver<Guid>(VdcQueryType.GetVdsGroupById, IdQueryParameters.class),
                             VdcActionType.UpdateVdsGroup,
                             new UpdateParametersProvider());
    }

    @Override
    public AssignedNetworksResource getAssignedNetworksSubResource() {
        return inject(new BackendClusterNetworksResource(id));
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                                                             VdcQueryType.GetPermissionsForObject,
                                                             new GetPermissionsForObjectParameters(guid),
                                                             Cluster.class,
                                                             VdcObjectType.VdsGroups));
    }

    private class UpdateParametersProvider implements ParametersProvider<Cluster, VDSGroup> {
        @Override
        public VdcActionParametersBase getParameters(Cluster incoming, VDSGroup entity) {
            final VDSGroup cluster = map(incoming, entity);
            final ManagementNetworkOnClusterOperationParameters managementNetworkOnClusterOperationParameters;
            final Guid dcId = getDataCenterId(cluster);
            if (dcId == null) {
                managementNetworkOnClusterOperationParameters =
                        new ManagementNetworkOnClusterOperationParameters(cluster);
            } else {
                final Guid managementNetworkId =
                        managementNetworkFinder.getManagementNetworkId(incoming, dcId);
                managementNetworkOnClusterOperationParameters =
                        new ManagementNetworkOnClusterOperationParameters(cluster, managementNetworkId);
            }
            return managementNetworkOnClusterOperationParameters;
        }
    }

    @Override
    public GlusterVolumesResource getGlusterVolumesResource() {
        return inject(new BackendGlusterVolumesResource(this, id));
    }

    @Override
    protected Cluster doPopulate(Cluster cluster, VDSGroup entity) {
        return parent.doPopulate(cluster, entity);
    }

    @Override
    public GlusterHooksResource getGlusterHooksResource() {
        return inject(new BackendGlusterHooksResource(this));
    }

    @Override
    public AffinityGroupsResource getAffinityGroupsResource() {
        return inject(new BackendAffinityGroupsResource(id));
    }

    @Override
    public AssignedCpuProfilesResource getCpuProfilesResource() {
        return inject(new BackendAssignedCpuProfilesResource(id));
    }

    protected Guid getDataCenterId(VDSGroup cluster) {
        return cluster.getStoragePoolId();
    }

    @Override
    public Response resetEmulatedMachine(Action action) {
        VdcQueryReturnValue result = runQuery(VdcQueryType.GetVdsGroupById, new IdQueryParameters(guid));
        if (result != null && result.getSucceeded() && result.getReturnValue() != null) {
            ManagementNetworkOnClusterOperationParameters param = new ManagementNetworkOnClusterOperationParameters((VDSGroup)result.getReturnValue());
            param.setForceResetEmulatedMachine(true);
            return doAction(VdcActionType.UpdateVdsGroup, param, action);

        } else {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
        }
    }
}

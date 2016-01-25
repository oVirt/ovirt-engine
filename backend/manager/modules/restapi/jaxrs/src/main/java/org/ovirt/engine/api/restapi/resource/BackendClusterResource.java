package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendClustersResource.SUB_COLLECTIONS;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.resource.AffinityGroupsResource;
import org.ovirt.engine.api.resource.AssignedCpuProfilesResource;
import org.ovirt.engine.api.resource.AssignedNetworksResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.ClusterResource;
import org.ovirt.engine.api.resource.NetworkFiltersResource;
import org.ovirt.engine.api.resource.gluster.GlusterHooksResource;
import org.ovirt.engine.api.resource.gluster.GlusterVolumesResource;
import org.ovirt.engine.api.restapi.resource.gluster.BackendGlusterHooksResource;
import org.ovirt.engine.api.restapi.resource.gluster.BackendGlusterVolumesResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ClusterParametersBase;
import org.ovirt.engine.core.common.action.ManagementNetworkOnClusterOperationParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendClusterResource<P extends BackendClustersResource>
        extends AbstractBackendActionableResource<org.ovirt.engine.api.model.Cluster, Cluster> implements ClusterResource {

    protected final P parent;
    private final ManagementNetworkFinder managementNetworkFinder;

    public BackendClusterResource(String id, P parent) {
        super(id, org.ovirt.engine.api.model.Cluster.class, Cluster.class, SUB_COLLECTIONS);
        this.parent = parent;
        managementNetworkFinder = new ManagementNetworkFinder(this);
    }

    @Override
    public org.ovirt.engine.api.model.Cluster get() {
        return performGet(VdcQueryType.GetClusterById, new IdQueryParameters(guid));
    }

    @Override
    public org.ovirt.engine.api.model.Cluster update(org.ovirt.engine.api.model.Cluster incoming) {
        return performUpdate(incoming,
                             new QueryIdResolver<>(VdcQueryType.GetClusterById, IdQueryParameters.class),
                             VdcActionType.UpdateCluster,
                             new UpdateParametersProvider());
    }

    @Override
    public AssignedNetworksResource getNetworksResource() {
        return inject(new BackendClusterNetworksResource(id));
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                                                             VdcQueryType.GetPermissionsForObject,
                                                             new GetPermissionsForObjectParameters(guid),
                                                             org.ovirt.engine.api.model.Cluster.class,
                                                             VdcObjectType.Cluster));
    }

    private class UpdateParametersProvider implements ParametersProvider<org.ovirt.engine.api.model.Cluster, Cluster> {
        @Override
        public VdcActionParametersBase getParameters(org.ovirt.engine.api.model.Cluster incoming, Cluster entity) {
            final Cluster cluster = map(incoming, entity);
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
    protected org.ovirt.engine.api.model.Cluster doPopulate(org.ovirt.engine.api.model.Cluster cluster, Cluster entity) {
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

    @Override
    public NetworkFiltersResource getNetworkFiltersResource() {
        return null;
    }

    protected Guid getDataCenterId(Cluster cluster) {
        return cluster.getStoragePoolId();
    }

    @Override
    public Response resetEmulatedMachine(Action action) {
        VdcQueryReturnValue result = runQuery(VdcQueryType.GetClusterById, new IdQueryParameters(guid));
        if (result != null && result.getSucceeded() && result.getReturnValue() != null) {
            ManagementNetworkOnClusterOperationParameters param = new ManagementNetworkOnClusterOperationParameters(result.getReturnValue());
            param.setForceResetEmulatedMachine(true);
            return doAction(VdcActionType.UpdateCluster, param, action);

        } else {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
        }
    }

    @Override
    public Response remove() {
        get();
        return performAction(VdcActionType.RemoveCluster, new ClusterParametersBase(asGuid(id)));
    }
}

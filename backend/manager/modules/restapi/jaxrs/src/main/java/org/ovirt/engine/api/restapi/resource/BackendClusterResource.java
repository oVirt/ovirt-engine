package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.ClusterUpgradeAction;
import org.ovirt.engine.api.resource.AffinityGroupsResource;
import org.ovirt.engine.api.resource.AssignedCpuProfilesResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.ClusterEnabledFeaturesResource;
import org.ovirt.engine.api.resource.ClusterExternalProvidersResource;
import org.ovirt.engine.api.resource.ClusterNetworksResource;
import org.ovirt.engine.api.resource.ClusterResource;
import org.ovirt.engine.api.resource.NetworkFiltersResource;
import org.ovirt.engine.api.resource.gluster.GlusterHooksResource;
import org.ovirt.engine.api.resource.gluster.GlusterVolumesResource;
import org.ovirt.engine.api.restapi.resource.gluster.BackendGlusterHooksResource;
import org.ovirt.engine.api.restapi.resource.gluster.BackendGlusterVolumesResource;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ClusterOperationParameters;
import org.ovirt.engine.core.common.action.ClusterParametersBase;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendClusterResource<P extends BackendClustersResource>
        extends AbstractBackendActionableResource<org.ovirt.engine.api.model.Cluster, Cluster> implements ClusterResource {

    protected final P parent;
    private final ManagementNetworkFinder managementNetworkFinder;

    public BackendClusterResource(String id, P parent) {
        super(id, org.ovirt.engine.api.model.Cluster.class, Cluster.class);
        this.parent = parent;
        managementNetworkFinder = new ManagementNetworkFinder(this);
    }

    @Override
    public org.ovirt.engine.api.model.Cluster get() {
        return performGet(QueryType.GetClusterById, new IdQueryParameters(guid), LinkHelper.NO_PARENT);
    }

    @Override
    public org.ovirt.engine.api.model.Cluster update(org.ovirt.engine.api.model.Cluster incoming) {
        BackendExternalProviderHelper.completeExternalProviders(this, incoming.getExternalNetworkProviders());
        return performUpdate(incoming,
                             new QueryIdResolver<>(QueryType.GetClusterById, IdQueryParameters.class),
                             ActionType.UpdateCluster,
                             new UpdateParametersProvider());
    }

    @Override
    public ClusterNetworksResource getNetworksResource() {
        return inject(new BackendClusterNetworksResource(id));
    }

    @Override
    public Response syncAllNetworks(Action action) {
        return doAction(ActionType.SyncAllClusterNetworks, new ClusterParametersBase(guid), action);
    }

    @Override
    public Response refreshGlusterHealStatus(Action action) {
        return doAction(ActionType.SyncHealClusterVolumes, new ClusterParametersBase(guid), action);
    }

    @Override
    public Response upgrade(Action action) {
        if(action.getUpgradeAction() == ClusterUpgradeAction.START) {
            return doAction(ActionType.StartClusterUpgrade, new ClusterParametersBase(guid), action);
        }
        return doAction(ActionType.FinishClusterUpgrade, new ClusterParametersBase(guid), action);
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                                                             QueryType.GetPermissionsForObject,
                                                             new GetPermissionsForObjectParameters(guid),
                                                             org.ovirt.engine.api.model.Cluster.class,
                                                             VdcObjectType.Cluster));
    }

    private class UpdateParametersProvider implements ParametersProvider<org.ovirt.engine.api.model.Cluster, Cluster> {
        @Override
        public ActionParametersBase getParameters(org.ovirt.engine.api.model.Cluster incoming, Cluster entity) {
            final Cluster cluster = map(incoming, entity);
            final ClusterOperationParameters clusterOperationParameters;
            final Guid dcId = getDataCenterId(cluster);
            if (dcId == null) {
                clusterOperationParameters = new ClusterOperationParameters(cluster);
            } else {
                final Guid managementNetworkId =
                        managementNetworkFinder.getManagementNetworkId(incoming, dcId);
                clusterOperationParameters = new ClusterOperationParameters(cluster, managementNetworkId);
            }
            return clusterOperationParameters;
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
        return inject(new BackendNetworkFiltersResource());
    }

    protected Guid getDataCenterId(Cluster cluster) {
        return cluster.getStoragePoolId();
    }

    @Override
    public Response resetEmulatedMachine(Action action) {
        QueryReturnValue result = runQuery(QueryType.GetClusterById, new IdQueryParameters(guid));
        if (result != null && result.getSucceeded() && result.getReturnValue() != null) {
            ClusterOperationParameters param = new ClusterOperationParameters(result.getReturnValue());
            param.setForceResetEmulatedMachine(true);
            return doAction(ActionType.UpdateCluster, param, action);

        } else {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
        }
    }

    @Override
    public Response remove() {
        get();
        return performAction(ActionType.RemoveCluster, new ClusterParametersBase(asGuid(id)));
    }

    @Override
    public ClusterEnabledFeaturesResource getEnabledFeaturesResource() {
       return inject(new BackendClusterEnabledFeaturesResource(guid));
    }

    @Override
    public ClusterExternalProvidersResource getExternalNetworkProvidersResource() {
        return inject(new BackendClusterExternalNetworkProvidersResource(guid));
    }
}

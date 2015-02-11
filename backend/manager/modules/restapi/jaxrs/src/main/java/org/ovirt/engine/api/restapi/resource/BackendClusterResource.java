package org.ovirt.engine.api.restapi.resource;


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
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.ovirt.engine.api.restapi.resource.BackendClustersResource.SUB_COLLECTIONS;

public class BackendClusterResource<P extends BackendClustersResource>
        extends AbstractBackendSubResource<Cluster, VDSGroup> implements ClusterResource {

    protected final P parent;

    public BackendClusterResource(String id, P parent) {
        super(id, Cluster.class, VDSGroup.class, SUB_COLLECTIONS);
        this.parent = parent;
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

    protected class UpdateParametersProvider implements ParametersProvider<Cluster, VDSGroup> {
        @Override
        public VdcActionParametersBase getParameters(Cluster incoming, VDSGroup entity) {
            return new ManagementNetworkOnClusterOperationParameters(map(incoming, entity));
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
}

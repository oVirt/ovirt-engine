package org.ovirt.engine.api.restapi.resource;


import org.ovirt.engine.api.model.VnicProfile;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.VnicProfileResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVnicProfileResource extends AbstractBackendVnicProfileResource implements VnicProfileResource {

    protected BackendVnicProfileResource(String id) {
        super(id, BackendVnicProfilesResource.SUB_COLLECTIONS);
    }

    @Override
    public VnicProfile get() {
        return super.get();
    }

    @Override
    public VnicProfile update(VnicProfile resource) {
        return performUpdate(resource,
                new QueryIdResolver<Guid>(VdcQueryType.GetVnicProfileById, IdQueryParameters.class),
                VdcActionType.UpdateVnicProfile,
                new UpdateParametersProvider());
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                VdcQueryType.GetPermissionsForObject,
                new GetPermissionsForObjectParameters(guid),
                VnicProfile.class,
                VdcObjectType.VnicProfile));
    }

    protected class UpdateParametersProvider
            implements ParametersProvider<VnicProfile, org.ovirt.engine.core.common.businessentities.network.VnicProfile> {
        @Override
        public VdcActionParametersBase getParameters(VnicProfile incoming,
                org.ovirt.engine.core.common.businessentities.network.VnicProfile entity) {
            return new VnicProfileParameters(map(incoming, entity));
        }
    }
}

package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.VnicProfile;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.VnicProfileResource;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendVnicProfileResource extends AbstractBackendVnicProfileResource implements VnicProfileResource {

    protected BackendVnicProfileResource(String id) {
        super(id);
    }

    @Override
    public VnicProfile get() {
        return super.get();
    }

    @Override
    public VnicProfile update(VnicProfile resource) {
        return performUpdate(resource,
                new QueryIdResolver<>(QueryType.GetVnicProfileById, IdQueryParameters.class),
                ActionType.UpdateVnicProfile,
                new UpdateParametersProvider());
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return super.getPermissionsResource();
    }

    protected class UpdateParametersProvider
            implements ParametersProvider<VnicProfile, org.ovirt.engine.core.common.businessentities.network.VnicProfile> {
        @Override
        public ActionParametersBase getParameters(VnicProfile incoming,
                org.ovirt.engine.core.common.businessentities.network.VnicProfile entity) {
            return new VnicProfileParameters(map(incoming, entity));
        }
    }
}

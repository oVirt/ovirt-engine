package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.DiskProfile;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.DiskProfileResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.DiskProfileParameters;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendDiskProfileResource extends AbstractBackendDiskProfileResource implements DiskProfileResource {

    protected BackendDiskProfileResource(String id) {
        super(id);
    }

    @Override
    public DiskProfile get() {
        return super.get();
    }

    @Override
    public DiskProfile update(DiskProfile resource) {
        return performUpdate(resource,
                new QueryIdResolver<>(QueryType.GetDiskProfileById, IdQueryParameters.class),
                ActionType.UpdateDiskProfile,
                new UpdateParametersProvider());
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                QueryType.GetPermissionsForObject,
                new GetPermissionsForObjectParameters(guid),
                DiskProfile.class,
                VdcObjectType.DiskProfile));
    }

    protected class UpdateParametersProvider
            implements ParametersProvider<DiskProfile, org.ovirt.engine.core.common.businessentities.profiles.DiskProfile> {
        @Override
        public ActionParametersBase getParameters(DiskProfile incoming,
                org.ovirt.engine.core.common.businessentities.profiles.DiskProfile entity) {
            org.ovirt.engine.core.common.businessentities.profiles.DiskProfile map = map(incoming, entity);
            return new DiskProfileParameters(map);
        }
    }
}

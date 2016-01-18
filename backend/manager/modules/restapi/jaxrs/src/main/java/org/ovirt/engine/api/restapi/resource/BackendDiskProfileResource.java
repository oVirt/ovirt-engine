package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.DiskProfile;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.DiskProfileResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.DiskProfileParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendDiskProfileResource extends AbstractBackendDiskProfileResource implements DiskProfileResource {

    protected BackendDiskProfileResource(String id) {
        super(id, BackendDiskProfilesResource.SUB_COLLECTIONS);
    }

    @Override
    public DiskProfile get() {
        return super.get();
    }

    @Override
    public DiskProfile update(DiskProfile resource) {
        return performUpdate(resource,
                new QueryIdResolver<>(VdcQueryType.GetDiskProfileById, IdQueryParameters.class),
                VdcActionType.UpdateDiskProfile,
                new UpdateParametersProvider());
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                VdcQueryType.GetPermissionsForObject,
                new GetPermissionsForObjectParameters(guid),
                DiskProfile.class,
                VdcObjectType.DiskProfile));
    }

    protected class UpdateParametersProvider
            implements ParametersProvider<DiskProfile, org.ovirt.engine.core.common.businessentities.profiles.DiskProfile> {
        @Override
        public VdcActionParametersBase getParameters(DiskProfile incoming,
                org.ovirt.engine.core.common.businessentities.profiles.DiskProfile entity) {
            org.ovirt.engine.core.common.businessentities.profiles.DiskProfile map = map(incoming, entity);
            return new DiskProfileParameters(map);
        }
    }
}

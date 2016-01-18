package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.CpuProfile;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.CpuProfileResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CpuProfileParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendCpuProfileResource extends AbstractBackendCpuProfileResource implements CpuProfileResource {

    protected BackendCpuProfileResource(String id) {
        super(id, BackendCpuProfilesResource.SUB_COLLECTIONS);
    }

    @Override
    public CpuProfile get() {
        return super.get();
    }

    @Override
    public CpuProfile update(CpuProfile resource) {
        return performUpdate(resource,
                new QueryIdResolver<>(VdcQueryType.GetCpuProfileById, IdQueryParameters.class),
                VdcActionType.UpdateCpuProfile,
                new UpdateParametersProvider());
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                VdcQueryType.GetPermissionsForObject,
                new GetPermissionsForObjectParameters(guid),
                CpuProfile.class,
                VdcObjectType.CpuProfile));
    }

    protected class UpdateParametersProvider
            implements ParametersProvider<CpuProfile, org.ovirt.engine.core.common.businessentities.profiles.CpuProfile> {
        @Override
        public VdcActionParametersBase getParameters(CpuProfile incoming,
                org.ovirt.engine.core.common.businessentities.profiles.CpuProfile entity) {
            org.ovirt.engine.core.common.businessentities.profiles.CpuProfile map = map(incoming, entity);
            return new CpuProfileParameters(map);
        }
    }
}

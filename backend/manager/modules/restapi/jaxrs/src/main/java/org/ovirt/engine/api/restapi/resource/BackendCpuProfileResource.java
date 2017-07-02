package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.CpuProfile;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.CpuProfileResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CpuProfileParameters;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendCpuProfileResource extends AbstractBackendCpuProfileResource implements CpuProfileResource {

    protected BackendCpuProfileResource(String id) {
        super(id);
    }

    @Override
    public CpuProfile get() {
        return super.get();
    }

    @Override
    public CpuProfile update(CpuProfile resource) {
        return performUpdate(resource,
                new QueryIdResolver<>(QueryType.GetCpuProfileById, IdQueryParameters.class),
                ActionType.UpdateCpuProfile,
                new UpdateParametersProvider());
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                QueryType.GetPermissionsForObject,
                new GetPermissionsForObjectParameters(guid),
                CpuProfile.class,
                VdcObjectType.CpuProfile));
    }

    protected class UpdateParametersProvider
            implements ParametersProvider<CpuProfile, org.ovirt.engine.core.common.businessentities.profiles.CpuProfile> {
        @Override
        public ActionParametersBase getParameters(CpuProfile incoming,
                org.ovirt.engine.core.common.businessentities.profiles.CpuProfile entity) {
            org.ovirt.engine.core.common.businessentities.profiles.CpuProfile map = map(incoming, entity);
            return new CpuProfileParameters(map);
        }
    }
}

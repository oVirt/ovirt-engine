package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendRolesResource.SUB_COLLECTIONS;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.PermitsResource;
import org.ovirt.engine.api.resource.RoleResource;
import org.ovirt.engine.core.common.action.RolesOperationsParameters;
import org.ovirt.engine.core.common.action.RolesParameterBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendRoleResource
    extends AbstractBackendSubResource<Role, org.ovirt.engine.core.common.businessentities.Role>
    implements RoleResource {

    private Guid userId;

    public BackendRoleResource(String id) {
        this(id, null);
    }

    public BackendRoleResource(String id, Guid userId) {
        super(id, Role.class, org.ovirt.engine.core.common.businessentities.Role.class, SUB_COLLECTIONS);
        this.userId = userId;
    }

    @Override
    public Role get() {
        return performGet(VdcQueryType.GetRoleById,
                new IdQueryParameters(guid));
    }

    @Override
    protected Role addParents(Role role) {
        if (userId != null) {
            role.setUser(new User());
            role.getUser().setId(userId.toString());
        }
        return role;
    }

    @Override
    public PermitsResource getPermitsResource() {
        return inject(new BackendPermitsResource(guid));
    }

    @Override
    public Role update(Role role) {
        return performUpdate(role,
                new QueryIdResolver<>(VdcQueryType.GetRoleById, IdQueryParameters.class),
                VdcActionType.UpdateRole,
                new UpdateParametersProvider());
    }

    public class UpdateParametersProvider implements ParametersProvider<Role, org.ovirt.engine.core.common.businessentities.Role> {
        @Override
        public VdcActionParametersBase getParameters(Role model, org.ovirt.engine.core.common.businessentities.Role entity) {
            RolesOperationsParameters params = new RolesOperationsParameters();
            params.setRoleId(guid);
            params.setRole(map(model, entity));
            return params;
        }
    }

    @Override
    public Response remove() {
        get();
        return performAction(VdcActionType.RemoveRole, new RolesParameterBase(guid));
    }
}

package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.PermitsResource;
import org.ovirt.engine.api.resource.RoleResource;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RolesOperationsParameters;
import org.ovirt.engine.core.common.action.RolesParameterBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendRoleResource
    extends AbstractBackendSubResource<Role, org.ovirt.engine.core.common.businessentities.Role>
    implements RoleResource {

    private Guid userId;

    public BackendRoleResource(String id) {
        this(id, null);
    }

    public BackendRoleResource(String id, Guid userId) {
        super(id, Role.class, org.ovirt.engine.core.common.businessentities.Role.class);
        this.userId = userId;
    }

    @Override
    public Role get() {
        return performGet(QueryType.GetRoleById,
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
                new QueryIdResolver<>(QueryType.GetRoleById, IdQueryParameters.class),
                ActionType.UpdateRole,
                new UpdateParametersProvider());
    }

    public class UpdateParametersProvider implements ParametersProvider<Role, org.ovirt.engine.core.common.businessentities.Role> {
        @Override
        public ActionParametersBase getParameters(Role model, org.ovirt.engine.core.common.businessentities.Role entity) {
            RolesOperationsParameters params = new RolesOperationsParameters();
            params.setRoleId(guid);
            params.setRole(map(model, entity));
            return params;
        }
    }

    @Override
    public Response remove() {
        get();
        return performAction(ActionType.RemoveRole, new RolesParameterBase(guid));
    }
}

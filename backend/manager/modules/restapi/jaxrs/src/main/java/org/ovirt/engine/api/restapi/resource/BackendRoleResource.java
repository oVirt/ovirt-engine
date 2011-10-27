package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendRolesResource.SUB_COLLECTIONS;

import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.PermitsResource;
import org.ovirt.engine.api.resource.RoleResource;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationByRoleIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendRoleResource
    extends AbstractBackendSubResource<Role, roles>
    implements RoleResource {

    private Guid userId;

    public BackendRoleResource(String id) {
        this(id, null);
    }

    public BackendRoleResource(String id, Guid userId) {
        super(id, Role.class, roles.class, SUB_COLLECTIONS);
        this.userId = userId;
    }

    @Override
    public Role get() {
        return performGet(VdcQueryType.GetRoleById,
                          new MultilevelAdministrationByRoleIdParameters(guid));
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

}

package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.NotImplementedException;
import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.api.model.Roles;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.AssignedRolesResource;
import org.ovirt.engine.api.resource.RoleResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

/**
 * Role assignments to an individual user are mapped to system permissions.
 */
public class BackendAssignedRolesResource
        extends AbstractBackendCollectionResource<Role, Permissions>
        implements AssignedRolesResource {

    private Guid principalId;

    public BackendAssignedRolesResource(Guid principalId) {
        super(Role.class, Permissions.class);
        this.principalId = principalId;
    }

    @Override
    @SingleEntityResource
    public RoleResource getRoleSubResource(String id) {
        return inject(new BackendRoleResource(id, principalId));
    }

    @Override
    public Roles list() {
        return mapCollection(getBackendCollection(VdcQueryType.GetPermissionsByAdElementId,
                                                  new IdQueryParameters(principalId)));
    }

    @Override
    public Response performRemove(String id) {
        throw new NotImplementedException();
    }

    protected Roles mapCollection(List<Permissions> entities) {
        Roles collection = new Roles();
        for (Permissions entity : entities) {
            if (entity.getObjectType() == VdcObjectType.System) {
                collection.getRoles().add(addLinks(map(entity)));
            }
        }
        return collection;
    }

    @Override
    protected Role addParents(Role role) {
        role.setUser(new User());
        role.getUser().setId(principalId.toString());
        return role;
    }

    @Override
    protected Role doPopulate(Role model, Permissions entity) {
        return model;
    }
}

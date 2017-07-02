package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.api.model.Roles;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.AssignedRolesResource;
import org.ovirt.engine.api.resource.RoleResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

/**
 * Role assignments to an individual user are mapped to system permissions.
 */
public class BackendAssignedRolesResource
        extends AbstractBackendCollectionResource<Role, Permission>
        implements AssignedRolesResource {

    private Guid principalId;

    public BackendAssignedRolesResource(Guid principalId) {
        super(Role.class, Permission.class);
        this.principalId = principalId;
    }

    @Override
    public RoleResource getRoleResource(String id) {
        return inject(new BackendRoleResource(id, principalId));
    }

    @Override
    public Roles list() {
        return mapCollection(getBackendCollection(QueryType.GetPermissionsOnBehalfByAdElementId,
                                                  new IdQueryParameters(principalId)));
    }

    protected Roles mapCollection(List<Permission> entities) {
        Roles collection = new Roles();
        for (Permission entity : entities) {
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
}

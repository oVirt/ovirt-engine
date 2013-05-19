package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.api.model.Roles;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.AssignedRolesResource;
import org.ovirt.engine.api.resource.RoleResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

/**
 * Role assignments to an individual user are mapped to system permissions.
 */
public class BackendAssignedRolesResource
        extends AbstractBackendCollectionResource<Role, permissions>
        implements AssignedRolesResource {

    private Guid principalId;

    protected BackendAssignedRolesResource(Guid principalId) {
        super(Role.class, permissions.class);
        this.principalId = principalId;
    }

    @Override
    public Response add(Role role) {
        validateParameters(role, "id|name");
        validateEnums(Role.class, role);
        if (!role.isSetId()) {
            org.ovirt.engine.core.common.businessentities.Role entity = getEntity(
                org.ovirt.engine.core.common.businessentities.Role.class,
                VdcQueryType.GetRoleByName,
                new NameQueryParameters(role.getName()),
                role.getName());
            role.setId(entity.getId().toString());
        }
        return performCreate(VdcActionType.AddSystemPermission,
                               new PermissionsOperationsParametes(newPermission(role.getId())),
                               new QueryIdResolver<Guid>(VdcQueryType.GetPermissionById,
                                                   IdQueryParameters.class));
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
        return performAction(VdcActionType.RemovePermission,
                             new PermissionsOperationsParametes(getPermission(id)));
    }

    protected Roles mapCollection(List<permissions> entities) {
        Roles collection = new Roles();
        for (permissions entity : entities) {
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

    protected permissions newPermission(String roleId) {
        permissions permission = new permissions();
        permission.setad_element_id(principalId);
        permission.setrole_id(new Guid(roleId));
        return permission;
    }

    protected permissions getPermission(String roleId) {
        List<permissions> permissions =
            asCollection(getEntity(ArrayList.class,
                                   VdcQueryType.GetPermissionsByAdElementId,
                                   new IdQueryParameters(principalId),
                                   principalId.toString()));
        for (permissions p : permissions) {
            if (principalId.equals(p.getad_element_id())
                && roleId.equals(p.getrole_id().toString())
                && p.getObjectType() == VdcObjectType.System) {
                return p;
            }
        }
        return handleError(new EntityNotFoundException(roleId), true);
    }

    @Override
    protected Role doPopulate(Role model, permissions entity) {
        return model;
    }
}

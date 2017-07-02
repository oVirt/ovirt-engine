package org.ovirt.engine.api.restapi.resource;

import java.util.Map;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Permission;
import org.ovirt.engine.api.resource.PermissionResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendPermissionResource
        extends AbstractBackendSubResource<Permission, org.ovirt.engine.core.common.businessentities.Permission>
        implements PermissionResource {

    protected BackendAssignedPermissionsResource parent;
    private Guid targetId;
    private Class<? extends BaseResource> suggestedParentType;

    protected BackendPermissionResource(String id,
                                        Guid targetId,
                                        BackendAssignedPermissionsResource parent,
                                        Class<? extends BaseResource> suggestedParentType) {
        super(id, Permission.class, org.ovirt.engine.core.common.businessentities.Permission.class);
        this.targetId = targetId;
        this.parent = parent;
        this.suggestedParentType = suggestedParentType;
    }

    @Override
    public Permission get() {
        return performGet(QueryType.GetPermissionById,
                          new IdQueryParameters(guid),
                          suggestedParentType);
    }

    @Override
    protected Permission addParents(Permission permission) {
        return parent.addParents(permission);
    }

    @Override
    protected Permission map(org.ovirt.engine.core.common.businessentities.Permission entity, Permission template) {
        Map<Guid, DbUser> users = parent.getUsers();
        return parent.map(entity, users.containsKey(entity.getAdElementId()) ? users.get(entity.getAdElementId()) : null);
    }

    @Override
    protected Permission addLinks(Permission model, Class<? extends BaseResource> suggestedParent, String... subCollectionMembersToExclude) {
        return super.addLinks(model, model.getUser() != null ? suggestedParentType : Group.class);
    }

    @Override
    public Response remove() {
        get();
        return performAction(
            ActionType.RemovePermission,
            new PermissionsOperationsParameters(getPermissions(), targetId)
        );
    }

    private org.ovirt.engine.core.common.businessentities.Permission getPermissions() {
        return getEntity(
            org.ovirt.engine.core.common.businessentities.Permission.class,
            QueryType.GetPermissionById,
            new IdQueryParameters(guid),
            guid.toString()
        );
    }
}

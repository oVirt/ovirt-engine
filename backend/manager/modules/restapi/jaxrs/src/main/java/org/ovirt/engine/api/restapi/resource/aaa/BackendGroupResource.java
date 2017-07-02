package org.ovirt.engine.api.restapi.resource.aaa;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.AssignedRolesResource;
import org.ovirt.engine.api.resource.AssignedTagsResource;
import org.ovirt.engine.api.resource.aaa.GroupResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource;
import org.ovirt.engine.api.restapi.resource.BackendAssignedPermissionsResource;
import org.ovirt.engine.api.restapi.resource.BackendAssignedRolesResource;
import org.ovirt.engine.api.restapi.resource.BackendGroupTagsResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendGroupResource
        extends AbstractBackendSubResource<Group, DbGroup>
        implements GroupResource {

    private BackendGroupsResource parent;

    public BackendGroupResource(String id, BackendGroupsResource parent) {
        super(id, Group.class, DbGroup.class);
        this.parent = parent;
    }

    public void setParent(BackendGroupsResource parent) {
        this.parent = parent;
    }

    public BackendGroupsResource getParent() {
        return parent;
    }

    @Override
    public Group get() {
        return performGet(
            QueryType.GetDbGroupById,
            new IdQueryParameters(guid),
            BaseResource.class
        );
    }

    @Override
    public AssignedRolesResource getRolesResource() {
        return inject(new BackendAssignedRolesResource(guid));
    }

    @Override
    public AssignedTagsResource getTagsResource() {
        return inject(new BackendGroupTagsResource(id));
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(
            new BackendAssignedPermissionsResource(
                guid,
                QueryType.GetPermissionsOnBehalfByAdElementId,
                new IdQueryParameters(guid),
                Group.class
            )
        );
    }

    @Override
    public Response remove() {
        get();
        return performAction(ActionType.RemoveGroup, new IdParameters(guid));
    }
}

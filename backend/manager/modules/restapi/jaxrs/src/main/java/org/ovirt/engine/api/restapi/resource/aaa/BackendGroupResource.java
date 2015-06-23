package org.ovirt.engine.api.restapi.resource.aaa;

import static org.ovirt.engine.api.restapi.resource.aaa.BackendGroupsResource.SUB_COLLECTIONS;

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
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendGroupResource
        extends AbstractBackendSubResource<Group, DbGroup>
        implements GroupResource {

    private BackendGroupsResource parent;

    public BackendGroupResource(String id, BackendGroupsResource parent) {
        super(id, Group.class, DbGroup.class, SUB_COLLECTIONS);
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
            VdcQueryType.GetDbGroupById,
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
                VdcQueryType.GetPermissionsByAdElementId,
                new IdQueryParameters(guid),
                Group.class
            )
        );
    }

    @Override
    public Response remove() {
        get();
        return performAction(VdcActionType.RemoveGroup, new IdParameters(guid));
    }
}
